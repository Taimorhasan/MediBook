package com.example.medibook.activities.user;

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
import com.example.medibook.models.User;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.utils.PhotoPicker;
import com.example.medibook.utils.Ui;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import java.util.HashMap;
import java.util.Map;

/** Patients (and admins) edit the profile stored in users/{uid}. */
public class EditPatientProfileActivity extends AppCompatActivity {
    private String uid;
    private String photoUrl;
    private EditText name, phone, age, bio;
    private AutoCompleteTextView gender, blood;
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
        setContentView(R.layout.activity_edit_patient_profile);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.edit_profile);
        toolbar.setNavigationOnClickListener(v -> finish());

        name = findViewById(R.id.edit_name);
        phone = findViewById(R.id.edit_phone);
        age = findViewById(R.id.edit_age);
        bio = findViewById(R.id.edit_bio);
        gender = findViewById(R.id.edit_gender);
        blood = findViewById(R.id.edit_blood);
        gender.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.gender_array)));
        blood.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.blood_group_array)));
        avatar = findViewById(R.id.photo_avatar);
        progress = findViewById(R.id.photo_progress);

        save = findViewById(R.id.edit_bar).findViewById(R.id.bar_button);
        save.setText(R.string.save_changes);
        save.setOnClickListener(v -> save());
        save.setEnabled(false);

        picker = new PhotoPicker(this, "patient_profiles/" + uid, new PhotoPicker.Listener() {
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

        new UserRepository().getUser(uid, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User u) {
                name.setText(u.getName());
                phone.setText(u.getPhone());
                age.setText(u.getAge());
                bio.setText(u.getBio());
                if (!Ui.blank(u.getGender())) gender.setText(u.getGender(), false);
                if (!Ui.blank(u.getBloodGroup())) blood.setText(u.getBloodGroup(), false);
                photoUrl = u.getProfileImage();
                Ui.avatar(avatar.findViewById(R.id.avatar_image), avatar.findViewById(R.id.avatar_initials), photoUrl, u.getName());
                save.setEnabled(true);
            }

            @Override
            public void onFailure(String error) {
                Ui.snack(save, getString(R.string.error_generic, error));
            }
        });
    }

    private void save() {
        String n = text(name), p = text(phone), a = text(age);
        boolean ok = check(R.id.edit_name_layout, n.length() >= 2, R.string.error_required)
                & check(R.id.edit_phone_layout, p.isEmpty() || p.matches("03\\d{9}"), R.string.error_phone)
                & check(R.id.edit_age_layout, a.isEmpty() || Integer.parseInt(a) <= 120, R.string.error_age);
        if (!ok) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", n);
        updates.put("phone", p);
        updates.put("age", a);
        updates.put("bio", text(bio));
        updates.put("gender", gender.getText().toString());
        updates.put("bloodGroup", blood.getText().toString());
        updates.put("updatedAt", System.currentTimeMillis());
        if (!Ui.blank(photoUrl)) updates.put("profileImage", photoUrl);

        save.setEnabled(false);
        new UserRepository().updateUserFields(uid, updates, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                finish();
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
