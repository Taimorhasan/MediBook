package com.example.medibook.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medibook.R;
import com.example.medibook.adapters.AlertsAdapter;
import com.example.medibook.models.Notification;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.NotificationRepository;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class AlertsActivity extends AppCompatActivity {

    private RecyclerView alertsRecyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private AlertsAdapter alertsAdapter;
    private List<Notification> notificationsList;
    
    private AuthRepository authRepository;
    private NotificationRepository notificationRepository;
    
    // Navigation elements
    private LinearLayout navHomeLayout, navBookingsLayout, navAlertsLayout, navProfileLayout;
    private ImageView profileIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);

        // Initialize repositories
        authRepository = new AuthRepository();
        notificationRepository = new NotificationRepository();

        // Initialize views
        initializeViews();
        setupRecyclerView();
        setupNavigation();
        loadNotifications();
    }

    private void initializeViews() {
        alertsRecyclerView = findViewById(R.id.alertsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        
        // Navigation
        navHomeLayout = findViewById(R.id.nav_home_layout);
        navBookingsLayout = findViewById(R.id.nav_bookings_layout);
        navAlertsLayout = findViewById(R.id.nav_alerts_layout);
        navProfileLayout = findViewById(R.id.nav_profile_layout);
        profileIcon = findViewById(R.id.profileIcon);
    }

    private void setupRecyclerView() {
        notificationsList = new ArrayList<>();
        alertsAdapter = new AlertsAdapter(notificationsList, this);
        alertsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        alertsRecyclerView.setAdapter(alertsAdapter);
    }

    private void setupNavigation() {
        // Home navigation
        if (navHomeLayout != null) {
            navHomeLayout.setOnClickListener(v -> {
                Intent intent = new Intent(AlertsActivity.this, UserHomeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            });
        }

        // Bookings navigation
        if (navBookingsLayout != null) {
            navBookingsLayout.setOnClickListener(v -> {
                Intent intent = new Intent(AlertsActivity.this, DoctorListActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            });
        }

        // Alerts is current page - maybe refresh
        if (navAlertsLayout != null) {
            navAlertsLayout.setOnClickListener(v -> {
                loadNotifications();
                Toast.makeText(this, "Refreshed alerts", Toast.LENGTH_SHORT).show();
            });
        }

        // Profile navigation
        if (navProfileLayout != null) {
            navProfileLayout.setOnClickListener(v -> {
                Intent intent = new Intent(AlertsActivity.this, PatientProfileViewActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        // Profile icon in app bar
        if (profileIcon != null) {
            profileIcon.setOnClickListener(v -> {
                Intent intent = new Intent(AlertsActivity.this, PatientProfileViewActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        notificationsList.clear();

        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            progressBar.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            return;
        }

        String userId = currentUser.getUid();

        notificationRepository.getUserNotifications(userId, new NotificationRepository.NotificationsCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    notificationsList.clear();
                    notificationsList.addAll(notifications);

                    if (notificationsList.isEmpty()) {
                        notificationsList.addAll(getDefaultAlerts(userId));
                    }

                    alertsAdapter.notifyDataSetChanged();
                    alertsRecyclerView.setVisibility(View.VISIBLE);
                    emptyStateLayout.setVisibility(View.GONE);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    notificationsList.clear();
                    notificationsList.addAll(getDefaultAlerts(userId));
                    alertsAdapter.notifyDataSetChanged();
                    alertsRecyclerView.setVisibility(View.VISIBLE);
                    emptyStateLayout.setVisibility(View.GONE);
                    Toast.makeText(AlertsActivity.this,
                            "Showing local alerts: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private List<Notification> getDefaultAlerts(String userId) {
        List<Notification> defaults = new ArrayList<>();

        Notification booking = new Notification(
                "local-alert-book-doctor",
                userId,
                "Book your next appointment",
                "Choose a doctor by specialty and request an appointment from the Bookings tab.",
                "doctor_booking",
                false
        );
        booking.setTimestamp(System.currentTimeMillis());

        Notification reminder = new Notification(
                "local-alert-profile",
                userId,
                "Keep your profile updated",
                "A complete patient profile helps doctors review your appointment request faster.",
                "profile",
                true
        );
        reminder.setTimestamp(System.currentTimeMillis() - 3600000);

        defaults.add(booking);
        defaults.add(reminder);
        return defaults;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }
}
