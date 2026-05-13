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
    private View emptyStateView;
    private android.widget.EditText searchEditText;
    private android.widget.Spinner filterSpinner;
    private List<Doctor> fullDoctorList = new ArrayList<>();
    private List<Doctor> filteredList = new ArrayList<>();

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
        emptyStateView = findViewById(R.id.empty_state_view);
        searchEditText = findViewById(R.id.search_edit_text);
        filterSpinner = findViewById(R.id.status_filter_spinner);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DoctorVerificationAdapter(filteredList, this, this);
        recyclerView.setAdapter(adapter);

        // Search logic
        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Filter logic
        filterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void applyFilters() {
        String query = searchEditText.getText().toString().toLowerCase().trim();
        int filterPos = filterSpinner.getSelectedItemPosition();
        
        filteredList.clear();
        for (Doctor doctor : fullDoctorList) {
            String name = doctor.getName() != null ? doctor.getName().toLowerCase() : "";
            String specialty = doctor.getSpecialty() != null ? doctor.getSpecialty().toLowerCase() : "";
            String hospital = doctor.getHospitalName() != null ? doctor.getHospitalName().toLowerCase() : "";

            boolean matchesSearch = name.contains(query) || 
                                     specialty.contains(query) ||
                                     hospital.contains(query);
            
            boolean matchesFilter = true;
            switch (filterPos) {
                case 1: // Pending
                    matchesFilter = !doctor.isVerified();
                    break;
                case 2: // Verified
                    matchesFilter = doctor.isVerified();
                    break;
                case 3: // Active
                    matchesFilter = doctor.isActive();
                    break;
                case 4: // Inactive
                    matchesFilter = !doctor.isActive();
                    break;
            }

            if (matchesSearch && matchesFilter) {
                filteredList.add(doctor);
            }
        }
        
        adapter.updateList(filteredList);
        emptyStateView.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadDoctors() {
        progressBar.setVisibility(View.VISIBLE);
        doctorRepository.getAllDoctorsAdmin(new DoctorRepository.DoctorsCallback() {
            @Override
            public void onSuccess(List<Doctor> doctors) {
                progressBar.setVisibility(View.GONE);
                fullDoctorList = doctors;
                applyFilters();
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
                String msg = verify ? "Doctor verified successfully" : "Doctor verification removed";
                Toast.makeText(ManageDoctorsActivity.this, msg, Toast.LENGTH_SHORT).show();
                
                // Update local model to reflect change immediately without full reload
                doctor.setVerified(verify);
                applyFilters();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageDoctorsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditDoctor(Doctor doctor) {
        showManageDoctorDialog(doctor);
    }

    @Override
    public void onToggleStatus(Doctor doctor, boolean active) {
        progressBar.setVisibility(View.VISIBLE);
        doctor.setActive(active);
        doctorRepository.updateDoctor(doctor, new DoctorRepository.DoctorCallback() {
            @Override
            public void onSuccess(Doctor updatedDoctor) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageDoctorsActivity.this, "Status updated", Toast.LENGTH_SHORT).show();
                loadDoctors();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageDoctorsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showManageDoctorDialog(Doctor doctor) {
        com.example.medibook.repositories.HospitalRepository hospitalRepo = new com.example.medibook.repositories.HospitalRepository();
        hospitalRepo.getAllHospitals(new com.example.medibook.repositories.HospitalRepository.HospitalsCallback() {
            @Override
            public void onSuccess(List<com.example.medibook.models.Hospital> hospitals) {
                if (hospitals.isEmpty()) {
                    Toast.makeText(ManageDoctorsActivity.this, "No hospitals found. Please add hospitals first.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] hospitalNames = new String[hospitals.size()];
                for (int i = 0; i < hospitals.size(); i++) {
                    hospitalNames[i] = hospitals.get(i).getName();
                }

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ManageDoctorsActivity.this);
                builder.setTitle("Assign Hospital to Dr. " + doctor.getName());
                builder.setItems(hospitalNames, (dialog, which) -> {
                    com.example.medibook.models.Hospital selectedHospital = hospitals.get(which);
                    doctor.setHospitalId(selectedHospital.getHospitalId());
                    doctor.setHospitalName(selectedHospital.getName());
                    
                    progressBar.setVisibility(View.VISIBLE);
                    doctorRepository.updateDoctor(doctor, new DoctorRepository.DoctorCallback() {
                        @Override
                        public void onSuccess(Doctor updatedDoctor) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ManageDoctorsActivity.this, "Doctor updated successfully", Toast.LENGTH_SHORT).show();
                            loadDoctors();
                        }

                        @Override
                        public void onFailure(String error) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ManageDoctorsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ManageDoctorsActivity.this, "Error loading hospitals: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
