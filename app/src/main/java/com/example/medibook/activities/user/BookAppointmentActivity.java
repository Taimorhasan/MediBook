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
import com.example.medibook.models.Doctor;
import com.example.medibook.repositories.AppointmentRepository;
import com.example.medibook.repositories.DoctorRepository;
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
    private DoctorRepository doctorRepository;
    private NotificationRepository notificationRepository;
    private FirebaseUser currentUser;
    private Doctor selectedDoctor;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        // Initialize repositories
        appointmentRepository = new AppointmentRepository();
        doctorRepository = new DoctorRepository();
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
        datePicker.setMinDate(startOfTodayMillis());
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

        confirmButton = findViewById(R.id.confirm_appointment_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookAppointment();
            }
        });

        loadDoctorForBooking();
    }

    private void loadDoctorForBooking() {
        confirmButton.setEnabled(false);
        doctorRepository.getDoctor(doctorId, new DoctorRepository.DoctorCallback() {
            @Override
            public void onSuccess(Doctor doctor) {
                runOnUiThread(() -> {
                    selectedDoctor = doctor;
                    if (doctor == null || !doctor.isActive() || !doctor.isVerified()) {
                        Toast.makeText(BookAppointmentActivity.this,
                                "This doctor is not available for booking", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    doctorName = doctor.getName();
                    confirmButton.setEnabled(true);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(BookAppointmentActivity.this,
                            "Unable to verify doctor: " + error, Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    private void bookAppointment() {
        if (currentUser == null) {
            Toast.makeText(this, "Please login to book appointment", Toast.LENGTH_SHORT).show();
            return;
        }

        String validationError = validateBookingInput();
        if (validationError != null) {
            Toast.makeText(this, validationError, Toast.LENGTH_SHORT).show();
            return;
        }

        // Create appointment object
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(java.util.UUID.randomUUID().toString());
        appointment.setPatientId(currentUser.getUid());
        appointment.setPatientName(getCurrentPatientName());
        appointment.setDoctorId(doctorId);
        appointment.setDoctorName(doctorName);
        appointment.setHospitalId(selectedDoctor != null ? selectedDoctor.getHospitalId() : null);
        appointment.setDate(selectedDate);
        appointment.setTime(selectedTime);
        appointment.setStatus("pending");
        appointment.setCreatedBy(currentUser.getUid());
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
                    "Your appointment with Dr. " + doctorName + " on " + selectedDate + " at " + selectedTime + " has been requested.",
                    "appointment_booked",
                    new NotificationRepository.NotificationCallback() {
                        @Override
                        public void onSuccess() {
                            notifyDoctorAndFinish();
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

    private void notifyDoctorAndFinish() {
        notificationRepository.sendNotification(
                doctorId,
                "New Appointment Request",
                getCurrentPatientName() + " requested an appointment on " + selectedDate + " at " + selectedTime + ".",
                "appointment_request",
                new NotificationRepository.NotificationCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(BookAppointmentActivity.this,
                                    "Appointment requested successfully!", Toast.LENGTH_LONG).show();
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(BookAppointmentActivity.this,
                                    "Appointment requested, but doctor alert failed: " + error, Toast.LENGTH_LONG).show();
                            finish();
                        });
                    }
                }
        );
    }

    private String validateBookingInput() {
        if (doctorId == null || doctorId.trim().isEmpty()) return "Doctor information is missing";
        if (doctorName == null || doctorName.trim().isEmpty()) return "Doctor name is missing";
        if (selectedDoctor == null) return "Doctor details are still loading";
        if (!selectedDoctor.isActive() || !selectedDoctor.isVerified()) return "This doctor is not available for booking";
        if (selectedDate == null || selectedDate.trim().isEmpty()) return "Please select a date";
        if (selectedTime == null || selectedTime.trim().isEmpty()) return "Please select a time slot";
        if (isPastDate(selectedDate)) return "Please select today or a future date";
        if (!isDoctorAvailableOnSelectedDay()) return "Doctor is not available on the selected day";
        return null;
    }

    private boolean isPastDate(String dateValue) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date selected = sdf.parse(dateValue);
            Date today = sdf.parse(sdf.format(new Date()));
            return selected != null && today != null && selected.before(today);
        } catch (Exception e) {
            return true;
        }
    }

    private boolean isDoctorAvailableOnSelectedDay() {
        if (selectedDoctor == null || selectedDoctor.getAvailableDays() == null || selectedDoctor.getAvailableDays().isEmpty()) {
            return true;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date selected = sdf.parse(selectedDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selected);
            String dayName = new SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.getTime());
            for (String availableDay : selectedDoctor.getAvailableDays()) {
                if (availableDay != null && availableDay.equalsIgnoreCase(dayName)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    private long startOfTodayMillis() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today.getTimeInMillis();
    }

    private String getCurrentPatientName() {
        if (currentUser == null) return "";
        String displayName = currentUser.getDisplayName();
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName.trim();
        }
        String email = currentUser.getEmail();
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return "Patient";
    }
}
