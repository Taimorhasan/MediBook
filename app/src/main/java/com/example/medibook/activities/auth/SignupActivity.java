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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, emailEditText, phoneEditText, passwordEditText;
    private Button signupButton;
    private TextView loginRedirect;
    private ProgressBar progressBar;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize repository
        authRepository = new AuthRepository();

        // Initialize views
        nameEditText = findViewById(R.id.name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        phoneEditText = findViewById(R.id.phone_edit_text);
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
                String password = passwordEditText.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(SignupActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                performSignup(name, email, phone, password);
            }
        });

        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to LoginActivity
            }
        });
    }

    private void performSignup(String name, String email, String phone, String password) {
        progressBar.setVisibility(View.VISIBLE);
        signupButton.setEnabled(false);

        authRepository.signUp(name, email, phone, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                progressBar.setVisibility(View.GONE);
                signupButton.setEnabled(true);
                Toast.makeText(SignupActivity.this, "Account Created Successfully", Toast.LENGTH_LONG).show();
                finish(); // Go back to login
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