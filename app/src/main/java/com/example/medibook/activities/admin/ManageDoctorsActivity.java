package com.example.medibook.activities.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.activities.common.BaseActivity;
import com.example.medibook.adapters.DoctorVerificationAdapter;
import com.example.medibook.models.Doctor;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.DoctorRepository;
import java.util.ArrayList;
import java.util.List;

public class ManageDoctorsActivity extends BaseActivity implements DoctorVerificationAdapter.OnDoctorActionListener {

    private RecyclerView recyclerView;
    private DoctorVerificationAdapter adapter;
    private DoctorRepository doctorRepository;
    private ProgressBar progressBar;
    private List<Doctor> doctorList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_doctors);
        // RBAC: Strict role verification for the admin panel.
        if (!checkRoleAndRedirect("admin")) {
            return;
        }

        initViews();
        setupToolbar();
        doctorRepository = new DoctorRepository();
        loadDoctors();
    }

    private void setupToolbar() {
        // Find the standard Toolbar ID from the included layout. 
        // ID conflict fixed by removing 'toolbar_layout' from the <include> tag.
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Doctor Verification");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        recyclerView = findViewById(R.id.doctors_recycler_view);
        progressBar = findViewById(R.id.loading_progress);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DoctorVerificationAdapter(doctorList, this, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadDoctors() {
        progressBar.setVisibility(View.VISIBLE);
        doctorRepository.getAllDoctorsAdmin(new DoctorRepository.DoctorsCallback() {
            @Override
            public void onSuccess(List<Doctor> doctors) {
                progressBar.setVisibility(View.GONE);
                doctorList = doctors;
                adapter.updateList(doctorList);
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageDoctorsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onVerifyDoctor(Doctor doctor, boolean verify) {
        progressBar.setVisibility(View.VISIBLE);
        doctorRepository.verifyDoctor(doctor.getDoctorId(), verify, new AuthRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageDoctorsActivity.this, "Doctor verified successfully", Toast.LENGTH_SHORT).show();
                loadDoctors(); // Refresh list
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageDoctorsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
