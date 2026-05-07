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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class AdminLoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private Button loginButton;
    private Button googleLoginButton;
    private ProgressBar progressBar;
    private AuthRepository authRepository;
    private UserRepository userRepository;
    private NotificationRepository notificationRepository;
    private GoogleSignInClient googleSignInClient;
    private static final int GOOGLE_SIGN_IN_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        // Initialize repositories
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
        notificationRepository = new NotificationRepository();

        // Initialize Google Sign-In
        setupGoogleSignIn();

        // Initialize views
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        googleLoginButton = findViewById(R.id.google_login_button);
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

        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performGoogleSignIn();
            }
        });
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void performGoogleSignIn() {
        progressBar.setVisibility(View.VISIBLE);
        googleLoginButton.setEnabled(false);
        loginButton.setEnabled(false);

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN_CODE) {
            try {
                com.google.android.gms.auth.api.signin.GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                
                if (account != null && account.getIdToken() != null) {
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    authenticateWithGoogle(credential, account);
                } else {
                    progressBar.setVisibility(View.GONE);
                    googleLoginButton.setEnabled(true);
                    loginButton.setEnabled(true);
                    Toast.makeText(this, "Google Sign-In failed: Account error", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                progressBar.setVisibility(View.GONE);
                googleLoginButton.setEnabled(true);
                loginButton.setEnabled(true);
                Toast.makeText(this, "Google Sign-In error: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void authenticateWithGoogle(AuthCredential credential, com.google.android.gms.auth.api.signin.GoogleSignInAccount account) {
        authRepository.signInWithCredential(credential, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                // Check if user exists and verify admin role
                userRepository.getUser(firebaseUser.getUid(), new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        if (user != null && user.getRoleIds() != null && user.getRoleIds().contains("admin")) {
                            handleGoogleLoginSuccess(firebaseUser);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            googleLoginButton.setEnabled(true);
                            loginButton.setEnabled(true);
                            Toast.makeText(AdminLoginActivity.this, "This account does not have admin privileges", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        progressBar.setVisibility(View.GONE);
                        googleLoginButton.setEnabled(true);
                        loginButton.setEnabled(true);
                        Toast.makeText(AdminLoginActivity.this, "Failed to verify admin status: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                googleLoginButton.setEnabled(true);
                loginButton.setEnabled(true);
                Toast.makeText(AdminLoginActivity.this, "Authentication failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleGoogleLoginSuccess(FirebaseUser firebaseUser) {
        // Get FCM token and update user profile
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String fcmToken = task.getResult();
                    notificationRepository.updateFCMToken(firebaseUser.getUid(), fcmToken);
                }

                progressBar.setVisibility(View.GONE);
                googleLoginButton.setEnabled(true);
                loginButton.setEnabled(true);

                // Navigate to admin dashboard
                Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
                finish();
            });
    }

    private void performLogin(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);
        googleLoginButton.setEnabled(false);

        authRepository.signIn(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                // Get user data and verify admin role
                userRepository.getUser(firebaseUser.getUid(), new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        // Verify user is an admin
                        if (user.getRoleIds().contains("admin")) {
                            // Get FCM token and update user profile
                            FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult() != null) {
                                        String fcmToken = task.getResult();
                                        notificationRepository.updateFCMToken(firebaseUser.getUid(), fcmToken);
                                    }

                                    progressBar.setVisibility(View.GONE);
                                    loginButton.setEnabled(true);
                                    googleLoginButton.setEnabled(true);

                                    Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                                    startActivity(intent);
                                    finish();
                                });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            loginButton.setEnabled(true);
                            googleLoginButton.setEnabled(true);
                            Toast.makeText(AdminLoginActivity.this, "This account does not have admin privileges", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        googleLoginButton.setEnabled(true);
                        Toast.makeText(AdminLoginActivity.this, "Failed to load user profile: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                googleLoginButton.setEnabled(true);
                Toast.makeText(AdminLoginActivity.this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
