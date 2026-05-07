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
import com.example.medibook.activities.user.UserLoginActivity;
import com.example.medibook.repositories.AuthRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, emailEditText, phoneEditText, passwordEditText;
    private MaterialButton signupButton, btnGoogleSignIn, btnAppleSignIn;
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
        signupButton = findViewById(R.id.signupButton);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        btnAppleSignIn = findViewById(R.id.btnAppleSignIn);
        loginRedirect = findViewById(R.id.loginRedirect);
        progressBar = findViewById(R.id.progressBar);
        authRepository = new AuthRepository();
    }

    private void setupClickListeners() {
        // Sign Up Button Click
        signupButton.setOnClickListener(v -> {
            performSignup();
        });

        // Login Redirect Click (Already have account)
        loginRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, UserLoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Google Sign In Click
        btnGoogleSignIn.setOnClickListener(v -> {
            Toast.makeText(this, "Google Sign In clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement Google Sign In
        });

        // Apple Sign In Click
        btnAppleSignIn.setOnClickListener(v -> {
            Toast.makeText(this, "Apple Sign In clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement Apple Sign In
        });
    }

    private void performSignup() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Check if fields are empty
        if (name.isEmpty()) {
            nameEditText.setError("Name is required");
            nameEditText.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            phoneEditText.setError("Phone number is required");
            phoneEditText.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        // Show progress, disable button
        progressBar.setVisibility(View.VISIBLE);
        signupButton.setEnabled(false);

        // Sign up with Firebase
        authRepository.signUp(name, email, phone, password, "patient", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                progressBar.setVisibility(View.GONE);
                signupButton.setEnabled(true);
                Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                
                // Navigate to login
                Intent intent = new Intent(SignupActivity.this, UserLoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                signupButton.setEnabled(true);
                Toast.makeText(SignupActivity.this, "Signup failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}