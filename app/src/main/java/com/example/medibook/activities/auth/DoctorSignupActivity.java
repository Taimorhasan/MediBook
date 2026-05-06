package com.example.medibook.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.DoctorDetailRepository;
import com.example.medibook.models.Doctor;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class DoctorSignupActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, emailEditText, phoneEditText, specialtyEditText, 
            experienceEditText, passwordEditText;
    private Button signupButton;
    private TextView loginRedirect;
    private ProgressBar progressBar;
    private AuthRepository authRepository;
    private DoctorDetailRepository doctorRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_signup);

        // Initialize repositories
        authRepository = new AuthRepository();
        doctorRepository = new DoctorDetailRepository();

        // Initialize views
        nameEditText = findViewById(R.id.name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        phoneEditText = findViewById(R.id.phone_edit_text);
        specialtyEditText = findViewById(R.id.specialty_edit_text);
        experienceEditText = findViewById(R.id.experience_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        signupButton = findViewById(R.id.signup_button);
        loginRedirect = findViewById(R.id.login_redirect);
        progressBar = findViewById(R.id.progress_bar);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();
                String specialty = specialtyEditText.getText().toString().trim();
                String experience = experienceEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Validation
                if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || 
                    specialty.isEmpty() || experience.isEmpty() || password.isEmpty()) {
                    Toast.makeText(DoctorSignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(DoctorSignupActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!email.contains("@")) {
                    Toast.makeText(DoctorSignupActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                    return;
                }

                performSignup(name, email, phone, specialty, experience, password);
            }
        });

        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to DoctorLoginActivity
                Intent intent = new Intent(DoctorSignupActivity.this, DoctorLoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void performSignup(String name, String email, String phone, String specialty, 
                              String experience, String password) {
        progressBar.setVisibility(View.VISIBLE);
        signupButton.setEnabled(false);

        // Sign up with role "doctor"
        authRepository.signUp(name, email, phone, password, "doctor", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Now save doctor-specific details
                Doctor doctor = new Doctor(user.getUid(), name, specialty, "", phone, email, 
                        experience, "0", "");

                doctorRepository.saveDoctorProfile(user.getUid(), doctor, new DoctorDetailRepository.VoidCallback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        signupButton.setEnabled(true);
                        Toast.makeText(DoctorSignupActivity.this, "Doctor Account Created Successfully!", Toast.LENGTH_LONG).show();
                        
                        // Navigate to doctor login or dashboard
                        Intent intent = new Intent(DoctorSignupActivity.this, DoctorLoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        progressBar.setVisibility(View.GONE);
                        signupButton.setEnabled(true);
                        Toast.makeText(DoctorSignupActivity.this, "Failed to create doctor profile: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                signupButton.setEnabled(true);
                Toast.makeText(DoctorSignupActivity.this, "Signup failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
