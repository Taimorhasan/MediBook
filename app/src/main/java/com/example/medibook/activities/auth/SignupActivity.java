package com.example.medibook.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;
import com.example.medibook.activities.user.UserLoginActivity;
import com.example.medibook.repositories.AuthRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, emailEditText, phoneEditText, passwordEditText;
    private TextInputLayout nameLayout, emailLayout, phoneLayout, passwordLayout;
    private MaterialButton signupButton, btnGoogleSignIn;
    private TextView loginRedirect;
    private ProgressBar progressBar;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        
        signupButton = findViewById(R.id.signupButton);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        loginRedirect = findViewById(R.id.loginRedirect);
        progressBar = findViewById(R.id.progressBar);
        
        authRepository = new AuthRepository();
    }

    private void setupClickListeners() {
        // ✅ Sign Up Button - WITH VALIDATIONS
        signupButton.setOnClickListener(v -> {
            if (validateInputs()) {
                performSignup();
            }
        });

        // ✅ Google Sign Up - Redirect to Login for Google flow
        btnGoogleSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, UserLoginActivity.class);
            startActivity(intent);
            finish();
        });

        // ✅ Already have account? Go to Login
        loginRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, UserLoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // ✅✅✅ COMPREHENSIVE VALIDATIONS ✅✅✅
    private boolean validateInputs() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        boolean isValid = true;

        // Clear previous errors
        nameLayout.setError(null);
        emailLayout.setError(null);
        phoneLayout.setError(null);
        passwordLayout.setError(null);

        // 🔹 NAME: Not empty, no numbers, min 3 chars
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Name is required");
            nameEditText.requestFocus();
            isValid = false;
        } else if (name.matches(".*\\d.*")) {
            nameLayout.setError("Name cannot contain numbers");
            nameEditText.requestFocus();
            isValid = false;
        } else if (name.length() < 3) {
            nameLayout.setError("Name must be at least 3 characters");
            nameEditText.requestFocus();
            isValid = false;
        }

        // 🔹 EMAIL: Not empty, valid format
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            emailEditText.requestFocus();
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email address");
            emailEditText.requestFocus();
            isValid = false;
        }

        // 🔹 PHONE: Not empty, digits only, EXACTLY 11 digits
        if (TextUtils.isEmpty(phone)) {
            phoneLayout.setError("Phone number is required");
            phoneEditText.requestFocus();
            isValid = false;
        } else if (!phone.matches("^[0-9]+$")) {
            phoneLayout.setError("Phone number must contain only digits");
            phoneEditText.requestFocus();
            isValid = false;
        } else if (phone.length() != 11) {
            phoneLayout.setError("Phone number must be exactly 11 digits");
            phoneEditText.requestFocus();
            isValid = false;
        }

        // 🔹 PASSWORD: Not empty, min 8 chars
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            passwordEditText.requestFocus();
            isValid = false;
        } else if (password.length() < 8) {
            passwordLayout.setError("Password must be at least 8 characters");
            passwordEditText.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    // ✅ SIGNUP: Save to Firebase Auth + Firestore
    private void performSignup() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);
        signupButton.setEnabled(false);

        // 🔹 Create user in Firebase Auth + Firestore
        authRepository.signUp(name, email, phone, password, "patient", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                progressBar.setVisibility(View.GONE);
                
                Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                
                // 🔹 Redirect to Login to complete the flow
                Intent intent = new Intent(SignupActivity.this, UserLoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                signupButton.setEnabled(true);
                
                // 🔹 Handle specific Firebase errors
                if (error.contains("email already in use")) {
                    emailLayout.setError("This email is already registered");
                    emailEditText.requestFocus();
                } else {
                    Toast.makeText(SignupActivity.this, "Signup failed: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}