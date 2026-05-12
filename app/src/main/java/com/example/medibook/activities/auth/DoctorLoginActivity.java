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
import com.example.medibook.activities.doctor.DoctorDashboardActivity;
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

public class DoctorLoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private Button loginButton;
    private Button googleLoginButton;
    private TextView signupRedirect;
    private ProgressBar progressBar;
    private AuthRepository authRepository;
    private UserRepository userRepository;
    private NotificationRepository notificationRepository;
    private GoogleSignInClient googleSignInClient;
    private static final int GOOGLE_SIGN_IN_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_login);

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
        signupRedirect = findViewById(R.id.signup_redirect);
        progressBar = findViewById(R.id.progress_bar);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(DoctorLoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(DoctorLoginActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
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

        signupRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DoctorLoginActivity.this, DoctorSignupActivity.class);
                startActivity(intent);
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
                String errorMsg = "Google Sign-In failed (Code: " + e.getStatusCode() + ")";
                if (e.getStatusCode() == 10) {
                    errorMsg += ": Developer Error. Check SHA-1 in Firebase Console.";
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void authenticateWithGoogle(AuthCredential credential, com.google.android.gms.auth.api.signin.GoogleSignInAccount account) {
        authRepository.signInWithCredential(credential, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                // Check if user profile exists, if not create one as doctor
                authRepository.checkUserExists(firebaseUser.getUid(), new AuthRepository.UserExistsCallback() {
                    @Override
                    public void onResult(boolean exists) {
                        if (!exists) {
                            // Create doctor profile for first-time Google sign-in users
                            String email = account.getEmail() != null ? account.getEmail() : firebaseUser.getEmail();
                            String name = account.getDisplayName() != null ? account.getDisplayName() : "Doctor";
                            String phone = firebaseUser.getPhoneNumber() != null ? firebaseUser.getPhoneNumber() : "";
                            
                            // Create doctor profile with basic info
                            authRepository.createUserProfile(firebaseUser.getUid(), name, email, phone, "doctor", new AuthRepository.VoidCallback() {
                                @Override
                                public void onSuccess() {
                                    handleGoogleLoginSuccess(firebaseUser);
                                }

                                @Override
                                public void onFailure(String error) {
                                    handleGoogleLoginSuccess(firebaseUser);
                                }
                            });
                        } else {
                            handleGoogleLoginSuccess(firebaseUser);
                        }
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                googleLoginButton.setEnabled(true);
                loginButton.setEnabled(true);
                Toast.makeText(DoctorLoginActivity.this, "Authentication failed: " + error, Toast.LENGTH_SHORT).show();
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

                // Get user data from Firestore to verify doctor role
                userRepository.getUser(firebaseUser.getUid(), new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        progressBar.setVisibility(View.GONE);
                        googleLoginButton.setEnabled(true);
                        loginButton.setEnabled(true);

                        // Verify user is a doctor
                        if (user.getRoleIds().contains("doctor")) {
                            startActivity(new Intent(DoctorLoginActivity.this, DoctorDashboardActivity.class));
                            finish();
                        } else {
                            Toast.makeText(DoctorLoginActivity.this, "This account is not registered as a doctor", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        progressBar.setVisibility(View.GONE);
                        googleLoginButton.setEnabled(true);
                        loginButton.setEnabled(true);
                        Toast.makeText(DoctorLoginActivity.this, "Failed to load user profile: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            });
    }

    private void performLogin(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);
        googleLoginButton.setEnabled(false);

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

                        // Get user data from Firestore to verify doctor role
                        userRepository.getUser(firebaseUser.getUid(), new UserRepository.UserCallback() {
                            @Override
                            public void onSuccess(User user) {
                                progressBar.setVisibility(View.GONE);
                                loginButton.setEnabled(true);
                                googleLoginButton.setEnabled(true);

                                // Verify user is a doctor
                                if (user.getRoleIds().contains("doctor")) {
                                    startActivity(new Intent(DoctorLoginActivity.this, DoctorDashboardActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(DoctorLoginActivity.this, "This account is not registered as a doctor", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(String error) {
                                progressBar.setVisibility(View.GONE);
                                loginButton.setEnabled(true);
                                googleLoginButton.setEnabled(true);
                                Toast.makeText(DoctorLoginActivity.this, "Failed to load user profile: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                googleLoginButton.setEnabled(true);
                Toast.makeText(DoctorLoginActivity.this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
