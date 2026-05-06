package com.example.medibook.activities.user;

import android.os.Bundle;
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

public class AppointmentsActivity extends AppCompatActivity implements AppointmentAdapter.OnAppointmentClickListener {

    private RecyclerView appointmentsRecyclerView;
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> appointmentList;
    private AppointmentRepository appointmentRepository;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);

        // Initialize repositories
        appointmentRepository = new AppointmentRepository();
        authRepository = new AuthRepository();

        // Initialize views
        appointmentsRecyclerView = findViewById(R.id.appointments_recycler_view);
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        appointmentList = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(appointmentList, this);
        appointmentsRecyclerView.setAdapter(appointmentAdapter);

        // Load appointments
        loadAppointments();
    }

    private void loadAppointments() {
        String userId = authRepository.getCurrentUser().getUid();
        appointmentRepository.getPatientAppointments(userId, new AppointmentRepository.AppointmentsCallback() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                appointmentList.clear();
                appointmentList.addAll(appointments);
                appointmentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AppointmentsActivity.this, "Failed to load appointments: " + error, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(AppointmentsActivity.this, "Appointment cancelled", Toast.LENGTH_SHORT).show();
                loadAppointments(); // Refresh list
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AppointmentsActivity.this, "Failed to cancel appointment: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}