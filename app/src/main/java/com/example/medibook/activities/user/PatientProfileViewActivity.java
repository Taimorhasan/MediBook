package com.example.medibook.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.medibook.R;
import com.example.medibook.repositories.PatientRepository;
import com.example.medibook.services.CloudinaryService;
import com.google.firebase.auth.FirebaseAuth;
import com.example.medibook.activities.auth.UnifiedLoginActivity;

public class PatientProfileViewActivity extends com.example.medibook.activities.common.BaseActivity {

    private ImageView profileImageView;
    private TextView nameTextView, emailTextView, phoneTextView, ageTextView;
    private TextView genderTextView, bloodGroupTextView, bioTextView;
    private Button editButton, logoutButton;
    private ProgressBar loadingProgress;
    private ScrollView contentScrollView;
    
    private PatientRepository patientRepository;
    private CloudinaryService cloudinaryService;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile_view);

        if (mAuth.getCurrentUser() == null) {
            navigateToLogin();
            return;
        }

        patientRepository = new PatientRepository();
        cloudinaryService = new CloudinaryService();
        currentUserId = mAuth.getCurrentUser().getUid();

        // Initialize views
        profileImageView = findViewById(R.id.profile_image);
        nameTextView = findViewById(R.id.name_text_view);
        emailTextView = findViewById(R.id.email_text_view);
        phoneTextView = findViewById(R.id.phone_text_view);
        ageTextView = findViewById(R.id.age_text_view);
        genderTextView = findViewById(R.id.gender_text_view);
        bloodGroupTextView = findViewById(R.id.blood_group_text_view);
        bioTextView = findViewById(R.id.bio_text_view);
        editButton = findViewById(R.id.edit_button);
        logoutButton = findViewById(R.id.logout_button);
        loadingProgress = findViewById(R.id.loading_progress);
        contentScrollView = findViewById(R.id.content_scroll_view);

        // Initially hide content and show loading
        contentScrollView.setVisibility(View.GONE);
        loadingProgress.setVisibility(View.VISIBLE);

        // Load profile data
        loadProfileData();

        // Edit button
        editButton.setOnClickListener(v -> {
            startActivity(new Intent(PatientProfileViewActivity.this, EditPatientProfileActivity.class));
        });

        // Logout button
        logoutButton.setOnClickListener(v -> logout());
    }

    private void loadProfileData() {
        // Show loading
        loadingProgress.setVisibility(View.VISIBLE);
        contentScrollView.setVisibility(View.GONE);

        patientRepository.getPatient(currentUserId, new PatientRepository.PatientCallback() {
            @Override
            public void onSuccess(com.example.medibook.models.User patient) {
                runOnUiThread(() -> {
                    // Hide loading and show content
                    loadingProgress.setVisibility(View.GONE);
                    contentScrollView.setVisibility(View.VISIBLE);

                    if (patient != null) {
                        nameTextView.setText(patient.getName() != null ? patient.getName() : "N/A");
                        emailTextView.setText(patient.getEmail() != null ? patient.getEmail() : "N/A");
                        phoneTextView.setText(patient.getPhone() != null ? patient.getPhone() : "N/A");
                        ageTextView.setText(patient.getAge() != null ? patient.getAge() : "N/A");
                        genderTextView.setText(patient.getGender() != null ? patient.getGender() : "Not Specified");
                        bloodGroupTextView.setText(patient.getBloodGroup() != null ? patient.getBloodGroup() : "Not Specified");
                        bioTextView.setText(patient.getBio() != null ? patient.getBio() : "No bio provided");

                        if (patient.getProfileImage() != null && !patient.getProfileImage().isEmpty()) {
                            String optimizedUrl = cloudinaryService.getOptimizedImageUrl(patient.getProfileImage(), 300, 300);
                            Glide.with(PatientProfileViewActivity.this)
                                .load(optimizedUrl)
                                .circleCrop()
                                .into(profileImageView);
                        }
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    // Hide loading and show error
                    loadingProgress.setVisibility(View.GONE);
                    contentScrollView.setVisibility(View.VISIBLE);
                    Toast.makeText(PatientProfileViewActivity.this, "Failed to load profile: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        navigateToLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile data when returning from edit
        loadProfileData();
    }
}
