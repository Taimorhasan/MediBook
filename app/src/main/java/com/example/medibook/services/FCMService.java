package com.example.medibook.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.example.medibook.R;
import com.example.medibook.activities.common.SplashActivity;
import com.example.medibook.repositories.NotificationRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;

/** Shows pushed booking updates and keeps the device token current. */
public class FCMService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "medibook_updates";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        Map<String, String> data = message.getData();
        String title = message.getNotification() != null ? message.getNotification().getTitle() : data.get("title");
        String body = message.getNotification() != null ? message.getNotification().getBody() : data.get("body");
        if (title == null && body == null) return;

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID,
                    getString(R.string.tab_alerts), NotificationManager.IMPORTANCE_DEFAULT));
        }
        PendingIntent open = PendingIntent.getActivity(this, 0,
                new Intent(this, SplashActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        manager.notify((int) System.currentTimeMillis(), new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo)
                .setColor(ContextCompat.getColor(this, R.color.mb_hot))
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(open)
                .build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) new NotificationRepository().updateFCMToken(uid, token);
    }
}
