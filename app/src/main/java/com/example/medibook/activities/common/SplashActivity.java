package com.example.medibook.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medibook.R;
import com.example.medibook.activities.auth.UnifiedLoginActivity;
import com.example.medibook.activities.user.UserHomeActivity;
import com.example.medibook.activities.doctor.DoctorDashboardActivity;
import com.example.medibook.activities.admin.AdminDashboardActivity;
import com.example.medibook.models.Role;
import com.example.medibook.models.User;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.DoctorRepository;
import com.example.medibook.repositories.RoleRepository;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.utils.SessionManager;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private static final int PROGRESS_MAX = 100;

    private ProgressBar progressBar;
    private TextView tvInitializing;
    private Handler handler;
    private Runnable progressRunnable;
    private int progress = 0;
    private AuthRepository authRepository;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize repositories
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
        roleRepository = new RoleRepository();
        sessionManager = new SessionManager(this);

        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        tvInitializing = findViewById(R.id.tvInitializing);

        // Initialize handler
        handler = new Handler(Looper.getMainLooper());

        // Start progress animation
        startProgressAnimation();

        // Check for auto-login and navigate after delay
        new Handler(Looper.getMainLooper()).postDelayed(this::checkAutoLogin, SPLASH_DELAY);
    }

    private void startProgressAnimation() {
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                progress += 2;
                progressBar.setProgress(progress);

                if (progress < 30) {
                    tvInitializing.setText(R.string.initializing_session);
                } else if (progress < 60) {
                    tvInitializing.setText(R.string.loading_resources);
                } else if (progress < 90) {
                    tvInitializing.setText(R.string.verifying_security);
                } else {
                    tvInitializing.setText(R.string.finalizing_setup);
                }

                if (progress < PROGRESS_MAX) {
                    handler.postDelayed(this, 60);
                }
            }
        };
        handler.post(progressRunnable);
    }

    private void checkAutoLogin() {
        handler.removeCallbacks(progressRunnable);

        FirebaseUser currentUser = authRepository.getCurrentUser();

        if (currentUser != null) {
            userRepository.getUser(currentUser.getUid(), new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    if (user != null && user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
                        resolveRolesAndNavigate(user);
                    } else {
                        navigateToUnifiedLogin();
                    }
                }

                @Override
                public void onFailure(String error) {
                    navigateToUnifiedLogin();
                }
            });
        } else {
            navigateToUnifiedLogin();
        }
    }

    /**
     * Resolves role Firestore document IDs to actual Role objects,
     * then picks the correct dashboard — same logic as UnifiedLoginActivity.
     */
    private void resolveRolesAndNavigate(User user) {
        roleRepository.getRolesByIds(user.getRoleIds(), new RoleRepository.RolesCallback() {
            @Override
            public void onSuccess(List<Role> roles) {
                if (roles.isEmpty()) {
                    navigateToUnifiedLogin();
                    return;
                }

                // Check if admin role exists - if so, redirect directly to admin dashboard
                for (Role role : roles) {
                    if (isAdminRole(role)) {
                        sessionManager.saveUserSession(user.getUserId(), "admin");
                        Intent intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }

                // Check if doctor role exists - if so, check verification
                for (Role role : roles) {
                    if (isDoctorRole(role)) {
                        checkDoctorVerification(user);
                        return;
                    }
                }

                // If multiple roles but no admin/doctor, show selection screen
                if (roles.size() > 1) {
                    // Sort roles to prioritize doctor over patient for selection
                    java.util.Collections.sort(roles, (r1, r2) -> {
                        String d1 = r1.getDashboardType() != null ? r1.getDashboardType() : "";
                        String d2 = r2.getDashboardType() != null ? r2.getDashboardType() : "";

                        int p1 = getPriorityForDashboardType(d1);
                        int p2 = getPriorityForDashboardType(d2);
                        return Integer.compare(p1, p2);
                    });

                    Intent intent = new Intent(SplashActivity.this, PortalSelectionActivity.class);
                    intent.putExtra("userId", user.getUserId());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    redirectByRole(user, roles.get(0));
                }
            }

            @Override
            public void onFailure(String error) {
                navigateToUnifiedLogin();
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
        String dashboardType = role.getDashboardType();
        if (dashboardType == null) dashboardType = "";

        if (isAdminRole(role)) {
            sessionManager.saveUserSession(user.getUserId(), "admin");
            Intent intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else if (isDoctorRole(role)) {
            checkDoctorVerification(user);
        } else {
            // patient or any other role → UserHome
            sessionManager.saveUserSession(user.getUserId(), dashboardType.isEmpty() ? "patient" : dashboardType);
            Intent intent = new Intent(SplashActivity.this, UserHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void checkDoctorVerification(User user) {
        new DoctorRepository().getDoctor(user.getUserId(), new DoctorRepository.DoctorCallback() {
            @Override
            public void onSuccess(com.example.medibook.models.Doctor doctor) {
                if (doctor != null && doctor.isVerified()) {
                    sessionManager.saveUserSession(user.getUserId(), "doctor");
                    Intent intent = new Intent(SplashActivity.this, DoctorDashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Doctor not verified — send to login so they get the proper message
                    authRepository.signOut();
                    sessionManager.clearSession();
                    navigateToUnifiedLogin();
                }
            }

            @Override
            public void onFailure(String error) {
                authRepository.signOut();
                sessionManager.clearSession();
                navigateToUnifiedLogin();
            }
        });
    }

    private void navigateToUnifiedLogin() {
        Intent intent = new Intent(SplashActivity.this, UnifiedLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && progressRunnable != null) {
            handler.removeCallbacks(progressRunnable);
        }
    }
}
