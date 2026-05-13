package com.example.medibook.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;

public class UserHomeActivity extends AppCompatActivity implements AppointmentAdapter.OnAppointmentClickListener {

    private Button btnViewDetails;
    private Button btnBookVisit;
    private LinearLayout navHome;
    private LinearLayout navBookings;
    private LinearLayout navAlerts;
    private LinearLayout navProfile;

    // Appointment related views
    private RecyclerView appointmentsRecyclerView;
    private ProgressBar appointmentsLoadingProgress;
    private TextView noAppointmentsTextView;
    private TextView upcomingAppointmentsTitle;

    // Data
    private List<Appointment> appointments;
    private AppointmentAdapter appointmentAdapter;
    private AppointmentRepository appointmentRepository;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        // Initialize repositories and data
        appointmentRepository = new AppointmentRepository();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        appointments = new ArrayList<>();

        // Initialize Buttons
        btnViewDetails = findViewById(R.id.btn_view_details);
        btnBookVisit = findViewById(R.id.btn_book_visit);
        
        // Initialize Bottom Nav Items
        navHome = findViewById(R.id.nav_home_layout);
        navBookings = findViewById(R.id.nav_bookings_layout);
        navAlerts = findViewById(R.id.nav_alerts_layout);
        navProfile = findViewById(R.id.nav_profile_layout);

        // Initialize appointment views
        appointmentsRecyclerView = findViewById(R.id.appointments_recycler_view);
        appointmentsLoadingProgress = findViewById(R.id.appointments_loading_progress);
        noAppointmentsTextView = findViewById(R.id.no_appointments_text);
        upcomingAppointmentsTitle = findViewById(R.id.upcoming_appointments_title);

        // Setup appointments RecyclerView
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointmentAdapter = new AppointmentAdapter(appointments, this);
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
                    // Navigate to Appointments Activity
                    Intent intent = new Intent(UserHomeActivity.this, BookingsListActivity.class);
                    startActivity(intent);
                    // Add smooth slide animation: new screen slides in from right, current slides out to left
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
        if (btnViewDetails != null) {
            btnViewDetails.setVisibility(View.GONE);
        }

        // 3. Make "Book Visit" functional
        btnBookVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Doctor List Activity
                Intent intent = new Intent(UserHomeActivity.this, DoctorListActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
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
        // Handle appointment cancellation
        Toast.makeText(this, "Cancel appointment functionality not implemented yet", Toast.LENGTH_SHORT).show();
        // TODO: Implement appointment cancellation
    }
}