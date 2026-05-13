package com.example.medibook.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.example.medibook.R;
import com.example.medibook.models.Doctor;
import com.example.medibook.repositories.DoctorRepository;

public class DoctorProfileActivity extends AppCompatActivity {

    private TextView doctorNameTextView;
    private TextView doctorSpecialtyTextView;
    private TextView doctorExperienceTextView;
    private TextView doctorRatingTextView;
    private TextView doctorBioTextView;
    private ImageView doctorImageView;
    private Button bookButton;
    private ProgressBar loadingProgress;
    private View contentLayout;

    private DoctorRepository doctorRepository;
    private String doctorId;
    private String doctorName;
    private String doctorSpecialty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);

        // Initialize repository
        doctorRepository = new DoctorRepository();

        // Get doctor info from intent
        doctorId = getIntent().getStringExtra("doctorId");
        doctorName = getIntent().getStringExtra("doctorName");
        doctorSpecialty = getIntent().getStringExtra("doctorSpecialty");

        if (doctorId == null) {
            Toast.makeText(this, "Doctor information not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(doctorName != null ? doctorName : "Doctor Profile");
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        initializeViews();

        // Setup book appointment button
        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DoctorProfileActivity.this, BookAppointmentActivity.class);
                intent.putExtra("doctorId", doctorId);
                intent.putExtra("doctorName", doctorName);
                intent.putExtra("doctorSpecialty", doctorSpecialty);
                startActivity(intent);
            }
        });

        // Load doctor profile
        loadDoctorProfile();
    }

    private void initializeViews() {
        doctorNameTextView = findViewById(R.id.doctor_name_tv);
        doctorSpecialtyTextView = findViewById(R.id.doctor_specialty_tv);
        doctorExperienceTextView = findViewById(R.id.doctor_experience_tv);
        doctorRatingTextView = findViewById(R.id.doctor_rating_tv);
        doctorBioTextView = findViewById(R.id.doctor_bio_tv);
        doctorImageView = findViewById(R.id.doctor_image_view);
        bookButton = findViewById(R.id.profile_book_button);
        loadingProgress = findViewById(R.id.loading_progress);
        contentLayout = findViewById(R.id.content_layout);

        // Initially hide content and show loading
        contentLayout.setVisibility(View.GONE);
        loadingProgress.setVisibility(View.VISIBLE);
    }

    private void loadDoctorProfile() {
        doctorRepository.getDoctor(doctorId, new DoctorRepository.DoctorCallback() {
            @Override
            public void onSuccess(Doctor doctor) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    contentLayout.setVisibility(View.VISIBLE);
                    populateDoctorData(doctor);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    Toast.makeText(DoctorProfileActivity.this,
                        "Failed to load doctor profile: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void populateDoctorData(Doctor doctor) {
        // Set doctor name
        if (doctor.getName() != null && !doctor.getName().trim().isEmpty()) {
            doctorNameTextView.setText(doctor.getName());
        }

        // Set specialty
        if (doctor.getSpecialty() != null && !doctor.getSpecialty().trim().isEmpty()) {
            doctorSpecialtyTextView.setText(doctor.getSpecialty());
        } else {
            doctorSpecialtyTextView.setText("Specialty not specified");
        }

        // Set experience
        if (doctor.getExperience() != null && !doctor.getExperience().trim().isEmpty()) {
            doctorExperienceTextView.setText(doctor.getExperience());
        } else {
            doctorExperienceTextView.setText("Experience not specified");
        }

        // Set rating
        if (doctor.getRating() != null && !doctor.getRating().trim().isEmpty()) {
            doctorRatingTextView.setText(doctor.getRating() + " ⭐");
        } else {
            doctorRatingTextView.setText("Not rated yet");
        }

        // Set bio
        if (doctor.getBio() != null && !doctor.getBio().trim().isEmpty()) {
            doctorBioTextView.setText(doctor.getBio());
        } else {
            doctorBioTextView.setText("No bio available");
        }

        // Load profile image
        if (doctor.getProfileImage() != null && !doctor.getProfileImage().trim().isEmpty()) {
            Glide.with(this)
                .load(doctor.getProfileImage())
                .placeholder(R.drawable.ic_medical_kit)
                .error(R.drawable.ic_medical_kit)
                .circleCrop()
                .into(doctorImageView);
        } else {
            doctorImageView.setImageResource(R.drawable.ic_medical_kit);
        }

        // Update toolbar title with real name
        if (getSupportActionBar() != null && doctor.getName() != null) {
            getSupportActionBar().setTitle(doctor.getName());
        }

        // Store updated doctor info for booking
        doctorName = doctor.getName();
        doctorSpecialty = doctor.getSpecialty();
    }
}
