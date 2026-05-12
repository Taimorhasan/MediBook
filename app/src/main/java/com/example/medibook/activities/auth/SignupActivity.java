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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.example.medibook.activities.user.UserHomeActivity;
import com.example.medibook.activities.doctor.DoctorDashboardActivity;
import com.example.medibook.activities.admin.AdminDashboardActivity;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, emailEditText, phoneEditText, passwordEditText;
    private TextInputLayout nameLayout, emailLayout, phoneLayout, passwordLayout;
    private MaterialButton signupButton, btnGoogleSignIn;
    private TextView loginRedirect;
    private ProgressBar progressBar;
    private AuthRepository authRepository;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        
        initializeViews();
        setupGoogleSignIn();
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

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        // ✅ Sign Up Button - WITH VALIDATIONS
        signupButton.setOnClickListener(v -> {
            if (validateInputs()) {
                performSignup();
            }
        });

        // ✅ Google Sign Up - Native Flow
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

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

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            com.google.android.gms.tasks.Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                String errorMsg = "Google Sign-In failed (Code: " + e.getStatusCode() + ")";
                if (e.getStatusCode() == 10) {
                    errorMsg += ": Developer Error. Check SHA-1 in Firebase Console.";
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        progressBar.setVisibility(View.VISIBLE);
        authRepository.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null), new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                checkUserAndNavigate(user);
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SignupActivity.this, "Authentication failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserAndNavigate(FirebaseUser user) {
        authRepository.checkUserExists(user.getUid(), exists -> {
            if (exists) {
                // If user exists, redirect based on their role
                authRepository.getUserRole(user.getUid(), role -> {
                    progressBar.setVisibility(View.GONE);
                    if ("admin".equals(role)) {
                        Toast.makeText(SignupActivity.this, "Welcome back, Admin!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignupActivity.this, AdminDashboardActivity.class));
                    } else if ("doctor".equals(role)) {
                        Toast.makeText(SignupActivity.this, "Welcome back, Doctor!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignupActivity.this, DoctorDashboardActivity.class));
                    } else {
                        Toast.makeText(SignupActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignupActivity.this, UserHomeActivity.class));
                    }
                    finish();
                });
            } else {
                // New user, create as patient
                authRepository.createUserProfile(user.getUid(), 
                    user.getDisplayName() != null ? user.getDisplayName() : "Google User",
                    user.getEmail(), "", "patient", new AuthRepository.VoidCallback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SignupActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupActivity.this, UserHomeActivity.class));
                            finish();
                        }

                        @Override
                        public void onFailure(String error) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SignupActivity.this, "Failed to create profile: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });
    }
}