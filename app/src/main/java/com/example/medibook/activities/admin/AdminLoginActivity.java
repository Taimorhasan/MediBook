package com.example.medibook.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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

public class AdminLoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private Button loginButton;
    private ProgressBar progressBar;
    private AuthRepository authRepository;
    private UserRepository userRepository;
    private NotificationRepository notificationRepository;
    private com.example.medibook.utils.SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        // Initialize repositories
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
        notificationRepository = new NotificationRepository();
        sessionManager = new com.example.medibook.utils.SessionManager(this);

        // Initialize views
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progress_bar);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(AdminLoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(AdminLoginActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                performLogin(email, password);
            }
        });

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void performLogin(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        authRepository.signIn(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                // Get user data and verify admin role
                userRepository.getUser(firebaseUser.getUid(), new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        // Verify user is an admin
                        boolean isAdmin = false;
                        
                        // Check roleIds array (Modern check)
                        if (user.getRoleIds() != null && user.getRoleIds().contains("admin")) {
                            isAdmin = true;
                        } 
                        // Fallback to single 'role' field
                        else if ("admin".equalsIgnoreCase(user.getRole())) {
                            isAdmin = true;
                        }

                        if (isAdmin) {
                            // Get FCM token and update user profile
                            FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult() != null) {
                                        String fcmToken = task.getResult();
                                        notificationRepository.updateFCMToken(firebaseUser.getUid(), fcmToken);
                                    }

                                    progressBar.setVisibility(View.GONE);
                                    loginButton.setEnabled(true);

                                    // SAVE SESSION BEFORE NAVIGATING - Using commit for immediate effect
                                    boolean saved = sessionManager.saveUserSession(firebaseUser.getUid(), "admin");
                                    android.util.Log.d("AdminLogin", "Session saved: " + saved + " for role: admin");

                                    Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                                    startActivity(intent);
                                    finish();
                                });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            loginButton.setEnabled(true);
                            Toast.makeText(AdminLoginActivity.this, "This account does not have admin privileges", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        Toast.makeText(AdminLoginActivity.this, "Failed to load user profile: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                Toast.makeText(AdminLoginActivity.this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
