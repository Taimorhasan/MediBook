package com.example.medibook.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medibook.R;
import com.example.medibook.activities.auth.LoginActivity;
import com.example.medibook.activities.user.UserHomeActivity;
import com.example.medibook.activities.doctor.DoctorDashboardActivity;
import com.example.medibook.activities.admin.AdminDashboardActivity;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.models.User;
import com.google.firebase.auth.FirebaseUser;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Initialize repositories
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
        
        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        tvInitializing = findViewById(R.id.tvInitializing);
        
        // Initialize handler
        handler = new Handler(Looper.getMainLooper());
        
        // Start progress animation
        startProgressAnimation();
        
        // Check for auto-login and navigate
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkAutoLogin();
        }, SPLASH_DELAY);
    }
    
    private void startProgressAnimation() {
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                progress += 2;
                progressBar.setProgress(progress);
                
                // Update status text based on progress
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
                    handler.postDelayed(this, 60); // Update every 60ms
                }
            }
        };
        
        handler.post(progressRunnable);
    }
    
    private void checkAutoLogin() {
        // Remove callbacks to prevent memory leaks
        handler.removeCallbacks(progressRunnable);
        
        // Check if user is already logged in
        FirebaseUser currentUser = authRepository.getCurrentUser();
        
        if (currentUser != null) {
            // User is already logged in, get their role and redirect
            userRepository.getUser(currentUser.getUid(), new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    navigateBasedOnRole(user);
                }
                
                @Override
                public void onFailure(String error) {
                    // If we can't fetch user data, go to portal selection
                    navigateToPortalSelection();
                }
            });
        } else {
            // No user logged in, go to portal selection
            navigateToPortalSelection();
        }
    }
    
    private void navigateBasedOnRole(User user) {
        Intent intent;
        
        if (user.getRoleIds() != null) {
            if (user.getRoleIds().contains("admin")) {
                intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
            } else if (user.getRoleIds().contains("doctor")) {
                intent = new Intent(SplashActivity.this, DoctorDashboardActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, UserHomeActivity.class);
            }
        } else {
            intent = new Intent(SplashActivity.this, UserHomeActivity.class);
        }
        
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    private void navigateToPortalSelection() {
        Intent intent = new Intent(SplashActivity.this, PortalSelectionActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handlers
        if (handler != null && progressRunnable != null) {
            handler.removeCallbacks(progressRunnable);
        }
    }
}
