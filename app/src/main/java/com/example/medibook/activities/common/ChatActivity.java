package com.example.medibook.activities.common;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.adapters.MessageAdapter;
import com.example.medibook.models.Appointment;
import com.example.medibook.models.Chat;
import com.example.medibook.models.Doctor;
import com.example.medibook.models.Message;
import com.example.medibook.models.User;
import com.example.medibook.repositories.ChatRepository;
import com.example.medibook.repositories.DoctorRepository;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.utils.PhotoPicker;
import com.example.medibook.utils.StateView;
import com.example.medibook.utils.Ui;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;

/** A live conversation between a patient and a doctor: text and photos. */
public class ChatActivity extends AppCompatActivity {
    private static final String EXTRA_CHAT_ID = "chatId";
    private static final String EXTRA_APPOINTMENT_ID = "appointmentId";
    private static final String EXTRA_PATIENT_ID = "patientId";
    private static final String EXTRA_DOCTOR_ID = "doctorId";
    private static final String EXTRA_PATIENT_NAME = "patientName";
    private static final String EXTRA_DOCTOR_NAME = "doctorName";

    /** From a confirmed appointment: creates the conversation the first time. */
    public static void open(Context c, Appointment a) {
        c.startActivity(new Intent(c, ChatActivity.class)
                .putExtra(EXTRA_APPOINTMENT_ID, a.getAppointmentId())
                .putExtra(EXTRA_PATIENT_ID, a.getPatientId())
                .putExtra(EXTRA_DOCTOR_ID, a.getDoctorId())
                .putExtra(EXTRA_PATIENT_NAME, a.getPatientName())
                .putExtra(EXTRA_DOCTOR_NAME, a.getDoctorName()));
    }

    /** From the inbox: the conversation already exists. */
    public static void open(Context c, String chatId) {
        c.startActivity(new Intent(c, ChatActivity.class).putExtra(EXTRA_CHAT_ID, chatId));
    }

    private final ChatRepository repo = new ChatRepository();
    private String me;
    private Chat chat;
    private boolean amPatient;
    private ListenerRegistration listener;
    private MessageAdapter adapter;
    private RecyclerView list;
    private EditText input;
    private View send, attach, progress;
    private StateView state;
    private PhotoPicker picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = FirebaseAuth.getInstance().getUid();
        if (me == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_chat);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        list = findViewById(R.id.chat_list);
        input = findViewById(R.id.chat_input);
        send = findViewById(R.id.chat_send);
        attach = findViewById(R.id.chat_attach);
        progress = findViewById(R.id.chat_progress);
        state = new StateView(findViewById(R.id.chat_state));

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        list.setLayoutManager(lm);
        adapter = new MessageAdapter(me, url -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
        list.setAdapter(adapter);

        send.setOnClickListener(v -> sendText());
        attach.setOnClickListener(v -> picker.pick());
        setComposerEnabled(false);
        picker = new PhotoPicker(this, "chats", new PhotoPicker.Listener() {
            @Override
            public void onUploading(Uri preview) {
                progress.setVisibility(View.VISIBLE);
                attach.setEnabled(false);
            }

            @Override
            public void onUploaded(String url) {
                progress.setVisibility(View.GONE);
                attach.setEnabled(true);
                repo.send(chat, me, null, url, getString(R.string.chat_photo), error -> {
                    if (error != null) Ui.snack(list, getString(R.string.error_generic, error));
                });
            }

            @Override
            public void onFailed(String error) {
                progress.setVisibility(View.GONE);
                attach.setEnabled(true);
                Ui.snack(list, getString(R.string.photo_failed, error));
            }
        });
        load();
    }

    private void load() {
        state.loading();
        ChatRepository.ChatCallback ready = new ChatRepository.ChatCallback() {
            @Override
            public void onSuccess(Chat c) {
                if (isFinishing()) return;
                chat = c;
                amPatient = me.equals(c.getPatientId());
                showHeader();
                setComposerEnabled(true);
                startListening();
            }

            @Override
            public void onFailure(String error) {
                if (!isFinishing()) state.error(error, ChatActivity.this::load);
            }
        };
        String chatId = getIntent().getStringExtra(EXTRA_CHAT_ID);
        if (chatId != null) {
            repo.get(chatId, ready);
        } else {
            Appointment a = new Appointment();
            a.setAppointmentId(getIntent().getStringExtra(EXTRA_APPOINTMENT_ID));
            a.setPatientId(getIntent().getStringExtra(EXTRA_PATIENT_ID));
            a.setDoctorId(getIntent().getStringExtra(EXTRA_DOCTOR_ID));
            a.setPatientName(getIntent().getStringExtra(EXTRA_PATIENT_NAME));
            a.setDoctorName(getIntent().getStringExtra(EXTRA_DOCTOR_NAME));
            repo.open(a, ready);
        }
    }

    private void showHeader() {
        TextView name = findViewById(R.id.chat_name);
        TextView sub = findViewById(R.id.chat_sub);
        View avatar = findViewById(R.id.chat_avatar);
        String other = amPatient ? Ui.doctorName(chat.getDoctorName()) : Ui.orDash(chat.getPatientName());
        name.setText(other);
        sub.setText(amPatient ? R.string.role_doctor : R.string.role_patient);
        Ui.avatar(avatar.findViewById(R.id.avatar_image), avatar.findViewById(R.id.avatar_initials), null, other);
        if (amPatient) {
            new DoctorRepository().getDoctor(chat.getDoctorId(), new DoctorRepository.DoctorCallback() {
                @Override
                public void onSuccess(Doctor d) {
                    if (!Ui.blank(d.getSpecialty())) sub.setText(d.getSpecialty());
                    Ui.avatar(avatar.findViewById(R.id.avatar_image), avatar.findViewById(R.id.avatar_initials), d.getProfileImage(), other);
                }

                @Override public void onFailure(String error) {}
            });
        } else {
            new UserRepository().getUser(chat.getPatientId(), new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User u) {
                    Ui.avatar(avatar.findViewById(R.id.avatar_image), avatar.findViewById(R.id.avatar_initials), u.getProfileImage(), other);
                }

                @Override public void onFailure(String error) {}
            });
        }
    }

    private void startListening() {
        if (chat == null || listener != null) return;
        listener = repo.listen(chat.getChatId(), new ChatRepository.MessagesListener() {
            @Override
            public void onMessages(List<Message> messages) {
                adapter.submit(messages);
                if (messages.isEmpty()) {
                    state.empty(R.drawable.ic_chat_circle_text, getString(R.string.chat_empty_title),
                            getString(R.string.chat_empty_body), null, null);
                } else {
                    state.hide();
                    list.scrollToPosition(messages.size() - 1);
                }
                repo.markRead(chat.getChatId(), amPatient);
            }

            @Override
            public void onError(String error) {
                state.error(error, ChatActivity.this::load);
            }
        });
    }

    private void sendText() {
        String text = input.getText() == null ? "" : input.getText().toString().trim();
        if (text.isEmpty() || chat == null) return;
        input.setText("");
        repo.send(chat, me, text, null, text, error -> {
            if (error != null) {
                input.setText(text); // give the text back so nothing is lost
                Ui.snack(list, getString(R.string.error_generic, error));
            }
        });
    }

    private void setComposerEnabled(boolean on) {
        send.setEnabled(on);
        attach.setEnabled(on);
        input.setEnabled(on);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }
}
