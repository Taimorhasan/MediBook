package com.example.medibook.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.example.medibook.R;
import com.example.medibook.activities.common.BaseActivity;
import com.example.medibook.activities.auth.UnifiedLoginActivity;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.DoctorRepository;
import com.example.medibook.repositories.AppointmentRepository;
import com.example.medibook.models.Doctor;
import com.example.medibook.models.Appointment;
import android.widget.TextView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;

public class AdminDashboardActivity extends BaseActivity {

    private AuthRepository authRepository;
    private DoctorRepository doctorRepository;
    private AppointmentRepository appointmentRepository;
    private String currentAdminId;
    private TextView doctorsCountText, appointmentsCountText;
    private View impersonationBanner;
    private TextView impersonationText;
    private Button stopImpersonationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        
        // Check if the user has the admin role
        if (!checkRoleAndRedirect("admin")) {
            return;
        }

        // Initialize repositories
        authRepository = new AuthRepository();
        doctorRepository = new DoctorRepository();
        appointmentRepository = new AppointmentRepository();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            android.util.Log.e("AdminDashboard", "Current user is null! Redirecting to portal.");
            navigateToLogin();
            return;
        }
        currentAdminId = user.getUid();

        doctorsCountText = findViewById(R.id.total_doctors_count);
        appointmentsCountText = findViewById(R.id.total_appointments_count);
        
        impersonationBanner = findViewById(R.id.impersonation_banner);
        impersonationText = findViewById(R.id.impersonation_text);
        stopImpersonationBtn = findViewById(R.id.stop_impersonation_btn);

        setupImpersonationUI();

        MaterialCardView manageDoctorsCard = findViewById(R.id.manage_doctors_card);
        MaterialCardView manageAppointmentsCard = findViewById(R.id.manage_appointments_card);
        MaterialCardView manageUsersCard = findViewById(R.id.manage_users_card);
        MaterialCardView manageHospitalsCard = findViewById(R.id.manage_hospitals_card);
        View logoutButton = findViewById(R.id.logout_button);

        fetchStats();

        manageDoctorsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, ManageDoctorsActivity.class);
                startActivity(intent);
            }
        });

        manageAppointmentsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, ManageAppointmentsActivity.class);
                startActivity(intent);
            }
        });

        manageUsersCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, ManageUsersActivity.class);
                startActivity(intent);
            }
        });

        manageHospitalsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, ManageHospitalsActivity.class);
                startActivity(intent);
            }
        });

        MaterialCardView manageRolesCard = findViewById(R.id.manage_roles_card);
        manageRolesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, ManageRolesActivity.class);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogout();
            }
        });
    }

    private void setupImpersonationUI() {
        if (sessionManager.isImpersonating()) {
            impersonationBanner.setVisibility(View.VISIBLE);
            String role = sessionManager.getUserRole();
            impersonationText.setText("Acting as: " + role.toUpperCase());
            
            stopImpersonationBtn.setOnClickListener(v -> {
                sessionManager.stopImpersonation();
                Toast.makeText(this, "Stopped acting as user", Toast.LENGTH_SHORT).show();
                
                // Refresh activity to update UI
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        } else {
            impersonationBanner.setVisibility(View.GONE);
        }
    }

    private void fetchStats() {
        doctorRepository.getAllDoctorsAdmin(new DoctorRepository.DoctorsCallback() {
            @Override
            public void onSuccess(List<Doctor> doctors) {
                if (doctorsCountText != null) {
                    doctorsCountText.setText(String.valueOf(doctors.size()));
                }
            }

            @Override
            public void onFailure(String error) {
                // Ignore silently or log
            }
        });

        appointmentRepository.getAllAppointments(new AppointmentRepository.AppointmentsCallback() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                if (appointmentsCountText != null) {
                    appointmentsCountText.setText(String.valueOf(appointments.size()));
                }
            }

            @Override
            public void onFailure(String error) {
                // Ignore silently or log
            }
        });
    }

    private void performLogout() {
        authRepository.signOutWithTokenCleanup(currentAdminId, new AuthRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AdminDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                navigateToLogin();
            }

            @Override
            public void onFailure(String error) {
                // Still navigate even if cleanup failed
                navigateToLogin();
            }
        });
    }
}
