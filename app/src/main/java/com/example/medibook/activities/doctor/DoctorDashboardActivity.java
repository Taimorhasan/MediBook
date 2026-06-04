package com.example.medibook.activities.doctor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.AppointmentRepository;
import com.example.medibook.repositories.DoctorDetailRepository;
import com.example.medibook.repositories.NotificationRepository;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.models.Doctor;
import com.example.medibook.models.Appointment;
import com.example.medibook.adapters.AppointmentAdapter;
import com.example.medibook.activities.auth.UnifiedLoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class DoctorDashboardActivity extends com.example.medibook.activities.common.BaseActivity implements AppointmentAdapter.OnAppointmentClickListener {

    private TextView welcomeTextView, doctorNameTextView, specialtyTextView;
    private Button logoutButton, editProfileButton;
    private RecyclerView appointmentsRecyclerView;
    private AppointmentAdapter appointmentAdapter;
    
    private DoctorDetailRepository doctorRepository;
    private AppointmentRepository appointmentRepository;
    private NotificationRepository notificationRepository;
    private UserRepository userRepository;
    private AuthRepository authRepository;
    private String currentDoctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        // Ensure only doctors can access
        if (!checkRoleAndRedirect("doctor")) {
            return;
        }

        // Initialize repositories
        doctorRepository = new DoctorDetailRepository();
        appointmentRepository = new AppointmentRepository();
        notificationRepository = new NotificationRepository();
        userRepository = new UserRepository();
        authRepository = new AuthRepository();

        // Get current user ID
        currentDoctorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize views
        welcomeTextView = findViewById(R.id.welcome_text);
        doctorNameTextView = findViewById(R.id.doctor_name);
        specialtyTextView = findViewById(R.id.doctor_specialty);
        logoutButton = findViewById(R.id.logout_button);
        editProfileButton = findViewById(R.id.edit_profile_button);
        appointmentsRecyclerView = findViewById(R.id.appointments_recycler);

        // Setup RecyclerView with empty list first
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointmentAdapter = new AppointmentAdapter(new ArrayList<>(), this, true, false);
        appointmentsRecyclerView.setAdapter(appointmentAdapter);

        // Load doctor profile
        loadDoctorProfile();
        loadAppointments();

        // Logout button
        logoutButton.setOnClickListener(v -> performLogout());

        // Edit profile button
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditDoctorProfileActivity.class);
            startActivity(intent);
        });
    }

    private void loadDoctorProfile() {
        doctorRepository.getDoctorProfile(currentDoctorId, new DoctorDetailRepository.DoctorCallback() {
            @Override
            public void onSuccess(Doctor doctor) {
                doctorNameTextView.setText(doctor.getName());
                specialtyTextView.setText(doctor.getSpecialty());
                welcomeTextView.setText("Welcome, Dr. " + doctor.getName());
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(DoctorDashboardActivity.this, "Failed to load doctor profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAppointments() {
        doctorRepository.getDoctorAppointments(currentDoctorId, new DoctorDetailRepository.AppointmentListCallback() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                runOnUiThread(() -> appointmentAdapter.updateList(appointments));
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(DoctorDashboardActivity.this, "Failed to load appointments", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void performLogout() {
        authRepository.signOutWithTokenCleanup(currentDoctorId, new AuthRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(DoctorDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                navigateToLogin();
            }

            @Override
            public void onFailure(String error) {
                // Still navigate even if cleanup failed
                navigateToLogin();
            }
        });
    }

    @Override
    public void onAppointmentClick(Appointment appointment) {
        Toast.makeText(this, "Appointment: " + appointment.getStatus(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelAppointment(Appointment appointment) {
        updateAppointmentStatus(appointment, "cancelled");
    }

    @Override
    public void onConfirmAppointment(Appointment appointment) {
        updateAppointmentStatus(appointment, "confirmed");
    }

    @Override
    public void onCompleteAppointment(Appointment appointment) {
        updateAppointmentStatus(appointment, "completed");
    }

    private void updateAppointmentStatus(Appointment appointment, String status) {
        if (appointment == null || appointment.getAppointmentId() == null || appointment.getAppointmentId().trim().isEmpty()) {
            Toast.makeText(this, "Appointment information is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        appointmentRepository.updateAppointmentStatus(appointment.getAppointmentId(), status, new AppointmentRepository.AppointmentCallback() {
            @Override
            public void onSuccess(Appointment updatedAppointment) {
                sendStatusNotification(updatedAppointment, status);
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(DoctorDashboardActivity.this,
                        "Failed to update appointment: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void sendStatusNotification(Appointment appointment, String status) {
        if (appointment == null || appointment.getPatientId() == null || appointment.getPatientId().trim().isEmpty()) {
            runOnUiThread(() -> {
                Toast.makeText(DoctorDashboardActivity.this,
                        "Appointment " + status, Toast.LENGTH_SHORT).show();
                loadAppointments();
            });
            return;
        }

        String title = "Appointment " + capitalize(status);
        String message = "Your appointment with Dr. " + safeDoctorName()
                + " on " + appointment.getDate()
                + " at " + appointment.getTime()
                + " has been " + status + ".";

        notificationRepository.sendNotification(
                appointment.getPatientId(),
                title,
                message,
                "appointment_" + status,
                new NotificationRepository.NotificationCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(DoctorDashboardActivity.this,
                                    "Appointment " + status, Toast.LENGTH_SHORT).show();
                            loadAppointments();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(DoctorDashboardActivity.this,
                                    "Appointment updated, alert failed: " + error, Toast.LENGTH_SHORT).show();
                            loadAppointments();
                        });
                    }
                }
        );
    }

    private String safeDoctorName() {
        CharSequence name = doctorNameTextView != null ? doctorNameTextView.getText() : "";
        return name != null && name.length() > 0 ? name.toString() : "your doctor";
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) return "";
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDoctorProfile();
        loadAppointments();
    }
}
