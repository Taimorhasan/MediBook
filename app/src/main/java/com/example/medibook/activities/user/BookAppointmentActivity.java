package com.example.medibook.activities.user;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medibook.R;
import com.example.medibook.models.Appointment;
import com.example.medibook.repositories.AppointmentRepository;
import com.example.medibook.repositories.NotificationRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BookAppointmentActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "MediBookPrefs";
    private String selectedDate = "";
    private String selectedTime = "";
    private String doctorId;
    private String doctorName;

    private AppointmentRepository appointmentRepository;
    private NotificationRepository notificationRepository;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        // Initialize repositories
        appointmentRepository = new AppointmentRepository();
        notificationRepository = new NotificationRepository();

        // Get current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get doctor info from intent
        doctorId = getIntent().getStringExtra("doctorId");
        doctorName = getIntent().getStringExtra("doctorName");

        if (doctorId == null || doctorName == null) {
            Toast.makeText(this, "Doctor information not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up date picker
        DatePicker datePicker = findViewById(R.id.date_picker);
        Calendar calendar = Calendar.getInstance();
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, monthOfYear, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                selectedDate = sdf.format(selectedCalendar.getTime());
            }
        });

        // Set default date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = sdf.format(calendar.getTime());

        // Set up time slot selection
        ChipGroup timeSlotGroup = findViewById(R.id.time_slot_group);
        timeSlotGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                Chip selectedChip = findViewById(checkedId);
                if (selectedChip != null) {
                    selectedTime = selectedChip.getText().toString();
                }
            }
        });

        Button confirmButton = findViewById(R.id.confirm_appointment_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookAppointment();
            }
        });
    }

    private void bookAppointment() {
        if (currentUser == null) {
            Toast.makeText(this, "Please login to book appointment", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create appointment object
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(java.util.UUID.randomUUID().toString());
        appointment.setPatientId(currentUser.getUid());
        appointment.setDoctorId(doctorId);
        appointment.setDoctorName(doctorName);
        appointment.setDate(selectedDate);
        appointment.setTime(selectedTime);
        appointment.setStatus("pending");
        appointment.setCreatedAt(System.currentTimeMillis());
        appointment.setUpdatedAt(System.currentTimeMillis());

        // Show loading
        findViewById(R.id.confirm_appointment_button).setEnabled(false);

        // Book appointment
        appointmentRepository.bookAppointment(appointment, new AppointmentRepository.AppointmentCallback() {
            @Override
            public void onSuccess(Appointment bookedAppointment) {
                // Send notification to patient
                notificationRepository.sendNotification(
                    currentUser.getUid(),
                    "Appointment Booked",
                    "Your appointment with Dr. " + doctorName + " on " + selectedDate + " at " + selectedTime + " has been booked successfully.",
                    new NotificationRepository.NotificationCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(BookAppointmentActivity.this,
                                    "Appointment booked successfully!", Toast.LENGTH_LONG).show();
                                finish();
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(BookAppointmentActivity.this,
                                    "Appointment booked but notification failed: " + error, Toast.LENGTH_LONG).show();
                                finish();
                            });
                        }
                    }
                );
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    findViewById(R.id.confirm_appointment_button).setEnabled(true);
                    Toast.makeText(BookAppointmentActivity.this,
                        "Failed to book appointment: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
