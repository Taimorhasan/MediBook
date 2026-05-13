package com.example.medibook.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.example.medibook.activities.auth.UnifiedLoginActivity;

public abstract class BaseActivity extends AppCompatActivity {
    protected SessionManager sessionManager;
    protected FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        mAuth = FirebaseAuth.getInstance();
    }

    protected boolean checkRoleAndRedirect(String requiredRole) {
        if (sessionManager == null) {
            sessionManager = new SessionManager(this);
        }

        String currentRole = sessionManager.getUserRole();
        android.util.Log.d("BaseActivity", "Checking role for " + getClass().getSimpleName() + ": required=" + requiredRole + ", current=" + currentRole);
        
        // If role is missing but user is logged in, this might be a race condition during login
        if (currentRole == null || currentRole.isEmpty()) {
            // Check if we are actually signed in to Firebase
            if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
                android.util.Log.w("BaseActivity", "No current user and no session role. Redirecting to Login.");
                navigateToLogin();
                return false;
            }
            android.util.Log.d("BaseActivity", "No session role but user is signed in. Allowing for now.");
            return true; 
        }

        if (!currentRole.equalsIgnoreCase(requiredRole)) {
            // Special case: If original role is admin, allow access to admin screens even if impersonating
            if (requiredRole.equalsIgnoreCase("admin") && "admin".equalsIgnoreCase(sessionManager.getOriginalRole())) {
                android.util.Log.d("BaseActivity", "Bypassing role check for admin (original role)");
                return true;
            }

            android.util.Log.w("BaseActivity", "Access Denied: " + requiredRole + " role required. User has: " + currentRole);
            Toast.makeText(this, "Access Denied: " + requiredRole + " role required", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return false;
        }
        return true;
    }

    protected boolean checkPermissionAndRedirect(String requiredPermission) {
        if (sessionManager == null) {
            sessionManager = new SessionManager(this);
        }

        if (sessionManager.hasPermission(requiredPermission)) {
            return true;
        }

        android.util.Log.w("BaseActivity", "Access Denied: Missing permission '" + requiredPermission + "'");
        Toast.makeText(this, "Access Denied: Missing required permission", Toast.LENGTH_SHORT).show();
        navigateToLogin();
        return false;
    }

    protected void navigateToLogin() {
        Intent intent = new Intent(this, UnifiedLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
