package com.example.medibook.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;
import com.example.medibook.activities.admin.AdminLoginActivity;
import com.example.medibook.activities.auth.LoginActivity;
import com.example.medibook.activities.auth.DoctorLoginActivity;

public class PortalSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portal_selection);

        // Initialize Buttons
        Button btnPatient = findViewById(R.id.btnPatient);
        Button btnDoctor = findViewById(R.id.btnDoctor);
        Button btnAdmin = findViewById(R.id.btnAdmin);

        // Patient Portal -> Login Activity
        btnPatient.setOnClickListener(v -> {
            Intent intent = new Intent(PortalSelectionActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Doctor Portal -> Doctor Login Activity
        btnDoctor.setOnClickListener(v -> {
            Intent intent = new Intent(PortalSelectionActivity.this, DoctorLoginActivity.class);
            startActivity(intent);
        });

        // Admin Portal -> Admin Login Activity
        btnAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(PortalSelectionActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });
    }
}
