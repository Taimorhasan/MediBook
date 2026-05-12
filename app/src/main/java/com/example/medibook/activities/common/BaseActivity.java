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

    protected void checkRoleAndRedirect(String requiredRole) {
        String currentRole = sessionManager.getUserRole();
        if (currentRole == null || !currentRole.equalsIgnoreCase(requiredRole)) {
            Toast.makeText(this, "Access Denied: Unauthorized Role", Toast.LENGTH_SHORT).show();
            navigateToPortal();
        }
    }

    protected void navigateToPortal() {
        Intent intent = new Intent(this, PortalSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
