package com.example.medibook.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medibook.R;
import com.example.medibook.activities.auth.SignupActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class UserLoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnSignIn, btnGoogleSignIn, btnAppleSignIn;
    private TextView tvForgotPassword, tvCreateAccount;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        btnAppleSignIn = findViewById(R.id.btnAppleSignIn);

        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);

        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnSignIn.setOnClickListener(v -> attemptLogin());

        tvForgotPassword.setOnClickListener(v -> {
            // TODO: Navigate to Forgot Password Activity
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show();
        });

        btnGoogleSignIn.setOnClickListener(v -> {
            // TODO: Implement Google Sign In
            Toast.makeText(this, "Google Sign In clicked", Toast.LENGTH_SHORT).show();
        });

        btnAppleSignIn.setOnClickListener(v -> {
            // TODO: Implement Apple Sign In
            Toast.makeText(this, "Apple Sign In clicked", Toast.LENGTH_SHORT).show();
        });

        tvCreateAccount.setOnClickListener(v -> {
            // ✅ FIX: was UserSignupActivity.class — that class does not exist.
            //         Replaced with SignupActivity.class (exists in activities/auth/).
            Intent intent = new Intent(UserLoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return;
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        btnSignIn.setEnabled(false);

        // Simulate login process (Replace with your Firebase/Auth logic)
        new android.os.Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            btnSignIn.setEnabled(true);

            // TODO: Verify credentials with your backend/Firebase
            boolean loginSuccess = true; // Replace with actual auth result

            if (loginSuccess) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                navigateToHome();
            } else {
                Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
            }
        }, 2000); // 2 seconds delay for simulation
    }

    private void navigateToHome() {
        Intent intent = new Intent(UserLoginActivity.this, UserHomeActivity.class);
        startActivity(intent);
        finish(); // Close login activity so user can't go back to it
    }
}