package com.example.medibook.activities.user;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.models.User;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, phoneTextView;
    private AuthRepository authRepository;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize repositories
        authRepository = new AuthRepository();
        userRepository = new UserRepository();

        // Initialize views
        nameTextView = findViewById(R.id.profile_name);
        emailTextView = findViewById(R.id.profile_email);
        phoneTextView = findViewById(R.id.profile_phone);

        // Load user profile
        loadUserProfile();
    }

    private void loadUserProfile() {
        String userId = authRepository.getCurrentUser().getUid();
        userRepository.getUser(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                nameTextView.setText(user.getName());
                emailTextView.setText(user.getEmail());
                phoneTextView.setText(user.getPhone());
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}