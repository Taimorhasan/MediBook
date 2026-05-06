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
import com.example.medibook.repositories.NotificationRepository;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.models.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView signupRedirect;
    private ProgressBar progressBar;
    private AuthRepository authRepository;
    private UserRepository userRepository;
    private NotificationRepository notificationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize repositories
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
        notificationRepository = new NotificationRepository();

        // Initialize views
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        signupRedirect = findViewById(R.id.signup_redirect);
        progressBar = findViewById(R.id.progress_bar);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(LoginActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                performLogin(email, password);
            }
        });

        signupRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void performLogin(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        authRepository.signIn(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                // Get FCM token and update user profile
                FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String fcmToken = task.getResult();
                            notificationRepository.updateFCMToken(firebaseUser.getUid(), fcmToken);
                        }

                        // Get user data from Firestore to determine role
                        userRepository.getUser(firebaseUser.getUid(), new UserRepository.UserCallback() {
                            @Override
                            public void onSuccess(User user) {
                                progressBar.setVisibility(View.GONE);
                                loginButton.setEnabled(true);

                                // Navigate based on role
                                navigateBasedOnRole(user);
                            }

                            @Override
                            public void onFailure(String error) {
                                progressBar.setVisibility(View.GONE);
                                loginButton.setEnabled(true);
                                Toast.makeText(LoginActivity.this, "Failed to load user profile: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateBasedOnRole(User user) {
        if (user.getRoleIds().contains("admin")) {
            startActivity(new Intent(this, com.example.medibook.activities.admin.AdminDashboardActivity.class));
        } else if (user.getRoleIds().contains("manager")) {
            // Navigate to manager dashboard (to be implemented)
            startActivity(new Intent(this, com.example.medibook.activities.user.UserHomeActivity.class));
        } else {
            // Default to patient dashboard
            startActivity(new Intent(this, com.example.medibook.activities.user.UserHomeActivity.class));
        }
        finish();
    }
}