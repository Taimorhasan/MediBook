package com.example.medibook.activities.user;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.adapters.DoctorAdapter;
import com.example.medibook.models.Doctor;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.DoctorRepository;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.models.User;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.List;

public class UserHomeActivity extends AppCompatActivity implements DoctorAdapter.OnDoctorClickListener {

    private RecyclerView doctorsRecyclerView;
    private DoctorAdapter doctorAdapter;
    private List<Doctor> doctorList;
    private DoctorRepository doctorRepository;
    private AuthRepository authRepository;
    private UserRepository userRepository;
    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        // Initialize repositories
        doctorRepository = new DoctorRepository();
        authRepository = new AuthRepository();
        userRepository = new UserRepository();

        // Initialize views
        doctorsRecyclerView = findViewById(R.id.doctors_recycler_view);
        welcomeTextView = findViewById(R.id.user_name);
        doctorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        doctorList = new ArrayList<>();
        doctorAdapter = new DoctorAdapter(doctorList, this);
        doctorsRecyclerView.setAdapter(doctorAdapter);

        // Load user data
        loadUserData();

        // Load doctors
        loadDoctors();
    }

    private void loadUserData() {
        String userId = authRepository.getCurrentUser().getUid();
        userRepository.getUser(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                welcomeTextView.setText("Welcome Back, " + user.getName());
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(UserHomeActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDoctors() {
        doctorRepository.getAllDoctors(new DoctorRepository.DoctorsCallback() {
            @Override
            public void onSuccess(List<Doctor> doctors) {
                doctorList.clear();
                doctorList.addAll(doctors);
                doctorAdapter.notifyDataSetChanged();

                // If no doctors in database, show sample data
                if (doctors.isEmpty()) {
                    addSampleDoctors();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(UserHomeActivity.this, "Failed to load doctors: " + error, Toast.LENGTH_SHORT).show();
                // Show sample data on failure
                addSampleDoctors();
            }
        });
    }

    private void addSampleDoctors() {
        doctorList.clear();
        doctorList.add(new Doctor("Dr. Sarah Wilson", "Cardiologist", "4.8", android.R.drawable.ic_menu_myplaces));
        doctorList.add(new Doctor("Dr. James Miller", "Dentist", "4.5", android.R.drawable.ic_menu_myplaces));
        doctorList.add(new Doctor("Dr. Emily Brown", "General Physician", "4.9", android.R.drawable.ic_menu_myplaces));
        doctorList.add(new Doctor("Dr. Michael Chen", "Orthopedic", "4.7", android.R.drawable.ic_menu_myplaces));
        doctorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDoctorClick(Doctor doctor) {
        Intent intent = new Intent(this, DoctorProfileActivity.class);
        // Pass doctor data via intent or save to viewmodel
        intent.putExtra("doctor_id", doctor.getDoctorId());
        intent.putExtra("doctor_name", doctor.getName());
        intent.putExtra("doctor_specialty", doctor.getSpecialty());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            performLogout();
            return true;
        } else if (item.getItemId() == R.id.menu_appointments) {
            startActivity(new Intent(this, AppointmentsActivity.class));
            return true;
        } else if (item.getItemId() == R.id.menu_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performLogout() {
        String userId = authRepository.getCurrentUser().getUid();
        authRepository.signOutWithTokenCleanup(userId, new AuthRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(UserHomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(UserHomeActivity.this, com.example.medibook.activities.common.PortalSelectionActivity.class));
                finish();
            }

            @Override
            public void onFailure(String error) {
                // Still navigate even if cleanup failed
                startActivity(new Intent(UserHomeActivity.this, com.example.medibook.activities.common.PortalSelectionActivity.class));
                finish();
            }
        });
    }
}
