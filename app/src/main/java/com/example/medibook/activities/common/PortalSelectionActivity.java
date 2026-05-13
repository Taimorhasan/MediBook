package com.example.medibook.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.medibook.R;
import com.example.medibook.activities.admin.AdminDashboardActivity;
import com.example.medibook.activities.auth.UnifiedLoginActivity;
import com.example.medibook.activities.doctor.DoctorDashboardActivity;
import com.example.medibook.activities.user.UserHomeActivity;
import com.example.medibook.models.Doctor;
import com.example.medibook.models.Role;
import com.example.medibook.models.User;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.DoctorRepository;
import com.example.medibook.repositories.RoleRepository;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.utils.SessionManager;

import java.util.List;

/**
 * Shown when a user has multiple roles. Dynamically loads their assigned roles
 * from Firestore and presents one card/button per role for selection.
 */
public class PortalSelectionActivity extends AppCompatActivity {

    private LinearLayout rolesContainer;
    private ProgressBar progressBar;
    private TextView tvTitle;
    private TextView tvSubtitle;

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private AuthRepository authRepository;
    private SessionManager sessionManager;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        userRepository = new UserRepository();
        roleRepository = new RoleRepository();
        authRepository = new AuthRepository();
        sessionManager = new SessionManager(this);

        userId = getIntent().getStringExtra("userId");

        rolesContainer = findViewById(R.id.roles_container);
        progressBar = findViewById(R.id.progress_bar);
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);

        // If no userId passed, try to get from Firebase current user
        if (userId == null || userId.isEmpty()) {
            com.google.firebase.auth.FirebaseUser currentUser = authRepository.getCurrentUser();
            if (currentUser != null) {
                userId = currentUser.getUid();
            } else {
                goToLogin();
                return;
            }
        }

        loadUserRoles();
    }

    private void loadUserRoles() {
        showLoading(true);
        userRepository.getUser(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user == null || user.getRoleIds() == null || user.getRoleIds().isEmpty()) {
                    showLoading(false);
                    Toast.makeText(PortalSelectionActivity.this,
                            "No roles assigned to this account.", Toast.LENGTH_LONG).show();
                    goToLogin();
                    return;
                }

                roleRepository.getRolesByIds(user.getRoleIds(), new RoleRepository.RolesCallback() {
                    @Override
                    public void onSuccess(List<Role> roles) {
                        showLoading(false);
                        if (roles.isEmpty()) {
                            Toast.makeText(PortalSelectionActivity.this,
                                    "No valid roles found.", Toast.LENGTH_LONG).show();
                            goToLogin();
                            return;
                        }
                        buildRoleCards(user, roles);
                    }

                    @Override
                    public void onFailure(String error) {
                        showLoading(false);
                        Toast.makeText(PortalSelectionActivity.this,
                                "Failed to load roles: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(PortalSelectionActivity.this,
                        "Error loading account: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildRoleCards(User user, List<Role> roles) {
        // Sort roles to prioritize admin, then doctor, then patient
        java.util.Collections.sort(roles, (r1, r2) -> {
            String d1 = r1.getDashboardType() != null ? r1.getDashboardType() : "";
            String d2 = r2.getDashboardType() != null ? r2.getDashboardType() : "";
            
            int p1 = getPriorityForDashboardType(d1);
            int p2 = getPriorityForDashboardType(d2);
            return Integer.compare(p1, p2);
        });
        
        rolesContainer.removeAllViews();

        for (Role role : roles) {
            View card = buildRoleCard(role, () -> onRoleSelected(user, role));
            rolesContainer.addView(card);
        }
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

    private View buildRoleCard(Role role, Runnable onSelect) {
        // Card wrapper
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(16));
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(16));
        cardView.setCardElevation(dpToPx(2));
        cardView.setCardBackgroundColor(0xFFFFFFFF);

        // Inner layout
        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24));

        // Role name
        TextView tvRoleName = new TextView(this);
        tvRoleName.setText(role.getRoleName() != null ? role.getRoleName() : "Portal");
        tvRoleName.setTextSize(20);
        tvRoleName.setTextColor(0xFF111827);
        tvRoleName.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.setMargins(0, 0, 0, dpToPx(6));
        tvRoleName.setLayoutParams(nameParams);

        // Dashboard type sub-label
        TextView tvDashboard = new TextView(this);
        String dashType = role.getDashboardType() != null ? role.getDashboardType() : "";
        tvDashboard.setText(getDescriptionForDashboard(dashType));
        tvDashboard.setTextSize(14);
        tvDashboard.setTextColor(0xFF6B7280);
        LinearLayout.LayoutParams dashParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        dashParams.setMargins(0, 0, 0, dpToPx(20));
        tvDashboard.setLayoutParams(dashParams);

        // Select button
        Button button = new Button(this);
        button.setText("Enter as " + (role.getRoleName() != null ? role.getRoleName() : "this role"));
        button.setTextColor(0xFFFFFFFF);
        button.setTextSize(14);
        button.setTypeface(null, android.graphics.Typeface.BOLD);
        try {
            button.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_primary));
        } catch (Exception ignored) {}
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(48)
        );
        button.setLayoutParams(btnParams);
        button.setOnClickListener(v -> onSelect.run());

        inner.addView(tvRoleName);
        inner.addView(tvDashboard);
        inner.addView(button);
        cardView.addView(inner);
        return cardView;
    }

    private String getDescriptionForDashboard(String dashboardType) {
        switch (dashboardType.toLowerCase()) {
            case "admin":
                return "Manage users, roles, appointments, and system settings.";
            case "doctor":
                return "View your appointments, manage schedules, and patient records.";
            case "patient":
                return "Book appointments, view history, and manage your health profile.";
            default:
                return "Access your personalized portal.";
        }
    }

    private void onRoleSelected(User user, Role role) {
        String dashboardType = role.getDashboardType();
        if (dashboardType == null) dashboardType = "";

        if ("admin".equalsIgnoreCase(dashboardType)) {
            sessionManager.saveUserSession(user.getUserId(), "admin");
            startActivity(new Intent(this, AdminDashboardActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        } else if ("doctor".equalsIgnoreCase(dashboardType)) {
            checkDoctorVerification(user);
        } else {
            sessionManager.saveUserSession(user.getUserId(), dashboardType.isEmpty() ? "patient" : dashboardType);
            startActivity(new Intent(this, UserHomeActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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
                    startActivity(new Intent(PortalSelectionActivity.this, DoctorDashboardActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                } else {
                    Toast.makeText(PortalSelectionActivity.this,
                            "Doctor account pending verification.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(PortalSelectionActivity.this,
                        "Verification check failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToLogin() {
        startActivity(new Intent(this, UnifiedLoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rolesContainer.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
