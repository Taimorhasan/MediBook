package com.example.medibook.activities.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.activities.common.BaseActivity;
import com.example.medibook.adapters.HospitalAdapter;
import com.example.medibook.models.Hospital;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.HospitalRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ManageHospitalsActivity extends BaseActivity implements HospitalAdapter.OnHospitalActionListener {

    private RecyclerView recyclerView;
    private HospitalAdapter adapter;
    private HospitalRepository hospitalRepository;
    private ProgressBar progressBar;
    private FloatingActionButton addFab;
    private List<Hospital> hospitalList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_hospitals);
        if (!checkRoleAndRedirect("admin")) {
            return;
        }

        initViews();
        setupToolbar();
        hospitalRepository = new HospitalRepository();
        loadHospitals();
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Hospitals");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        recyclerView = findViewById(R.id.hospitals_recycler_view);
        progressBar = findViewById(R.id.loading_progress);
        addFab = findViewById(R.id.add_hospital_fab);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HospitalAdapter(hospitalList, this, this);
        recyclerView.setAdapter(adapter);

        addFab.setOnClickListener(v -> showHospitalDialog(null));
    }

    private void loadHospitals() {
        progressBar.setVisibility(View.VISIBLE);
        hospitalRepository.getAllHospitals(new HospitalRepository.HospitalsCallback() {
            @Override
            public void onSuccess(List<Hospital> hospitals) {
                progressBar.setVisibility(View.GONE);
                hospitalList = hospitals;
                adapter.updateList(hospitalList);
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageHospitalsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditHospital(Hospital hospital) {
        showHospitalDialog(hospital);
    }

    private void showHospitalDialog(Hospital hospital) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_hospital, null);
        
        EditText nameEdit = view.findViewById(R.id.edit_hospital_name);
        EditText addressEdit = view.findViewById(R.id.edit_hospital_address);
        EditText phoneEdit = view.findViewById(R.id.edit_hospital_phone);
        EditText emailEdit = view.findViewById(R.id.edit_hospital_email);

        if (hospital != null) {
            builder.setTitle("Edit Hospital");
            nameEdit.setText(hospital.getName());
            addressEdit.setText(hospital.getAddress());
            phoneEdit.setText(hospital.getPhone());
            emailEdit.setText(hospital.getEmail());
        } else {
            builder.setTitle("Add New Hospital");
        }

        builder.setView(view);
        builder.setPositiveButton(hospital != null ? "Update" : "Add", (dialog, which) -> {
            String name = nameEdit.getText().toString().trim();
            String address = addressEdit.getText().toString().trim();
            String phone = phoneEdit.getText().toString().trim();
            String email = emailEdit.getText().toString().trim();

            if (name.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Name and Address are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (hospital != null) {
                hospital.setName(name);
                hospital.setAddress(address);
                hospital.setPhone(phone);
                hospital.setEmail(email);
                updateHospital(hospital);
            } else {
                Hospital newHospital = new Hospital(
                        UUID.randomUUID().toString(),
                        name, address, phone, email, 0.0, 0.0
                );
                addHospital(newHospital);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addHospital(Hospital hospital) {
        progressBar.setVisibility(View.VISIBLE);
        hospitalRepository.addHospital(hospital, new HospitalRepository.HospitalCallback() {
            @Override
            public void onSuccess(Hospital addedHospital) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageHospitalsActivity.this, "Hospital added", Toast.LENGTH_SHORT).show();
                loadHospitals();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageHospitalsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateHospital(Hospital hospital) {
        progressBar.setVisibility(View.VISIBLE);
        hospitalRepository.updateHospital(hospital, new AuthRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageHospitalsActivity.this, "Hospital updated", Toast.LENGTH_SHORT).show();
                loadHospitals();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageHospitalsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
