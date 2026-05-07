package com.example.medibook.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;
import com.example.medibook.activities.common.PortalSelectionActivity;
import com.example.medibook.repositories.AuthRepository;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private AuthRepository authRepository;
    private String currentAdminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize repositories
        authRepository = new AuthRepository();
        currentAdminId = FirebaseAuth.getInstance().getCurrentUser().getUid();

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
                performLogout();
            }
        });
    }

    private void performLogout() {
        authRepository.signOutWithTokenCleanup(currentAdminId, new AuthRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AdminDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AdminDashboardActivity.this, PortalSelectionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                // Still navigate even if cleanup failed
                Intent intent = new Intent(AdminDashboardActivity.this, PortalSelectionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
