package com.example.medibook.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;
import com.example.medibook.activities.auth.LoginActivity;
import com.example.medibook.activities.admin.AdminLoginActivity;
import com.google.android.material.card.MaterialCardView;

public class PortalSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portal_selection);

        MaterialCardView patientCard = findViewById(R.id.patient_card);
        MaterialCardView adminCard = findViewById(R.id.admin_card);

        patientCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PortalSelectionActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        adminCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PortalSelectionActivity.this, AdminLoginActivity.class);
                startActivity(intent);
            }
        });
    }
}