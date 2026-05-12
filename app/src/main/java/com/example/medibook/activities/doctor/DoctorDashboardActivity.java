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
import com.example.medibook.repositories.DoctorDetailRepository;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.models.Doctor;
import com.example.medibook.models.Appointment;
import com.example.medibook.adapters.AppointmentAdapter;
import com.example.medibook.activities.common.PortalSelectionActivity;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class DoctorDashboardActivity extends com.example.medibook.activities.common.BaseActivity implements AppointmentAdapter.OnAppointmentClickListener {

    private TextView welcomeTextView, doctorNameTextView, specialtyTextView;
    private Button logoutButton, editProfileButton;
    private RecyclerView appointmentsRecyclerView;
    private AppointmentAdapter appointmentAdapter;
    
    private DoctorDetailRepository doctorRepository;
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
        appointmentAdapter = new AppointmentAdapter(new ArrayList<>(), this);
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
                appointmentAdapter = new AppointmentAdapter(appointments, DoctorDashboardActivity.this);
                appointmentsRecyclerView.setAdapter(appointmentAdapter);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(DoctorDashboardActivity.this, "Failed to load appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogout() {
        authRepository.signOutWithTokenCleanup(currentDoctorId, new AuthRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(DoctorDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DoctorDashboardActivity.this, PortalSelectionActivity.class));
                finish();
            }

            @Override
            public void onFailure(String error) {
                // Still navigate even if cleanup failed
                startActivity(new Intent(DoctorDashboardActivity.this, PortalSelectionActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onAppointmentClick(Appointment appointment) {
        Toast.makeText(this, "Appointment: " + appointment.getStatus(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelAppointment(Appointment appointment) {
        Toast.makeText(this, "Cancel appointment feature coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDoctorProfile();
        loadAppointments();
    }
}
