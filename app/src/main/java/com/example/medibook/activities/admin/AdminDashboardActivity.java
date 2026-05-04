package com.example.medibook.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;
import com.example.medibook.activities.common.PortalSelectionActivity;
import com.google.android.material.card.MaterialCardView;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        MaterialCardView manageDoctorsCard = findViewById(R.id.manage_doctors_card);
        MaterialCardView manageAppointmentsCard = findViewById(R.id.manage_appointments_card);
        Button logoutButton = findViewById(R.id.logout_button);

        manageDoctorsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // To be implemented: Intent to ManageDoctorsActivity
            }
        });

        manageAppointmentsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // To be implemented: Intent to ManageAppointmentsActivity
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, PortalSelectionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
