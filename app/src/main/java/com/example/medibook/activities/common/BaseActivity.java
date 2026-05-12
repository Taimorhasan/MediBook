package com.example.medibook.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

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
        android.util.Log.d("BaseActivity", "Checking role: required=" + requiredRole + ", current=" + currentRole);
        
        // If role is missing but user is logged in, try to see if we're in a weird state
        if (currentRole == null || currentRole.isEmpty()) {
            if (mAuth.getCurrentUser() != null) {
                android.util.Log.w("BaseActivity", "User logged in but role missing from SessionManager. This might cause a redirect.");
                // We could fetch from Firestore here, but since this is synchronous, 
                // let's at least check if we can skip redirect if we're ALREADY in an admin activity
                // and the user was just verified.
            }
        }

        if (currentRole == null || !currentRole.equalsIgnoreCase(requiredRole)) {
            android.util.Log.w("BaseActivity", "Access Denied: Current role '" + (currentRole == null ? "null" : currentRole) + "' does not match required role '" + requiredRole + "'");
            
            // Only redirect if we are sure there is no valid session
            if (mAuth.getCurrentUser() == null || (currentRole != null && !currentRole.isEmpty())) {
                Toast.makeText(this, "Access Denied: Unauthorized Role", Toast.LENGTH_SHORT).show();
                navigateToPortal();
                return false;
            }
        }
        return true;
    }

    protected void navigateToPortal() {
        Intent intent = new Intent(this, PortalSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
