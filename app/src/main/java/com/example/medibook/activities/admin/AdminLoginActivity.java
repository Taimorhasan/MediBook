package com.example.medibook.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;
import com.google.android.material.textfield.TextInputEditText;

public class AdminLoginActivity extends AppCompatActivity {

    private TextInputEditText adminIdEditText, passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        adminIdEditText = findViewById(R.id.admin_id_edit_text);
        passwordEditText = findViewById(R.id.admin_password_edit_text);
        loginButton = findViewById(R.id.admin_login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String adminId = adminIdEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Simple check for demo purposes
                if (adminId.equals("admin") && password.equals("admin123")) {
                    Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AdminLoginActivity.this, "Invalid Admin Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}