package com.example.medibook.activities.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.activities.common.BaseActivity;
import com.example.medibook.adapters.AppointmentAdapter;
import com.example.medibook.models.Appointment;
import com.example.medibook.repositories.AppointmentRepository;
import java.util.ArrayList;
import java.util.List;

public class ManageAppointmentsActivity extends BaseActivity implements AppointmentAdapter.OnAppointmentClickListener {

    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private AppointmentRepository appointmentRepository;
    private ProgressBar progressBar;
    private View emptyStateView;
    private List<Appointment> appointmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_appointments);
        if (!checkRoleAndRedirect("admin")) {
            return;
        }

        initViews();
        setupToolbar();
        appointmentRepository = new AppointmentRepository();
        loadAppointments();
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Appointments");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        recyclerView = findViewById(R.id.appointments_recycler_view);
        progressBar = findViewById(R.id.loading_progress);
        emptyStateView = findViewById(R.id.empty_state_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(appointmentList, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadAppointments() {
        progressBar.setVisibility(View.VISIBLE);
        appointmentRepository.getAllAppointments(new AppointmentRepository.AppointmentsCallback() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                progressBar.setVisibility(View.GONE);
                appointmentList = appointments;
                adapter.updateList(appointmentList);
                emptyStateView.setVisibility(appointmentList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageAppointmentsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAppointmentClick(Appointment appointment) {
        // Option to show details if needed
    }

    @Override
    public void onCancelAppointment(Appointment appointment) {
        progressBar.setVisibility(View.VISIBLE);
        appointmentRepository.cancelAppointment(appointment.getAppointmentId(), new AppointmentRepository.AppointmentCallback() {
            @Override
            public void onSuccess(Appointment updatedAppointment) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageAppointmentsActivity.this, "Appointment cancelled", Toast.LENGTH_SHORT).show();
                loadAppointments(); // Refresh
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageAppointmentsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
