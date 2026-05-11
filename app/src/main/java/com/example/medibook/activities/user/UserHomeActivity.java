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
    private LinearLayout navBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        // Initialize Buttons
        btnViewDetails = findViewById(R.id.btn_view_details);
        btnBookVisit = findViewById(R.id.btn_book_visit);
        
        // Initialize Bottom Nav Item
        navBookings = findViewById(R.id.nav_bookings_layout);

        // 1. Setup Bottom Navigation Click Listener
        if (navBookings != null) {
            navBookings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to Appointments Activity
                    Intent intent = new Intent(UserHomeActivity.this, BookingsListActivity.class);
                    startActivity(intent);
                }
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
                Toast.makeText(UserHomeActivity.this, "Navigating to Doctor Profile", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(UserHomeActivity.this, DoctorProfileActivity.class);
                // startActivity(intent);
            }
        });
    }
}