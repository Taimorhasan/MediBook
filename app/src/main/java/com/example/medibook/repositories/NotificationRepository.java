package com.example.medibook.repositories;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.medibook.models.Notification;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    public interface NotificationsCallback {
        void onSuccess(List<Notification> notifications);
        void onFailure(String error);
    }

    public void sendNotification(String userId, String title, String message, NotificationCallback callback) {
        sendNotification(userId, title, message, "general", callback);
    }

    public void sendNotification(String userId, String title, String message, String type, NotificationCallback callback) {
        if (userId == null || userId.trim().isEmpty()) {
            callback.onFailure("User ID is required for notification");
            return;
        }
        if (title == null || title.trim().isEmpty()) {
            callback.onFailure("Notification title is required");
            return;
        }
        if (message == null || message.trim().isEmpty()) {
            callback.onFailure("Notification message is required");
            return;
        }

        // Store notification in Firestore
        Notification notification = new Notification();
        notification.setNotificationId(db.collection("notifications").document().getId());
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
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

    public void getUserNotifications(String userId, NotificationsCallback callback) {
        if (userId == null || userId.trim().isEmpty()) {
            callback.onFailure("User ID is required");
            return;
        }

        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .limit(50)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Notification> notifications = new ArrayList<>();
                    snapshot.forEach(document -> {
                        Notification notification = document.toObject(Notification.class);
                        if (notification != null) {
                            if (notification.getNotificationId() == null || notification.getNotificationId().trim().isEmpty()) {
                                notification.setNotificationId(document.getId());
                            }
                            notifications.add(notification);
                        }
                    });
                    Collections.sort(notifications, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));
                    callback.onSuccess(notifications);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void markAsRead(String notificationId, NotificationCallback callback) {
        if (notificationId == null || notificationId.trim().isEmpty()) {
            callback.onFailure("Notification ID is missing");
            return;
        }

        db.collection("notifications").document(notificationId)
                .update("read", true)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
