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

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // 3 seconds
    private static final int PROGRESS_MAX = 100;
    
    private ProgressBar progressBar;
    private TextView tvInitializing;
    private Handler handler;
    private Runnable progressRunnable;
    private int progress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        tvInitializing = findViewById(R.id.tvInitializing);
        
        // Initialize handler
        handler = new Handler(Looper.getMainLooper());
        
        // Start progress animation
        startProgressAnimation();
        
        // Navigate to next screen after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigateToNextScreen();
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
    
    private void navigateToNextScreen() {
        // Remove callbacks to prevent memory leaks
        handler.removeCallbacks(progressRunnable);
        
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