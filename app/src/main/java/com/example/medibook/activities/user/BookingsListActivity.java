package com.example.medibook.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;

public class BookingsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings_list);

        // Initialize Bottom Nav Items
        LinearLayout navHome = findViewById(R.id.nav_home_layout);
        LinearLayout navBookings = findViewById(R.id.nav_bookings_layout);
        LinearLayout navAlerts = findViewById(R.id.nav_alerts_layout);
        LinearLayout navProfile = findViewById(R.id.nav_profile_layout);

        // 1. Click Home to go back to Dashboard
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(BookingsListActivity.this, UserHomeActivity.class);
                startActivity(intent);
                finish(); // Close this screen so back button doesn't loop
            });
        }

        // 2. Bookings is already active, maybe refresh or do nothing
        if (navBookings != null) {
            navBookings.setOnClickListener(v -> {
                Toast.makeText(this, "You are already on the Bookings page", Toast.LENGTH_SHORT).show();
            });
        }

        // 3. Alerts Placeholder
        if (navAlerts != null) {
            navAlerts.setOnClickListener(v -> {
                Toast.makeText(this, "Alerts page coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // 4. Profile Placeholder
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Toast.makeText(this, "Profile page coming soon", Toast.LENGTH_SHORT).show();
            });
        }
    }
}