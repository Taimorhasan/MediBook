package com.example.medibook.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.adapters.AppointmentAdapter;
import com.example.medibook.models.Appointment;
import com.example.medibook.repositories.AppointmentRepository;
import com.example.medibook.repositories.NotificationRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;

public class UserHomeActivity extends AppCompatActivity implements AppointmentAdapter.OnAppointmentClickListener {

    private LinearLayout navHome;
    private LinearLayout navBookings;
    private LinearLayout navAlerts;
    private LinearLayout navProfile;

    // Appointment related views
    private RecyclerView appointmentsRecyclerView;
    private ProgressBar appointmentsLoadingProgress;
    private TextView noAppointmentsTextView;
    private TextView upcomingAppointmentsTitle;
    private TextView viewAllSpecialties;
    private ChipGroup specialtiesChipGroup;
    private TextInputEditText dashboardSearchEditText;
    private LinearLayout searchSuggestionsLayout;

    // Data
    private List<Appointment> appointments;
    private AppointmentAdapter appointmentAdapter;
    private AppointmentRepository appointmentRepository;
    private NotificationRepository notificationRepository;
    private FirebaseUser currentUser;
    private final String[] searchableDoctorTerms = {
            "Dr. Sarah Miller", "Cardiology", "Cardiologist",
            "Dr. Marcus Chen", "Pediatrics", "Pediatrician",
            "Dr. Elena Rodriguez", "Dermatology", "Dermatologist",
            "Dr. James Wilson", "Neurology", "Neurologist",
            "Dr. Aisha Khan", "General Medicine",
            "Orthopedics", "Psychiatry", "Radiology"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        // Initialize repositories and data
        appointmentRepository = new AppointmentRepository();
        notificationRepository = new NotificationRepository();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        appointments = new ArrayList<>();

        // Initialize Bottom Nav Items
        navHome = findViewById(R.id.nav_home_layout);
        navBookings = findViewById(R.id.nav_bookings_layout);
        navAlerts = findViewById(R.id.nav_alerts_layout);
        navProfile = findViewById(R.id.nav_profile_layout);

        // Initialize views
        appointmentsRecyclerView = findViewById(R.id.appointments_recycler_view);
        appointmentsLoadingProgress = findViewById(R.id.appointments_loading_progress);
        noAppointmentsTextView = findViewById(R.id.no_appointments_text);
        upcomingAppointmentsTitle = findViewById(R.id.upcoming_appointments_title);
        viewAllSpecialties = findViewById(R.id.view_all_specialties);
        specialtiesChipGroup = findViewById(R.id.specialties_chip_group);
        dashboardSearchEditText = findViewById(R.id.dashboard_search_edit_text);
        searchSuggestionsLayout = findViewById(R.id.search_suggestions_layout);

        // Set dynamic welcome message
        setWelcomeMessage();

        // Populate specialties
        populateSpecialties();
        setupDashboardSearch();

        // Setup appointments RecyclerView
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointmentAdapter = new AppointmentAdapter(appointments, this, false, true);
        appointmentsRecyclerView.setAdapter(appointmentAdapter);

        // Load appointments
        loadAppointments();

        // 1. Home is current page - maybe refresh
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Toast.makeText(this, "Refreshed home", Toast.LENGTH_SHORT).show();
                loadAppointments(); // Refresh appointments
            });
        }

        // 2. Setup Bookings Navigation
        if (navBookings != null) {
            navBookings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(UserHomeActivity.this, DoctorListActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        }

        // 3. Setup Alerts Navigation
        if (navAlerts != null) {
            navAlerts.setOnClickListener(v -> {
                Intent intent = new Intent(UserHomeActivity.this, AlertsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        // 4. Setup Profile Navigation
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(UserHomeActivity.this, PatientProfileViewActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        // 2. Make "View Details" functional - hide this button since we have RecyclerView now
        // Button removed as it doesn't exist in layout

    }

    private void loadAppointments() {
        if (currentUser == null) {
            showNoAppointments("Please login to view appointments");
            return;
        }

        showAppointmentsLoading(true);

        appointmentRepository.getPatientAppointments(currentUser.getUid(), new AppointmentRepository.AppointmentsCallback() {
            @Override
            public void onSuccess(List<Appointment> loadedAppointments) {
                runOnUiThread(() -> {
                    showAppointmentsLoading(false);
                    appointments.clear();
                    appointments.addAll(loadedAppointments);
                    appointmentAdapter.notifyDataSetChanged();

                    if (appointments.isEmpty()) {
                        showNoAppointments("No upcoming appointments");
                    } else {
                        showAppointmentsList();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    showAppointmentsLoading(false);
                    showNoAppointments("Failed to load appointments: " + error);
                    Toast.makeText(UserHomeActivity.this, "Error loading appointments", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showAppointmentsLoading(boolean show) {
        appointmentsLoadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        appointmentsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        noAppointmentsTextView.setVisibility(View.GONE);
        upcomingAppointmentsTitle.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showNoAppointments(String message) {
        appointmentsLoadingProgress.setVisibility(View.GONE);
        appointmentsRecyclerView.setVisibility(View.GONE);
        noAppointmentsTextView.setVisibility(View.VISIBLE);
        noAppointmentsTextView.setText(message);
        upcomingAppointmentsTitle.setVisibility(View.VISIBLE);
    }

    private void showAppointmentsList() {
        appointmentsLoadingProgress.setVisibility(View.GONE);
        appointmentsRecyclerView.setVisibility(View.VISIBLE);
        noAppointmentsTextView.setVisibility(View.GONE);
        upcomingAppointmentsTitle.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh appointments when returning to this activity
        loadAppointments();
    }

    @Override
    public void onAppointmentClick(Appointment appointment) {
        // Navigate to appointment details
        Toast.makeText(this, "Appointment clicked: " + appointment.getDoctorName(), Toast.LENGTH_SHORT).show();
        // TODO: Navigate to AppointmentDetailActivity when created
    }

    @Override
    public void onCancelAppointment(Appointment appointment) {
        if (appointment == null || appointment.getAppointmentId() == null) {
            Toast.makeText(this, "Appointment information is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        appointmentRepository.cancelAppointment(appointment.getAppointmentId(), new AppointmentRepository.AppointmentCallback() {
            @Override
            public void onSuccess(Appointment updatedAppointment) {
                notifyDoctorAboutCancellation(updatedAppointment);
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(UserHomeActivity.this,
                        "Failed to cancel appointment: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void notifyDoctorAboutCancellation(Appointment appointment) {
        if (appointment == null || appointment.getDoctorId() == null || appointment.getDoctorId().trim().isEmpty()) {
            runOnUiThread(() -> {
                Toast.makeText(UserHomeActivity.this, "Appointment cancelled", Toast.LENGTH_SHORT).show();
                loadAppointments();
            });
            return;
        }

        notificationRepository.sendNotification(
                appointment.getDoctorId(),
                "Appointment Cancelled",
                "A patient cancelled the appointment on " + appointment.getDate() + " at " + appointment.getTime() + ".",
                "appointment_cancelled",
                new NotificationRepository.NotificationCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(UserHomeActivity.this, "Appointment cancelled", Toast.LENGTH_SHORT).show();
                            loadAppointments();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(UserHomeActivity.this,
                                    "Appointment cancelled, alert failed: " + error, Toast.LENGTH_SHORT).show();
                            loadAppointments();
                        });
                    }
                }
        );
    }

    private void setWelcomeMessage() {
        TextView welcomeTextView = findViewById(R.id.welcome_user_text);
        if (welcomeTextView != null && currentUser != null) {
            String greeting = getTimeBasedGreeting();
            String userName = getUserDisplayName();
            String welcomeMessage = greeting + ", " + userName;
            welcomeTextView.setText(welcomeMessage);
        }
    }

    private String getTimeBasedGreeting() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            return "Good Afternoon";
        } else if (hour >= 17 && hour < 21) {
            return "Good Evening";
        } else {
            return "Good Night";
        }
    }

    private String getUserDisplayName() {
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.trim().isEmpty()) {
                // Get first name only
                String[] nameParts = displayName.split("\\s+");
                return nameParts[0];
            }

            String email = currentUser.getEmail();
            if (email != null && email.contains("@")) {
                // Use part before @ as name
                return email.substring(0, email.indexOf("@"));
            }
        }
        return "User";
    }

    private void populateSpecialties() {
        if (specialtiesChipGroup == null) return;

        // Common medical specialties
        String[] specialties = {
            "General Medicine",
            "Cardiology",
            "Dermatology",
            "Neurology",
            "Orthopedics",
            "Pediatrics",
            "Psychiatry",
            "Radiology"
        };

        for (String specialty : specialties) {
            Chip chip = new Chip(this);
            chip.setText(specialty);
            chip.setCheckable(false);
            chip.setChipBackgroundColorResource(R.color.chip_background);
            chip.setTextColor(getColor(R.color.chip_text));
            chip.setChipStrokeWidth(1);
            chip.setChipStrokeColorResource(R.color.chip_stroke);
            chip.setOnClickListener(v -> openDoctorCategory(specialty));

            specialtiesChipGroup.addView(chip);
        }

        if (viewAllSpecialties != null) {
            viewAllSpecialties.setOnClickListener(v -> openDoctorSearch(""));
        }
    }

    private void setupDashboardSearch() {
        if (dashboardSearchEditText == null) return;

        dashboardSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showSearchSuggestions(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dashboardSearchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                openDoctorSearch(dashboardSearchEditText.getText() != null
                        ? dashboardSearchEditText.getText().toString()
                        : "");
                return true;
            }
            return false;
        });

        dashboardSearchEditText.setOnClickListener(v -> {
            showSearchSuggestions(dashboardSearchEditText.getText() != null
                    ? dashboardSearchEditText.getText().toString()
                    : "");
        });

        dashboardSearchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && dashboardSearchEditText.getText() != null
                    && dashboardSearchEditText.getText().toString().trim().length() >= 2) {
                openDoctorSearch(dashboardSearchEditText.getText().toString());
            }
        });
    }

    private void openDoctorSearch(String query) {
        hideSearchSuggestions();
        Intent intent = new Intent(UserHomeActivity.this, DoctorListActivity.class);
        intent.putExtra("searchQuery", query == null ? "" : query.trim());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void openDoctorCategory(String specialty) {
        hideSearchSuggestions();
        Intent intent = new Intent(UserHomeActivity.this, DoctorListActivity.class);
        intent.putExtra("specialty", specialty);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void showSearchSuggestions(String rawQuery) {
        if (searchSuggestionsLayout == null) return;
        String query = rawQuery == null ? "" : rawQuery.trim().toLowerCase();
        searchSuggestionsLayout.removeAllViews();

        int added = 0;
        for (String suggestion : searchableDoctorTerms) {
            if (query.isEmpty() || suggestion.toLowerCase().contains(query)) {
                addSuggestionRow(suggestion);
                added++;
                if (added >= 5) break;
            }
        }

        searchSuggestionsLayout.setVisibility(added > 0 ? View.VISIBLE : View.GONE);
    }

    private void addSuggestionRow(String suggestion) {
        TextView row = new TextView(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        row.setText(suggestion);
        row.setTextColor(getColor(R.color.text_dark));
        row.setTextSize(14);
        row.setPadding(18, 12, 18, 12);
        row.setOnClickListener(v -> openDoctorSearch(suggestion));
        searchSuggestionsLayout.addView(row);
    }

    private void hideSearchSuggestions() {
        if (searchSuggestionsLayout != null) {
            searchSuggestionsLayout.setVisibility(View.GONE);
        }
    }
}
