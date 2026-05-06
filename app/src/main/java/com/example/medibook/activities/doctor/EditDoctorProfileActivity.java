package com.example.medibook.activities.doctor;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;
import com.example.medibook.repositories.DoctorDetailRepository;
import com.example.medibook.models.Doctor;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class EditDoctorProfileActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, specialtyEditText, bioEditText, ratingEditText;
    private Button updateButton;
    private ProgressBar progressBar;
    private DoctorDetailRepository doctorRepository;
    private String doctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_doctor_profile);

        doctorRepository = new DoctorDetailRepository();
        doctorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize views
        nameEditText = findViewById(R.id.name_edit_text);
        specialtyEditText = findViewById(R.id.specialty_edit_text);
        bioEditText = findViewById(R.id.bio_edit_text);
        ratingEditText = findViewById(R.id.rating_edit_text);
        updateButton = findViewById(R.id.update_button);
        progressBar = findViewById(R.id.progress_bar);

        // Load doctor profile
        loadDoctorProfile();

        // Update button
        updateButton.setOnClickListener(v -> updateProfile());
    }

    private void loadDoctorProfile() {
        progressBar.setVisibility(View.VISIBLE);
        
        doctorRepository.getDoctorProfile(doctorId, new DoctorDetailRepository.DoctorCallback() {
            @Override
            public void onSuccess(Doctor doctor) {
                progressBar.setVisibility(View.GONE);
                nameEditText.setText(doctor.getName());
                specialtyEditText.setText(doctor.getSpecialty());
                bioEditText.setText(doctor.getBio());
                ratingEditText.setText(doctor.getRating());
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditDoctorProfileActivity.this, "Failed to load profile: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        String name = nameEditText.getText().toString().trim();
        String specialty = specialtyEditText.getText().toString().trim();
        String bio = bioEditText.getText().toString().trim();
        String rating = ratingEditText.getText().toString().trim();

        if (name.isEmpty() || specialty.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        updateButton.setEnabled(false);

        Doctor doctor = new Doctor(doctorId, name, specialty, "", "", "", "", rating, bio);
        
        doctorRepository.saveDoctorProfile(doctorId, doctor, new DoctorDetailRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                updateButton.setEnabled(true);
                Toast.makeText(EditDoctorProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                updateButton.setEnabled(true);
                Toast.makeText(EditDoctorProfileActivity.this, "Failed to update profile: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
