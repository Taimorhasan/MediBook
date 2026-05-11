package com.example.medibook.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medibook.R;
import com.example.medibook.models.Notification;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertViewHolder> {

    private List<Notification> notificationsList;
    private Context context;
    private FirebaseFirestore db;

    public AlertsAdapter(List<Notification> notificationsList, Context context) {
        this.notificationsList = notificationsList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        holder.bind(notificationsList.get(position));
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    class AlertViewHolder extends RecyclerView.ViewHolder {
        private View accentLine;
        private View iconBackground;
        private ImageView alertIcon;
        private TextView alertTitle;
        private TextView alertDescription;
        private TextView alertTimestamp;
        private TextView badgeNew;
        private Button alertActionButton;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            accentLine = itemView.findViewById(R.id.accentLine);
            iconBackground = itemView.findViewById(R.id.iconBackground);
            alertIcon = itemView.findViewById(R.id.alertIcon);
            alertTitle = itemView.findViewById(R.id.alertTitle);
            alertDescription = itemView.findViewById(R.id.alertDescription);
            alertTimestamp = itemView.findViewById(R.id.alertTimestamp);
            badgeNew = itemView.findViewById(R.id.badgeNew);
            alertActionButton = itemView.findViewById(R.id.alertActionButton);
        }

        public void bind(Notification notification) {
            // Set title
            alertTitle.setText(notification.getTitle());
            
            // Set description
            alertDescription.setText(notification.getMessage());
            
            // Set timestamp
            String timeAgo = getTimeAgo(notification.getTimestamp());
            alertTimestamp.setText(timeAgo);
            
            // Show/hide NEW badge
            if (!notification.isRead() && isRecent(notification.getTimestamp())) {
                badgeNew.setVisibility(View.VISIBLE);
            } else {
                badgeNew.setVisibility(View.GONE);
            }
            
            // Set icon based on notification type
            setAlertIcon(notification);
            
            // Set accent line for important alerts (Reminders/Confirmations)
            if (notification.getTitle().contains("confirmed") || 
                notification.getTitle().contains("Reminder")) {
                accentLine.setVisibility(View.VISIBLE);
            } else {
                accentLine.setVisibility(View.GONE);
            }
            
            // Show/hide action button (Check-in for upcoming appointments)
            if (notification.getTitle().contains("Appointment") && 
                notification.getTitle().contains("1 hour")) {
                alertActionButton.setVisibility(View.VISIBLE);
                alertActionButton.setOnClickListener(v -> {
                    // Handle check-in action (e.g., navigate to video call or appointment details)
                    // For now, just show a toast
                    android.widget.Toast.makeText(context, "Checking in...", android.widget.Toast.LENGTH_SHORT).show();
                });
            } else {
                alertActionButton.setVisibility(View.GONE);
            }
            
            // Mark as read when clicked
            itemView.setOnClickListener(v -> {
                if (!notification.isRead()) {
                    markAsRead(notification);
                }
            });
        }

        private void setAlertIcon(Notification notification) {
            String title = notification.getTitle().toLowerCase();
            
            if (title.contains("appointment") || title.contains("reminder")) {
                alertIcon.setImageResource(android.R.drawable.ic_menu_my_calendar);
                iconBackground.setBackgroundResource(R.drawable.bg_alert_icon);
            } else if (title.contains("confirmed") || title.contains("update")) {
                alertIcon.setImageResource(android.R.drawable.ic_menu_info_details);
                // You can create bg_alert_icon_confirmed.xml similar to bg_alert_icon but with green color
                iconBackground.setBackgroundResource(R.drawable.bg_alert_icon); 
            } else if (title.contains("new feature")) {
                alertIcon.setImageResource(android.R.drawable.ic_dialog_info);
                iconBackground.setBackgroundResource(R.drawable.bg_alert_icon);
            } else {
                alertIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                iconBackground.setBackgroundResource(R.drawable.bg_alert_icon);
            }
        }

        private void markAsRead(Notification notification) {
            db.collection("notifications")
                    .document(notification.getNotificationId())
                    .update("read", true)
                    .addOnSuccessListener(aVoid -> {
                        notification.setRead(true);
                        notifyItemChanged(getAdapterPosition());
                    })
                    .addOnFailureListener(e -> {
                        // Handle error silently or log it
                    });
        }

        private String getTimeAgo(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            if (diff < 60000) { // Less than a minute
                return "Just now";
            } else if (diff < 3600000) { // Less than an hour
                long minutes = diff / 60000;
                return minutes + " min ago";
            } else if (diff < 86400000) { // Less than 24 hours
                long hours = diff / 3600000;
                return hours + " hr ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            }
        }

        private boolean isRecent(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;
            return diff < 86400000; // Less than 24 hours
        }
    }
}