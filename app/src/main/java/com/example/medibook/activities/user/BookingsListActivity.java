package com.example.medibook.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.adapters.AppointmentAdapter;
import com.example.medibook.models.Appointment;
import com.example.medibook.repositories.AppointmentRepository;
import com.example.medibook.repositories.AuthRepository;
import java.util.ArrayList;
import java.util.List;

public class BookingsListActivity extends AppCompatActivity implements AppointmentAdapter.OnAppointmentClickListener {

    private RecyclerView bookingsRecyclerView;
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> bookingList;
    private AppointmentRepository appointmentRepository;
    private AuthRepository authRepository;
    private LinearLayout emptyStateLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings_list);

        // Initialize repositories
        appointmentRepository = new AppointmentRepository();
        authRepository = new AuthRepository();

        // Initialize views
        bookingsRecyclerView = findViewById(R.id.bookings_recycler_view);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        
        if (bookingsRecyclerView != null) {
            bookingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        bookingList = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(bookingList, this);
        if (bookingsRecyclerView != null) {
            bookingsRecyclerView.setAdapter(appointmentAdapter);
        }

        // Load bookings/appointments for the current user
        loadBookings();

        // Initialize Bottom Nav Items
        LinearLayout navHome = findViewById(R.id.nav_home_layout);
        LinearLayout navBookings = findViewById(R.id.nav_bookings_layout);
        LinearLayout navAlerts = findViewById(R.id.nav_alerts_layout);
        LinearLayout navProfile = findViewById(R.id.nav_profile_layout);

        // 1. Click Home to go back to Dashboard
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(BookingsListActivity.this, UserHomeActivity.class);
                startActivity(intent);
                // Add smooth slide animation: new screen slides in from left, current slides out to right
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish(); // Close this screen so back button doesn't loop
            });
        }

        // 2. Bookings is already active, maybe refresh or do nothing
        if (navBookings != null) {
            navBookings.setOnClickListener(v -> {
                loadBookings(); // Refresh the bookings list
                Toast.makeText(this, "Refreshed bookings list", Toast.LENGTH_SHORT).show();
            });
        }

        // 3. Alerts Navigation
        if (navAlerts != null) {
            navAlerts.setOnClickListener(v -> {
                Intent intent = new Intent(BookingsListActivity.this, AlertsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            });
        }

        // 4. Profile Navigation
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(BookingsListActivity.this, PatientProfileViewActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }
    }

    private void loadBookings() {
        // Check if user is authenticated before proceeding
        if (authRepository.getCurrentUser() == null) {
            Toast.makeText(this, "Please login to view your bookings", Toast.LENGTH_LONG).show();
            return;
        }

        String userId = authRepository.getCurrentUser().getUid();
        
        // Get appointments booked by this user (as a patient)
        appointmentRepository.getPatientAppointments(userId, new AppointmentRepository.AppointmentsCallback() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                bookingList.clear();
                bookingList.addAll(appointments);
                
                // Update UI on the main thread
                runOnUiThread(() -> {
                    if (appointmentAdapter != null) {
                        appointmentAdapter.notifyDataSetChanged();
                        
                        // Show/hide empty state based on appointment count
                        if (emptyStateLayout != null) {
                            if (bookingList.isEmpty()) {
                                emptyStateLayout.setVisibility(View.VISIBLE);
                                bookingsRecyclerView.setVisibility(View.GONE);
                            } else {
                                emptyStateLayout.setVisibility(View.GONE);
                                bookingsRecyclerView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(BookingsListActivity.this, "Failed to load bookings: " + error, Toast.LENGTH_SHORT).show();
                    
                    // Still update UI to show empty state if needed
                    if (appointmentAdapter != null) {
                        appointmentAdapter.notifyDataSetChanged();
                        
                        if (emptyStateLayout != null) {
                            if (bookingList.isEmpty()) {
                                emptyStateLayout.setVisibility(View.VISIBLE);
                                bookingsRecyclerView.setVisibility(View.GONE);
                            } else {
                                emptyStateLayout.setVisibility(View.GONE);
                                bookingsRecyclerView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onAppointmentClick(Appointment appointment) {
        // Handle appointment click - maybe show details or allow cancellation
        Toast.makeText(this, "Appointment: " + appointment.getDate() + " " + appointment.getTime(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelAppointment(Appointment appointment) {
        appointmentRepository.cancelAppointment(appointment.getAppointmentId(), new AppointmentRepository.AppointmentCallback() {
            @Override
            public void onSuccess(Appointment updatedAppointment) {
                runOnUiThread(() -> {
                    Toast.makeText(BookingsListActivity.this, "Appointment cancelled", Toast.LENGTH_SHORT).show();
                    loadBookings(); // Refresh list
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(BookingsListActivity.this, "Failed to cancel appointment: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}