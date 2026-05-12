package com.example.medibook.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;  // ✅ Added
import android.widget.TextView;      // ✅ Added
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;
import com.example.medibook.activities.auth.SignupActivity;
import com.example.medibook.repositories.AuthRepository;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseUser;

public class UserLoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnSignIn, btnGoogleSignIn;
    private TextView tvCreateAccount;  // ✅ Now recognized
    private ProgressBar progressBar;   // ✅ Now recognized
    private com.example.medibook.utils.SessionManager sessionManager;
    
    private AuthRepository authRepository;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        
        initializeViews();
        setupGoogleSignIn();
        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
        progressBar = findViewById(R.id.progressBar);
        
        authRepository = new AuthRepository();
        sessionManager = new com.example.medibook.utils.SessionManager(this);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        // ✅ Sign In Button
        btnSignIn.setOnClickListener(v -> {
            if (validateInputs()) {
                performEmailLogin();
            }
        });

        // ✅ Google Sign In Button
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        // ✅ Create Account
        tvCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(UserLoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    // 🔹 Validate Email & Password
    private boolean validateInputs() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean isValid = true;

        etEmail.setError(null);
        etPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            isValid = false;
        } else if (password.length() < 8) {
            etPassword.setError("Password must be at least 8 characters");
            etPassword.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    // 🔹 Email/Password Login
    private void performEmailLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);
        btnSignIn.setEnabled(false);

        authRepository.signIn(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                progressBar.setVisibility(View.GONE);
                btnSignIn.setEnabled(true);
                
                // Check role and navigate accordingly
                authRepository.getUserRole(user.getUid(), role -> {
                    // Update Session
                    sessionManager.saveUserSession(user.getUid(), role != null ? role : "patient");

                    if ("admin".equals(role)) {
                        Toast.makeText(UserLoginActivity.this, "Admin access granted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(UserLoginActivity.this, com.example.medibook.activities.admin.AdminDashboardActivity.class));
                        finish();
                    } else if ("doctor".equals(role)) {
                        Toast.makeText(UserLoginActivity.this, "Doctor access granted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(UserLoginActivity.this, com.example.medibook.activities.doctor.DoctorDashboardActivity.class));
                        finish();
                    } else {
                        // Default to patient
                        Toast.makeText(UserLoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToDashboard();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                btnSignIn.setEnabled(true);
                
                if (error.contains("user not found") || error.contains("no user record")) {
                    Toast.makeText(UserLoginActivity.this, "User not found. Please create an account.", Toast.LENGTH_LONG).show();
                } else if (error.contains("wrong password")) {
                    Toast.makeText(UserLoginActivity.this, "Incorrect password. Please try again.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(UserLoginActivity.this, "Login failed: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // 🔹 Google Sign-In
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account.getIdToken());
            }
        } catch (ApiException e) {
            String errorMsg = "Google Sign-In failed (Code: " + e.getStatusCode() + ")";
            if (e.getStatusCode() == 10) {
                errorMsg += ": Developer Error. Check SHA-1 in Firebase Console.";
            } else if (e.getStatusCode() == 12500) {
                errorMsg += ": Sign-In Failed. Check Google Play Services.";
            }
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        progressBar.setVisibility(View.VISIBLE);
        btnGoogleSignIn.setEnabled(false);
        
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        
        authRepository.signInWithCredential(credential, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                authRepository.checkUserExists(user.getUid(), exists -> {
                    progressBar.setVisibility(View.GONE);
                    btnGoogleSignIn.setEnabled(true);
                    
                    if (exists) {
                        authRepository.getUserRole(user.getUid(), role -> {
                            // Update Session
                            sessionManager.saveUserSession(user.getUid(), role != null ? role : "patient");

                            if ("admin".equals(role)) {
                                Toast.makeText(UserLoginActivity.this, "Admin access granted", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(UserLoginActivity.this, com.example.medibook.activities.admin.AdminDashboardActivity.class));
                                finish();
                            } else if ("doctor".equals(role)) {
                                Toast.makeText(UserLoginActivity.this, "Doctor access granted", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(UserLoginActivity.this, com.example.medibook.activities.doctor.DoctorDashboardActivity.class));
                                finish();
                            } else {
                                Toast.makeText(UserLoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                                navigateToDashboard();
                            }
                        });
                    } else {
                        // Auto-create patient profile for new Google users
                        authRepository.createPatientProfile(
                            user.getUid(),
                            user.getDisplayName() != null ? user.getDisplayName() : "Patient",
                            user.getEmail(),
                            "",
                            new AuthRepository.VoidCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(UserLoginActivity.this, "Account created with Google!", Toast.LENGTH_SHORT).show();
                                    navigateToDashboard();
                                }
                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(UserLoginActivity.this, "Profile setup failed: " + error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        );
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                btnGoogleSignIn.setEnabled(true);
                Toast.makeText(UserLoginActivity.this, "Authentication failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(UserLoginActivity.this, UserHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}