package com.example.medibook.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medibook.R;
import com.example.medibook.activities.admin.AdminDashboardActivity;
import com.example.medibook.activities.common.PortalSelectionActivity;
import com.example.medibook.activities.doctor.DoctorDashboardActivity;
import com.example.medibook.activities.user.UserHomeActivity;
import com.example.medibook.models.Doctor;
import com.example.medibook.models.Role;
import com.example.medibook.models.User;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.DoctorRepository;
import com.example.medibook.repositories.NotificationRepository;
import com.example.medibook.repositories.RoleRepository;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.utils.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;

public class UnifiedLoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private MaterialButton loginButton, googleLoginButton;
    private TextView signupRedirect;
    private FrameLayout progressOverlay;
    
    private AuthRepository authRepository;
    private UserRepository userRepository;
    private NotificationRepository notificationRepository;
    private RoleRepository roleRepository;
    private SessionManager sessionManager;
    private GoogleSignInClient googleSignInClient;
    
    private static final int GOOGLE_SIGN_IN_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unified_login);

        // Initialize repositories and utilities
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
        notificationRepository = new NotificationRepository();
        roleRepository = new RoleRepository();
        sessionManager = new SessionManager(this);

        // Initialize views
        initViews();
        
        // Setup Google Sign-In
        setupGoogleSignIn();

        // Set listeners
        loginButton.setOnClickListener(v -> handleEmailLogin());
        googleLoginButton.setOnClickListener(v -> performGoogleSignIn());
        signupRedirect.setOnClickListener(v -> {
            startActivity(new Intent(UnifiedLoginActivity.this, SignupActivity.class));
        });
    }

    private void initViews() {
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        googleLoginButton = findViewById(R.id.google_login_button);
        signupRedirect = findViewById(R.id.signup_redirect);
        progressOverlay = findViewById(R.id.progress_overlay);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void showLoading(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        googleLoginButton.setEnabled(!show);
    }

    private void handleEmailLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        authRepository.signIn(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                fetchUserDetailsAndNavigate(firebaseUser);
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(UnifiedLoginActivity.this, "Login Failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performGoogleSignIn() {
        showLoading(true);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN_CODE) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                if (account != null && account.getIdToken() != null) {
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    authenticateWithGoogle(credential, account);
                } else {
                    showLoading(false);
                    Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                showLoading(false);
                Toast.makeText(this, "Google Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void authenticateWithGoogle(AuthCredential credential, GoogleSignInAccount account) {
        authRepository.signInWithCredential(credential, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                authRepository.checkUserExists(firebaseUser.getUid(), exists -> {
                    if (!exists) {
                        // Create default patient profile for new Google users
                        String email = account.getEmail() != null ? account.getEmail() : firebaseUser.getEmail();
                        String name = account.getDisplayName() != null ? account.getDisplayName() : "Patient";
                        
                        authRepository.createPatientProfile(firebaseUser.getUid(), name, email, "", new AuthRepository.VoidCallback() {
                            @Override
                            public void onSuccess() {
                                fetchUserDetailsAndNavigate(firebaseUser);
                            }

                            @Override
                            public void onFailure(String error) {
                                fetchUserDetailsAndNavigate(firebaseUser);
                            }
                        });
                    } else {
                        fetchUserDetailsAndNavigate(firebaseUser);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(UnifiedLoginActivity.this, "Authentication failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserDetailsAndNavigate(FirebaseUser firebaseUser) {
        // Update FCM Token for notifications
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                notificationRepository.updateFCMToken(firebaseUser.getUid(), task.getResult());
            }

            // Fetch user profile to determine role
            userRepository.getUser(firebaseUser.getUid(), new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    showLoading(false);
                    if (user != null && user.getRoleIds() != null) {
                        navigateBasedOnRole(user);
                    } else {
                        Toast.makeText(UnifiedLoginActivity.this, "User profile not found", Toast.LENGTH_SHORT).show();
                        authRepository.signOut();
                    }
                }

                @Override
                public void onFailure(String error) {
                    showLoading(false);
                    Toast.makeText(UnifiedLoginActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void navigateBasedOnRole(User user) {
        android.util.Log.d("UnifiedLogin", "User roleIds: " + (user.getRoleIds() != null ? user.getRoleIds().toString() : "null"));
        roleRepository.getRolesByIds(user.getRoleIds(), new RoleRepository.RolesCallback() {
            @Override
            public void onSuccess(java.util.List<Role> roles) {
                android.util.Log.d("UnifiedLogin", "Fetched roles count: " + roles.size());
                for (Role role : roles) {
                    android.util.Log.d("UnifiedLogin", "Role: " + role.getRoleName() + ", DashboardType: " + role.getDashboardType());
                }

                showLoading(false);
                if (roles.isEmpty()) {
                    Toast.makeText(UnifiedLoginActivity.this, "No valid roles assigned to this account", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if admin role exists - if so, redirect directly to admin dashboard
                for (Role role : roles) {
                    if (isAdminRole(role)) {
                        android.util.Log.d("UnifiedLogin", "Admin role found, redirecting to admin dashboard");
                        sessionManager.saveUserSession(user.getUserId(), "admin");
                        startActivity(new Intent(UnifiedLoginActivity.this, AdminDashboardActivity.class));
                        finish();
                        return;
                    }
                }

                // Check if doctor role exists - if so, check verification
                for (Role role : roles) {
                    if (isDoctorRole(role)) {
                        android.util.Log.d("UnifiedLogin", "Doctor role found, checking verification");
                        checkDoctorVerification(user);
                        return;
                    }
                }

                // If multiple roles but no admin/doctor, show selection screen
                if (roles.size() > 1) {
                    android.util.Log.d("UnifiedLogin", "Multiple roles, showing selection screen");
                    // Sort roles to prioritize doctor over patient for selection
                    java.util.Collections.sort(roles, (r1, r2) -> {
                        String d1 = r1.getDashboardType() != null ? r1.getDashboardType() : "";
                        String d2 = r2.getDashboardType() != null ? r2.getDashboardType() : "";

                        int p1 = getPriorityForDashboardType(d1);
                        int p2 = getPriorityForDashboardType(d2);
                        return Integer.compare(p1, p2);
                    });

                    Intent intent = new Intent(UnifiedLoginActivity.this, PortalSelectionActivity.class);
                    intent.putExtra("userId", user.getUserId());
                    startActivity(intent);
                    finish();
                } else {
                    // Single role: Direct redirect
                    android.util.Log.d("UnifiedLogin", "Single role, direct redirect to: " + roles.get(0).getDashboardType());
                    redirectByRole(user, roles.get(0));
                }
            }

            @Override
            public void onFailure(String error) {
                android.util.Log.e("UnifiedLogin", "Failed to fetch roles: " + error);
                showLoading(false);
                Toast.makeText(UnifiedLoginActivity.this, "Failed to fetch roles: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private boolean isAdminRole(Role role) {
        // Check dashboardType first
        if ("admin".equalsIgnoreCase(role.getDashboardType())) {
            return true;
        }
        // Fallback: check role name for existing roles without dashboardType
        String roleName = role.getRoleName();
        return roleName != null && (roleName.toLowerCase().contains("admin") || "admin".equalsIgnoreCase(roleName));
    }

    private boolean isDoctorRole(Role role) {
        // Check dashboardType first
        if ("doctor".equalsIgnoreCase(role.getDashboardType())) {
            return true;
        }
        // Fallback: check role name for existing roles without dashboardType
        String roleName = role.getRoleName();
        return roleName != null && (roleName.toLowerCase().contains("doctor") || "doctor".equalsIgnoreCase(roleName));
    }

    private int getPriorityForDashboardType(String dashboardType) {
        if (dashboardType == null) dashboardType = "";
        switch (dashboardType.toLowerCase()) {
            case "admin":
                return 1;
            case "doctor":
                return 2;
            case "patient":
                return 3;
            default:
                return 4;
        }
    }

    private void redirectByRole(User user, Role role) {
        Intent intent = null;
        String dashboardType = role.getDashboardType();

        if (isAdminRole(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
            dashboardType = "admin";
        } else if (isDoctorRole(role)) {
            // Doctors need verification check
            checkDoctorVerification(user);
            return;
        } else if ("patient".equalsIgnoreCase(dashboardType) ||
                   (role.getRoleName() != null && role.getRoleName().toLowerCase().contains("patient"))) {
            intent = new Intent(this, UserHomeActivity.class);
            dashboardType = "patient";
        } else {
            // Default or fallback
            intent = new Intent(this, UserHomeActivity.class);
            dashboardType = "patient";
        }

        if (intent != null) {
            sessionManager.saveUserSession(user.getUserId(), dashboardType);
            startActivity(intent);
            finish();
        }
    }

    private void checkDoctorVerification(User user) {
        showLoading(true);
        new DoctorRepository().getDoctor(user.getUserId(), new DoctorRepository.DoctorCallback() {
            @Override
            public void onSuccess(Doctor doctor) {
                showLoading(false);
                if (doctor != null && doctor.isVerified()) {
                    sessionManager.saveUserSession(user.getUserId(), "doctor");
                    startActivity(new Intent(UnifiedLoginActivity.this, DoctorDashboardActivity.class));
                    finish();
                } else {
                    Toast.makeText(UnifiedLoginActivity.this, "Doctor account pending verification", Toast.LENGTH_LONG).show();
                    authRepository.signOut();
                    sessionManager.clearSession();
                }
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(UnifiedLoginActivity.this, "Verification status check failed", Toast.LENGTH_SHORT).show();
                authRepository.signOut();
            }
        });
    }
}
