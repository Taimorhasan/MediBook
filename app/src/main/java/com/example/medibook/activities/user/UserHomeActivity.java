package com.example.medibook.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;

public class UserHomeActivity extends AppCompatActivity {

    private Button btnViewDetails;
    private Button btnBookVisit;
    private LinearLayout navHome;
    private LinearLayout navBookings;
    private LinearLayout navAlerts;
    private LinearLayout navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        // Initialize Buttons
        btnViewDetails = findViewById(R.id.btn_view_details);
        btnBookVisit = findViewById(R.id.btn_book_visit);
        
        // Initialize Bottom Nav Items
        navHome = findViewById(R.id.nav_home_layout);
        navBookings = findViewById(R.id.nav_bookings_layout);
        navAlerts = findViewById(R.id.nav_alerts_layout);
        navProfile = findViewById(R.id.nav_profile_layout);

        // 1. Home is current page - maybe refresh
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Toast.makeText(this, "Refreshed home", Toast.LENGTH_SHORT).show();
            });
        }

        // 2. Setup Bookings Navigation
        if (navBookings != null) {
            navBookings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to Appointments Activity
                    Intent intent = new Intent(UserHomeActivity.this, BookingsListActivity.class);
                    startActivity(intent);
                    // Add smooth slide animation: new screen slides in from right, current slides out to left
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        }

        // 3. Setup Alerts Navigation
        if (navAlerts != null) {
            navAlerts.setOnClickListener(v -> {
                Intent intent = new Intent(UserHomeActivity.this, AlertsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        // 4. Setup Profile Navigation
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(UserHomeActivity.this, PatientProfileViewActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        // 2. Make "View Details" functional
        btnViewDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(UserHomeActivity.this, "Navigating to Appointment Details", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(UserHomeActivity.this, AppointmentDetailActivity.class);
                // startActivity(intent);
            }
        });

        // 3. Make "Book Visit" functional
        btnBookVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Doctor List Activity
                Intent intent = new Intent(UserHomeActivity.this, DoctorListActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }
}