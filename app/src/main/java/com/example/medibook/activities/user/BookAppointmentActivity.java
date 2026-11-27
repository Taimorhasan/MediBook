package com.example.medibook.activities.user;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;
import com.example.medibook.adapters.DoctorAdapter;
import com.example.medibook.models.Appointment;
import com.example.medibook.models.Doctor;
import com.example.medibook.models.User;
import com.example.medibook.repositories.AppointmentRepository;
import com.example.medibook.repositories.DoctorRepository;
import com.example.medibook.repositories.NotificationRepository;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.utils.PassView;
import com.example.medibook.utils.StateView;
import com.example.medibook.utils.Ui;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

/** Pick a day the doctor works and a free-looking slot, then send a pending request. */
public class BookAppointmentActivity extends AppCompatActivity {
    public static final String EXTRA_DOCTOR_ID = "doctorId";
    /** Clinic slots offered every working day. */
    private static final String[] SLOTS = {"09:00 AM", "10:30 AM", "12:00 PM", "03:00 PM", "05:00 PM", "07:00 PM"};
    private static final int DAYS_AHEAD = 21;

    private Doctor doctor;
    private String patientName;
    private String day;
    private String slot;
    private StateView state;
    private ChipGroup days, times;
    private TextView summary;
    private MaterialButton confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.book_title);
        toolbar.setNavigationOnClickListener(v -> finish());
        state = new StateView(findViewById(R.id.book_state));
        days = findViewById(R.id.book_days);
        times = findViewById(R.id.book_times);
        View bar = findViewById(R.id.book_bar);
        summary = bar.findViewById(R.id.bar_summary);
        confirm = bar.findViewById(R.id.bar_button);
        confirm.setText(R.string.confirm_booking);
        confirm.setOnClickListener(v -> book());
        load();
    }

    private void load() {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) {
            finish();
            return;
        }
        state.loading();
        new UserRepository().getUser(me.getUid(), new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                patientName = user.getName();
            }

            @Override
            public void onFailure(String error) {
                // Falls back to the auth profile name when booking.
            }
        });
        new DoctorRepository().getDoctor(getIntent().getStringExtra(EXTRA_DOCTOR_ID), new DoctorRepository.DoctorCallback() {
            @Override
            public void onSuccess(Doctor d) {
                if (isFinishing()) return;
                if (!d.isVerified() || !d.isActive()) {
                    state.empty(R.drawable.ic_calendar_x, getString(R.string.doctor_unavailable), null,
                            getString(R.string.back), BookAppointmentActivity.this::finish);
                    return;
                }
                doctor = d;
                state.hide();
                render();
            }

            @Override
            public void onFailure(String error) {
                if (!isFinishing()) state.error(error, BookAppointmentActivity.this::load);
            }
        });
    }

    private void render() {
        findViewById(R.id.book_form).setVisibility(View.VISIBLE);
        findViewById(R.id.book_bar).setVisibility(View.VISIBLE);
        String name = Ui.doctorName(doctor.getName());
        ((TextView) findViewById(R.id.book_doctor)).setText(name);
        ((TextView) findViewById(R.id.book_specialty)).setText(DoctorAdapter.specialtyLine(doctor));
        TextView meta = findViewById(R.id.book_meta);
        String metaText = DoctorAdapter.meta(meta, doctor);
        meta.setText(metaText);
        meta.setVisibility(metaText.isEmpty() ? View.GONE : View.VISIBLE);
        View avatar = findViewById(R.id.book_avatar);
        Ui.avatar(avatar.findViewById(R.id.avatar_image), avatar.findViewById(R.id.avatar_initials),
                doctor.getProfileImage(), name);

        SimpleDateFormat label = new SimpleDateFormat("EEE d MMM", Locale.US);
        SimpleDateFormat weekday = new SimpleDateFormat("EEEE", Locale.US);
        boolean anyDay = doctor.getAvailableDays() == null || doctor.getAvailableDays().isEmpty();
        Calendar cal = Calendar.getInstance();
        days.removeAllViews();
        for (int i = 0; i < DAYS_AHEAD; i++) {
            String iso = Ui.isoDay(cal.getTime());
            boolean works = anyDay || Ui.worksOn(doctor.getAvailableDays(), weekday.format(cal.getTime()));
            Chip chip = new Chip(this);
            chip.setId(View.generateViewId());
            chip.setText(label.format(cal.getTime()));
            chip.setTag(iso);
            chip.setCheckable(true);
            chip.setEnabled(works);
            days.addView(chip);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        days.setOnCheckedStateChangeListener((g, ids) -> {
            day = ids.isEmpty() ? null : (String) g.findViewById(ids.get(0)).getTag();
            slot = null;
            renderSlots();
            updateSummary();
        });
        renderSlots();
        updateSummary();
    }

    /** Slots for the chosen day; today's past slots are disabled. */
    private void renderSlots() {
        times.removeAllViews();
        long now = System.currentTimeMillis();
        for (String s : SLOTS) {
            Chip chip = new Chip(this);
            chip.setId(View.generateViewId());
            chip.setText(s);
            chip.setTag(s);
            chip.setCheckable(true);
            Appointment probe = new Appointment();
            probe.setDate(day);
            probe.setTime(s);
            chip.setEnabled(day != null && Ui.when(probe) > now);
            times.addView(chip);
        }
        times.setOnCheckedStateChangeListener((g, ids) -> {
            slot = ids.isEmpty() ? null : (String) g.findViewById(ids.get(0)).getTag();
            updateSummary();
        });
    }

    private void updateSummary() {
        boolean ready = day != null && slot != null;
        summary.setVisibility(View.VISIBLE);
        summary.setText(ready ? getString(R.string.booking_summary, Ui.dayLong(day), slot) : getString(R.string.booking_pick_both));
        confirm.setEnabled(ready);
    }

    private void book() {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null || doctor == null || day == null || slot == null) return;
        String name = !Ui.blank(patientName) ? patientName
                : !Ui.blank(me.getDisplayName()) ? me.getDisplayName() : getString(R.string.label_patient);
        CharSequence notes = ((EditText) findViewById(R.id.book_notes)).getText();
        long now = System.currentTimeMillis();

        Appointment a = new Appointment();
        a.setAppointmentId(UUID.randomUUID().toString());
        a.setPatientId(me.getUid());
        a.setPatientName(name);
        a.setDoctorId(doctor.getDoctorId());
        a.setDoctorName(doctor.getName());
        a.setDoctorSpecialty(doctor.getSpecialty());
        a.setHospitalId(doctor.getHospitalId());
        a.setDate(day);
        a.setTime(slot);
        a.setStatus("pending");
        a.setNotes(notes == null ? null : notes.toString().trim());
        a.setCreatedBy(me.getUid());
        a.setCreatedAt(now);
        a.setUpdatedAt(now);

        confirm.setEnabled(false);
        new AppointmentRepository().bookAppointment(a, new AppointmentRepository.AppointmentCallback() {
            @Override
            public void onSuccess(Appointment booked) {
                notifyBothSides(a);
                showDone(a);
            }

            @Override
            public void onFailure(String error) {
                confirm.setEnabled(true);
                Ui.snack(confirm, getString(R.string.booking_failed, error));
            }
        });
    }

    private void notifyBothSides(Appointment a) {
        NotificationRepository repo = new NotificationRepository();
        NotificationRepository.NotificationCallback ignore = new NotificationRepository.NotificationCallback() {
            @Override public void onSuccess() {}
            @Override public void onFailure(String error) {}
        };
        String dayText = Ui.dayShort(a.getDate());
        repo.sendNotification(a.getPatientId(), getString(R.string.notif_booked_title),
                getString(R.string.notif_booked_body, Ui.doctorName(a.getDoctorName()), dayText, a.getTime()),
                "appointment_booked", ignore);
        repo.sendNotification(a.getDoctorId(), getString(R.string.notif_request_title),
                getString(R.string.notif_request_body, a.getPatientName(), dayText, a.getTime()),
                "appointment_request", ignore);
    }

    private void showDone(Appointment a) {
        findViewById(R.id.book_form).setVisibility(View.GONE);
        findViewById(R.id.book_done).setVisibility(View.VISIBLE);
        PassView.bind(findViewById(R.id.book_pass), a, false, false, doctor.getProfileImage(), DoctorAdapter.specialtyLine(doctor));
        summary.setVisibility(View.GONE);
        confirm.setText(R.string.close);
        confirm.setEnabled(true);
        confirm.setOnClickListener(v -> finish());
    }
}
