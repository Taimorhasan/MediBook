package com.example.medibook.repositories;

import androidx.annotation.Nullable;
import com.example.medibook.models.Appointment;
import com.example.medibook.models.Chat;
import com.example.medibook.models.Message;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Patient–doctor conversations. Rules allow one only once an appointment between them is confirmed. */
public class ChatRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface ChatCallback {
        void onSuccess(Chat chat);
        void onFailure(String error);
    }

    public interface ChatsCallback {
        void onSuccess(List<Chat> chats);
        void onFailure(String error);
    }

    public interface MessagesListener {
        void onMessages(List<Message> messages);
        void onError(String error);
    }

    public interface Done {
        /** error is null on success. */
        void onDone(@Nullable String error);
    }

    public static String chatId(String patientId, String doctorId) {
        return patientId + "_" + doctorId;
    }

    private DocumentReference chat(String chatId) {
        return db.collection("chats").document(chatId);
    }

    /** Opens the pair's conversation, creating it from this (confirmed) appointment the first time. */
    public void open(Appointment a, ChatCallback callback) {
        String id = chatId(a.getPatientId(), a.getDoctorId());
        chat(id).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.onSuccess(toChat(doc));
                        return;
                    }
                    Map<String, Object> data = new HashMap<>();
                    data.put("chatId", id);
                    data.put("patientId", a.getPatientId());
                    data.put("doctorId", a.getDoctorId());
                    data.put("patientName", a.getPatientName());
                    data.put("doctorName", a.getDoctorName());
                    data.put("appointmentId", a.getAppointmentId());
                    data.put("lastAt", System.currentTimeMillis());
                    data.put("unreadPatient", 0);
                    data.put("unreadDoctor", 0);
                    chat(id).set(data)
                            .addOnSuccessListener(v -> chat(id).get()
                                    .addOnSuccessListener(created -> callback.onSuccess(toChat(created)))
                                    .addOnFailureListener(e -> callback.onFailure(e.getMessage())))
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void get(String chatId, ChatCallback callback) {
        chat(chatId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) callback.onSuccess(toChat(doc));
                    else callback.onFailure("Conversation not found");
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Live messages, oldest first. Remove the returned registration when the screen closes. */
    public ListenerRegistration listen(String chatId, MessagesListener listener) {
        return chat(chatId).collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limitToLast(300)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        listener.onError(e.getMessage());
                        return;
                    }
                    List<Message> messages = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Message m = doc.toObject(Message.class);
                            if (m != null) {
                                m.setMessageId(doc.getId());
                                messages.add(m);
                            }
                        }
                    }
                    listener.onMessages(messages);
                });
    }

    /** Writes the message and the conversation summary together, and bumps the other side's unread count. */
    public void send(Chat c, String senderId, @Nullable String text, @Nullable String imageUrl, String preview, Done done) {
        DocumentReference ref = chat(c.getChatId()).collection("messages").document();
        Map<String, Object> message = new HashMap<>();
        message.put("senderId", senderId);
        if (text != null) message.put("text", text);
        if (imageUrl != null) message.put("imageUrl", imageUrl);
        long now = System.currentTimeMillis();
        message.put("createdAt", now);

        boolean fromPatient = senderId.equals(c.getPatientId());
        Map<String, Object> summary = new HashMap<>();
        summary.put("lastMessage", preview);
        summary.put("lastAt", now);
        summary.put("lastSenderId", senderId);
        summary.put(fromPatient ? "unreadDoctor" : "unreadPatient", FieldValue.increment(1));

        WriteBatch batch = db.batch();
        batch.set(ref, message);
        batch.update(chat(c.getChatId()), summary);
        batch.commit()
                .addOnSuccessListener(v -> done.onDone(null))
                .addOnFailureListener(e -> done.onDone(e.getMessage()));
    }

    public void markRead(String chatId, boolean asPatient) {
        chat(chatId).update(asPatient ? "unreadPatient" : "unreadDoctor", 0);
    }

    /** The user's conversations, most recent first. */
    public void myChats(String uid, boolean asPatient, ChatsCallback callback) {
        db.collection("chats").whereEqualTo(asPatient ? "patientId" : "doctorId", uid).get()
                .addOnSuccessListener(snapshot -> {
                    List<Chat> chats = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) chats.add(toChat(doc));
                    Collections.sort(chats, (a, b) -> Long.compare(b.getLastAt(), a.getLastAt()));
                    callback.onSuccess(chats);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private static Chat toChat(DocumentSnapshot doc) {
        Chat c = doc.toObject(Chat.class);
        if (c == null) c = new Chat();
        c.setChatId(doc.getId());
        return c;
    }
}
