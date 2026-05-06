package com.example.medibook.repositories;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.medibook.models.Notification;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class NotificationRepository {
    private static final String TAG = "NotificationRepository";
    private FirebaseFirestore db;

    public NotificationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface NotificationCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public void sendNotification(String userId, String title, String message, NotificationCallback callback) {
        // Store notification in Firestore
        Notification notification = new Notification();
        notification.setNotificationId(db.collection("notifications").document().getId());
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTimestamp(System.currentTimeMillis());
        notification.setRead(false);

        db.collection("notifications").document(notification.getNotificationId())
                .set(notification)
                .addOnSuccessListener(aVoid -> {
                    // Send FCM message
                    sendFCMMessage(userId, title, message, callback);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private void sendFCMMessage(String userId, String title, String message, NotificationCallback callback) {
        // Get user's FCM token from Firestore
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String fcmToken = documentSnapshot.getString("fcmToken");
                    if (fcmToken != null && !fcmToken.isEmpty()) {
                        // Send FCM message
                        Map<String, String> data = new HashMap<>();
                        data.put("title", title);
                        data.put("body", message);

                        RemoteMessage.Builder messageBuilder = new RemoteMessage.Builder(fcmToken)
                                .setMessageId(Integer.toString((int) System.currentTimeMillis()))
                                .setData(data);

                        FirebaseMessaging.getInstance().send(messageBuilder.build());
                        callback.onSuccess();
                    } else {
                        Log.w(TAG, "No FCM token found for user: " + userId);
                        callback.onSuccess(); // Still consider it success since notification is stored
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get FCM token", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public void updateFCMToken(String userId, String fcmToken) {
        db.collection("users").document(userId)
                .update("fcmToken", fcmToken)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token updated for user: " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update FCM token", e));
    }
}