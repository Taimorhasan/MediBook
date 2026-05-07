package com.example.medibook.activities.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.medibook.R;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.services.CloudinaryService;
import com.google.firebase.auth.FirebaseAuth;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EditPatientProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private EditText nameEditText, phoneEditText, ageEditText, bioEditText;
    private Spinner genderSpinner, bloodGroupSpinner;
    private Button uploadPhotoButton, saveButton;
    private ProgressBar progressBar;
    
    private UserRepository userRepository;
    private CloudinaryService cloudinaryService;
    private String currentUserId;
    private Uri selectedImageUri;
    private String profileImageUrl = "";

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_patient_profile);

        userRepository = new UserRepository();
        cloudinaryService = new CloudinaryService();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize views
        profileImageView = findViewById(R.id.profile_image);
        nameEditText = findViewById(R.id.name_edit_text);
        phoneEditText = findViewById(R.id.phone_edit_text);
        ageEditText = findViewById(R.id.age_edit_text);
        bioEditText = findViewById(R.id.bio_edit_text);
        genderSpinner = findViewById(R.id.gender_spinner);
        bloodGroupSpinner = findViewById(R.id.blood_group_spinner);
        uploadPhotoButton = findViewById(R.id.upload_photo_button);
        saveButton = findViewById(R.id.save_button);
        progressBar = findViewById(R.id.progress_bar);

        // Load current profile data
        loadProfileData();

        // Upload photo button
        uploadPhotoButton.setOnClickListener(v -> pickImage());

        // Save button
        saveButton.setOnClickListener(v -> saveProfile());

        // Profile image click to enlarge
        profileImageView.setOnClickListener(v -> pickImage());
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Show preview
                Glide.with(this)
                    .load(selectedImageUri)
                    .circleCrop()
                    .into(profileImageView);
                
                uploadImageToCloudinary(selectedImageUri);
            }
        }
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        progressBar.setVisibility(View.VISIBLE);
        uploadPhotoButton.setEnabled(false);

        // Get file from URI
        String imagePath = getRealPathFromURI(imageUri);
        if (imagePath != null) {
            File imageFile = new File(imagePath);
            
            cloudinaryService.uploadImage(imageFile, "patient_profiles/" + currentUserId, 
                new CloudinaryService.ImageUploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        profileImageUrl = imageUrl;
                        progressBar.setVisibility(View.GONE);
                        uploadPhotoButton.setEnabled(true);
                        Toast.makeText(EditPatientProfileActivity.this, "Photo uploaded successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        progressBar.setVisibility(View.GONE);
                        uploadPhotoButton.setEnabled(true);
                        Toast.makeText(EditPatientProfileActivity.this, "Failed to upload photo: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    private String getRealPathFromURI(Uri uri) {
        try {
            String[] projection = {android.provider.MediaStore.Images.ImageColumns.DATA};
            android.database.Cursor cursor = managedQuery(uri, projection, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.ImageColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            return null;
        }
    }

    private void loadProfileData() {
        userRepository.getUser(currentUserId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(com.example.medibook.models.User user) {
                if (user != null) {
                    nameEditText.setText(user.getName() != null ? user.getName() : "");
                    phoneEditText.setText(user.getPhone() != null ? user.getPhone() : "");
                    ageEditText.setText(user.getAge() != null ? user.getAge() : "");
                    bioEditText.setText(user.getBio() != null ? user.getBio() : "");
                    
                    if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                        profileImageUrl = user.getProfileImage();
                        Glide.with(EditPatientProfileActivity.this)
                            .load(profileImageUrl)
                            .circleCrop()
                            .into(profileImageView);
                    }

                    // Set spinners if data exists
                    if (user.getGender() != null) {
                        setSpinnerSelection(genderSpinner, user.getGender());
                    }
                    if (user.getBloodGroup() != null) {
                        setSpinnerSelection(bloodGroupSpinner, user.getBloodGroup());
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(EditPatientProfileActivity.this, "Failed to load profile: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void saveProfile() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();
        String bio = bioEditText.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();
        String bloodGroup = bloodGroupSpinner.getSelectedItem().toString();

        if (name.isEmpty()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        // Create update map
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("age", age);
        updates.put("bio", bio);
        updates.put("gender", gender);
        updates.put("bloodGroup", bloodGroup);
        if (!profileImageUrl.isEmpty()) {
            updates.put("profileImage", profileImageUrl);
        }

        userRepository.updateUserFields(currentUserId, updates, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(com.example.medibook.models.User user) {
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                Toast.makeText(EditPatientProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                Toast.makeText(EditPatientProfileActivity.this, "Failed to update profile: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
