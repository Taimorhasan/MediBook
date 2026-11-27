package com.example.medibook.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;
import com.example.medibook.activities.doctor.EditDoctorProfileActivity;
import com.example.medibook.models.Doctor;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.DoctorDetailRepository;
import com.example.medibook.utils.RoleRouter;
import com.example.medibook.utils.SessionManager;
import com.example.medibook.utils.Ui;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

/**
 * Creates a patient account, or with EXTRA_DOCTOR a doctor account that waits for an admin to
 * verify it before it can sign in.
 */
public class SignupActivity extends AppCompatActivity {
    public static final String EXTRA_DOCTOR = "doctor";

    private final AuthRepository auth = new AuthRepository();
    private boolean doctor;
    private View overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        doctor = getIntent().getBooleanExtra(EXTRA_DOCTOR, false);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        overlay = findViewById(R.id.progress_overlay);

        ((AutoCompleteTextView) findViewById(R.id.specialty_input)).setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, EditDoctorProfileActivity.SPECIALTIES));

        MaterialCardView brand = findViewById(R.id.brand);
        // Compact here: the title alone, so no tear and no notches.
        brand.findViewById(R.id.brand_segments).setVisibility(View.GONE);
        brand.findViewById(R.id.brand_tear).setVisibility(View.GONE);
        if (savedInstanceState == null) Ui.rise(brand);

        MaterialButtonToggleGroup roles = findViewById(R.id.role_toggle);
        roles.check(doctor ? R.id.role_doctor : R.id.role_patient);
        roles.addOnButtonCheckedListener((group, id, checked) -> {
            if (checked) showRole(id == R.id.role_doctor);
        });
        showRole(doctor);

        findViewById(R.id.signup_button).setOnClickListener(v -> submit());
        findViewById(R.id.signin_redirect).setOnClickListener(v -> finish());
    }

    /** Doctors add specialty and experience, and wait for an admin to verify them. */
    private void showRole(boolean asDoctor) {
        doctor = asDoctor;
        ((TextView) findViewById(R.id.brand).findViewById(R.id.brand_title))
                .setText(asDoctor ? R.string.brand_title_doctor : R.string.brand_title_signup);
        ((TextView) findViewById(R.id.signup_subtitle))
                .setText(asDoctor ? R.string.doctor_signup_subtitle : R.string.signup_subtitle);
        findViewById(R.id.doctor_fields).setVisibility(asDoctor ? View.VISIBLE : View.GONE);
    }

    private void submit() {
        String name = text(R.id.name_input), email = text(R.id.email_input), phone = text(R.id.phone_input);
        String password = text(R.id.password_input), confirm = text(R.id.confirm_input);
        String specialty = text(R.id.specialty_input), experience = text(R.id.experience_input);

        boolean ok = check(R.id.name_layout, name.length() >= 2 && !name.matches(".*\\d.*"), R.string.error_required)
                & check(R.id.email_layout, Patterns.EMAIL_ADDRESS.matcher(email).matches(), R.string.error_email)
                & check(R.id.phone_layout, phone.matches("03\\d{9}"), R.string.error_phone)
                & check(R.id.password_layout, password.length() >= 6, R.string.error_password_short)
                & check(R.id.confirm_layout, confirm.equals(password), R.string.error_password_match);
        if (doctor) {
            ok &= check(R.id.specialty_layout, !specialty.isEmpty(), R.string.error_required)
                    & check(R.id.experience_layout, !experience.isEmpty(), R.string.error_required);
        }
        if (!ok) return;

        overlay.setVisibility(View.VISIBLE);
        auth.signUp(name, email, phone, password, doctor ? "doctor" : "patient", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                if (doctor) {
                    createDoctorProfile(user, name, email, phone, specialty, experience);
                } else {
                    RoleRouter.route(SignupActivity.this, user.getUid(), SignupActivity.this::fail);
                }
            }

            @Override
            public void onFailure(String error) {
                fail(getString(R.string.signup_failed, error));
            }
        });
    }

    private void createDoctorProfile(FirebaseUser user, String name, String email, String phone,
                                     String specialty, String experience) {
        Doctor d = new Doctor(user.getUid(), name, specialty, "", phone, email, experience, null, "", true);
        d.setVerified(false);
        new DoctorDetailRepository().saveDoctorProfile(user.getUid(), d, new DoctorDetailRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                // Doctors can't use the app until an admin verifies them.
                auth.signOut();
                new SessionManager(SignupActivity.this).clearSession();
                startActivity(new Intent(SignupActivity.this, UnifiedLoginActivity.class)
                        .putExtra(UnifiedLoginActivity.EXTRA_MESSAGE, getString(R.string.doctor_created))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }

            @Override
            public void onFailure(String error) {
                auth.signOut();
                fail(getString(R.string.signup_failed, error));
            }
        });
    }

    private void fail(String message) {
        overlay.setVisibility(View.GONE);
        Ui.snack(findViewById(R.id.signup_button), message);
    }

    private boolean check(int layoutId, boolean valid, int errorRes) {
        ((TextInputLayout) findViewById(layoutId)).setError(valid ? null : getString(errorRes));
        return valid;
    }

    private String text(int id) {
        CharSequence s = ((EditText) findViewById(id)).getText();
        return s == null ? "" : s.toString().trim();
    }
}
