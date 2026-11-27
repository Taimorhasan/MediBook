package com.example.medibook.activities.doctor;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.medibook.R;
import com.example.medibook.models.Doctor;
import com.example.medibook.models.User;
import com.example.medibook.repositories.DoctorDetailRepository;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.utils.PhotoPicker;
import com.example.medibook.utils.Ui;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Doctors edit their public profile and working days; name and phone are mirrored to users/{uid}. */
public class EditDoctorProfileActivity extends AppCompatActivity {
    public static final String[] SPECIALTIES = {"General Medicine", "Cardiology", "Dermatology", "Pediatrics",
            "Neurology", "Orthopedics", "Gynecology", "Psychiatry", "ENT", "Ophthalmology", "Dentistry", "Urology"};

    private final DoctorDetailRepository repo = new DoctorDetailRepository();
    private String uid;
    private String photoUrl;
    private EditText name, experience, fee, phone, degree, license, bio;
    private AutoCompleteTextView specialty;
    private ChipGroup days;
    private MaterialButton save;
    private View avatar, progress;
    private PhotoPicker picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_edit_doctor_profile);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.edit_profile);
        toolbar.setNavigationOnClickListener(v -> finish());

        name = findViewById(R.id.doc_name_input);
        specialty = findViewById(R.id.doc_specialty_input);
        experience = findViewById(R.id.doc_experience_input);
        fee = findViewById(R.id.doc_fee_input);
        phone = findViewById(R.id.doc_phone_input);
        degree = findViewById(R.id.doc_degree_input);
        license = findViewById(R.id.doc_license_input);
        bio = findViewById(R.id.doc_bio_input);
        days = findViewById(R.id.doc_days_input);
        avatar = findViewById(R.id.photo_avatar);
        progress = findViewById(R.id.photo_progress);
        specialty.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, SPECIALTIES));
        for (String day : Ui.WEEK) {
            Chip chip = new Chip(this);
            chip.setText(day.substring(0, 3));
            chip.setTag(day);
            chip.setCheckable(true);
            chip.setContentDescription(day);
            days.addView(chip);
        }

        save = findViewById(R.id.edit_bar).findViewById(R.id.bar_button);
        save.setText(R.string.save_changes);
        save.setOnClickListener(v -> save());
        save.setEnabled(false);

        picker = new PhotoPicker(this, "doctor_profiles/" + uid, new PhotoPicker.Listener() {
            @Override
            public void onUploading(Uri preview) {
                progress.setVisibility(View.VISIBLE);
                save.setEnabled(false);
                ImageView image = avatar.findViewById(R.id.avatar_image);
                image.setVisibility(View.VISIBLE);
                Glide.with(image).load(preview).circleCrop().into(image);
            }

            @Override
            public void onUploaded(String url) {
                photoUrl = url;
                progress.setVisibility(View.GONE);
                save.setEnabled(true);
                Ui.snack(save, R.string.photo_uploaded);
            }

            @Override
            public void onFailed(String error) {
                progress.setVisibility(View.GONE);
                save.setEnabled(true);
                Ui.snack(save, getString(R.string.photo_failed, error));
            }
        });
        findViewById(R.id.photo_change).setOnClickListener(v -> picker.pick());
        avatar.setOnClickListener(v -> picker.pick());

        repo.getDoctorProfile(uid, new DoctorDetailRepository.DoctorCallback() {
            @Override
            public void onSuccess(Doctor d) {
                name.setText(d.getName());
                specialty.setText(d.getSpecialty(), false);
                experience.setText(d.getExperience() == null ? "" : d.getExperience().replaceAll("[^0-9]", ""));
                fee.setText(d.getConsultationFee() == null ? "" : d.getConsultationFee().replaceAll("[^0-9]", ""));
                phone.setText(d.getPhone());
                degree.setText(d.getDegree());
                license.setText(d.getLicenseNumber());
                bio.setText(d.getBio());
                for (int i = 0; i < days.getChildCount(); i++) {
                    Chip chip = (Chip) days.getChildAt(i);
                    chip.setChecked(Ui.worksOn(d.getAvailableDays(), (String) chip.getTag()));
                }
                photoUrl = d.getProfileImage();
                Ui.avatar(avatar.findViewById(R.id.avatar_image), avatar.findViewById(R.id.avatar_initials), photoUrl, d.getName());
                save.setEnabled(true);
            }

            @Override
            public void onFailure(String error) {
                Ui.snack(save, getString(R.string.error_generic, error));
            }
        });
    }

    private void save() {
        String n = text(name), s = specialty.getText().toString().trim(), p = text(phone);
        boolean ok = check(R.id.doc_name_layout, n.length() >= 2, R.string.error_required)
                & check(R.id.doc_specialty_layout, !s.isEmpty(), R.string.error_required)
                & check(R.id.doc_phone_layout, p.isEmpty() || p.matches("03\\d{9}"), R.string.error_phone);
        if (!ok) return;

        List<String> working = new ArrayList<>();
        for (int i = 0; i < days.getChildCount(); i++) {
            Chip chip = (Chip) days.getChildAt(i);
            if (chip.isChecked()) working.add((String) chip.getTag());
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", n);
        updates.put("specialty", s);
        updates.put("experience", text(experience));
        updates.put("consultationFee", text(fee));
        updates.put("phone", p);
        updates.put("degree", text(degree));
        updates.put("licenseNumber", text(license));
        updates.put("bio", text(bio));
        updates.put("availableDays", working);
        if (!Ui.blank(photoUrl)) updates.put("profileImage", photoUrl);

        save.setEnabled(false);
        repo.updateDoctorProfile(uid, updates, new DoctorDetailRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                Map<String, Object> mirror = new HashMap<>();
                mirror.put("name", n);
                mirror.put("phone", p);
                if (!Ui.blank(photoUrl)) mirror.put("profileImage", photoUrl);
                new UserRepository().updateUserFields(uid, mirror, new UserRepository.UserCallback() {
                    @Override public void onSuccess(User user) { finish(); }
                    @Override public void onFailure(String error) { finish(); } // doctor profile is the source of truth
                });
            }

            @Override
            public void onFailure(String error) {
                save.setEnabled(true);
                Ui.snack(save, getString(R.string.error_generic, error));
            }
        });
    }

    private boolean check(int layoutId, boolean valid, int errorRes) {
        ((TextInputLayout) findViewById(layoutId)).setError(valid ? null : getString(errorRes));
        return valid;
    }

    private static String text(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
