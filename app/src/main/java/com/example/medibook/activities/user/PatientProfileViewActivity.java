package com.example.medibook.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.medibook.R;
import com.example.medibook.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

public class PatientProfileViewActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView nameTextView, emailTextView, phoneTextView, ageTextView;
    private TextView genderTextView, bloodGroupTextView, bioTextView;
    private Button editButton, logoutButton;
    
    private UserRepository userRepository;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile_view);

        userRepository = new UserRepository();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

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
        userRepository.getUser(currentUserId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(com.example.medibook.models.User user) {
                if (user != null) {
                    nameTextView.setText(user.getName() != null ? user.getName() : "N/A");
                    emailTextView.setText(user.getEmail() != null ? user.getEmail() : "N/A");
                    phoneTextView.setText(user.getPhone() != null ? user.getPhone() : "N/A");
                    ageTextView.setText(user.getAge() != null ? user.getAge() : "N/A");
                    genderTextView.setText(user.getGender() != null ? user.getGender() : "Not Specified");
                    bloodGroupTextView.setText(user.getBloodGroup() != null ? user.getBloodGroup() : "Not Specified");
                    bioTextView.setText(user.getBio() != null ? user.getBio() : "No bio provided");

                    if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                        Glide.with(PatientProfileViewActivity.this)
                            .load(user.getProfileImage())
                            .circleCrop()
                            .into(profileImageView);
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(PatientProfileViewActivity.this, "Failed to load profile: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(PatientProfileViewActivity.this, com.example.medibook.activities.common.PortalSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile data when returning from edit
        loadProfileData();
    }
}
