package com.example.medibook.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;
import com.example.medibook.adapters.DoctorAdapter;
import com.example.medibook.models.Doctor;
import com.example.medibook.repositories.DoctorRepository;
import com.example.medibook.utils.StateView;
import com.example.medibook.utils.Ui;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.List;

/** A doctor's public profile, with booking. Admins open it in preview mode, without booking. */
public class DoctorProfileActivity extends AppCompatActivity {
    public static final String EXTRA_DOCTOR_ID = "doctorId";
    public static final String EXTRA_PREVIEW = "preview";

    private StateView state;
    private String doctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        doctorId = getIntent().getStringExtra(EXTRA_DOCTOR_ID);
        state = new StateView(findViewById(R.id.profile_state));
        load();
    }

    private void load() {
        state.loading();
        new DoctorRepository().getDoctor(doctorId, new DoctorRepository.DoctorCallback() {
            @Override
            public void onSuccess(Doctor doctor) {
                if (isFinishing()) return;
                state.hide();
                render(doctor);
            }

            @Override
            public void onFailure(String error) {
                if (!isFinishing()) state.error(error, DoctorProfileActivity.this::load);
            }
        });
    }

    private void render(Doctor d) {
        findViewById(R.id.profile_scroll).setVisibility(View.VISIBLE);
        String name = Ui.doctorName(d.getName());
        ((TextView) findViewById(R.id.doc_name)).setText(name);
        ((TextView) findViewById(R.id.doc_specialty)).setText(DoctorAdapter.specialtyLine(d));
        View avatar = findViewById(R.id.doc_avatar);
        Ui.avatar(avatar.findViewById(R.id.avatar_image), avatar.findViewById(R.id.avatar_initials), d.getProfileImage(), name);
        if (d.isVerified()) {
            TextView verified = findViewById(R.id.doc_verified);
            verified.setVisibility(View.VISIBLE);
            Ui.pill(verified, getString(R.string.verified_doctor), R.color.mb_ok, R.color.mb_ok_bg, R.color.mb_ok_bg);
        }

        String years = d.getExperience() == null ? "" : d.getExperience().replaceAll("[^0-9]", "");
        ((TextView) findViewById(R.id.doc_experience)).setText(years.isEmpty() ? "—" : years);
        String fee = d.getConsultationFee() == null ? "" : d.getConsultationFee().replaceAll("[^0-9]", "");
        ((TextView) findViewById(R.id.doc_fee)).setText(fee.isEmpty() ? "—" : getString(R.string.fee_pkr, fee));
        List<String> days = d.getAvailableDays();
        boolean anyDay = days == null || days.isEmpty();
        ((TextView) findViewById(R.id.doc_days_count)).setText(anyDay ? "7/7" : days.size() + "/7");

        ((TextView) findViewById(R.id.doc_bio)).setText(Ui.blank(d.getBio()) ? getString(R.string.no_bio) : d.getBio());

        ChipGroup chips = findViewById(R.id.doc_days);
        chips.removeAllViews();
        for (String day : Ui.WEEK) {
            boolean on = anyDay || Ui.worksOn(days, day);
            Chip chip = new Chip(this);
            chip.setText(day.substring(0, 3));
            chip.setCheckable(true);
            chip.setChecked(on);
            chip.setClickable(false);
            chip.setEnabled(on);
            chip.setContentDescription(day);
            chips.addView(chip);
        }

        LinearLayout details = findViewById(R.id.doc_details);
        details.removeAllViews();
        Ui.detailRow(details, getString(R.string.hospital), d.getHospitalName());
        Ui.detailRow(details, getString(R.string.degree), d.getDegree());
        Ui.detailRow(details, getString(R.string.license), d.getLicenseNumber());

        boolean bookable = d.isVerified() && d.isActive() && !getIntent().getBooleanExtra(EXTRA_PREVIEW, false);
        View bar = findViewById(R.id.profile_bar);
        bar.setVisibility(bookable ? View.VISIBLE : View.GONE);
        MaterialButton book = bar.findViewById(R.id.bar_button);
        book.setText(R.string.book_appointment);
        book.setIconResource(R.drawable.ic_calendar_plus);
        book.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        book.setOnClickListener(v -> startActivity(new Intent(this, BookAppointmentActivity.class)
                .putExtra(BookAppointmentActivity.EXTRA_DOCTOR_ID, d.getDoctorId())));
    }

}
