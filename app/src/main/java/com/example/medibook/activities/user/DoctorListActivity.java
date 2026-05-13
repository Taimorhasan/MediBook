package com.example.medibook.activities.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.adapters.DoctorAdapter;
import com.example.medibook.models.Doctor;
import com.example.medibook.repositories.DoctorRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class DoctorListActivity extends AppCompatActivity implements DoctorAdapter.OnDoctorClickListener {

    private RecyclerView doctorsRecyclerView;
    private DoctorAdapter doctorAdapter;
    private List<Doctor> allDoctors;
    private List<Doctor> filteredDoctors;
    private ProgressBar loadingProgress;
    private View emptyStateLayout;
    private TextInputEditText searchEditText;
    private ChipGroup specialtyChipGroup;

    private DoctorRepository doctorRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_list);

        // Initialize repository
        doctorRepository = new DoctorRepository();

        // Initialize lists
        allDoctors = new ArrayList<>();
        filteredDoctors = new ArrayList<>();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        doctorsRecyclerView = findViewById(R.id.doctors_recycler_view);
        loadingProgress = findViewById(R.id.loading_progress);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        searchEditText = findViewById(R.id.search_edit_text);
        specialtyChipGroup = findViewById(R.id.specialty_chip_group);

        // Setup RecyclerView
        doctorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        doctorAdapter = new DoctorAdapter(filteredDoctors, this);
        doctorsRecyclerView.setAdapter(doctorAdapter);

        // Setup search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDoctors();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup specialty filter
        specialtyChipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                filterDoctors();
            }
        });

        // Load doctors
        loadDoctors();
    }

    private void loadDoctors() {
        showLoading(true);

        doctorRepository.getAllDoctors(new DoctorRepository.DoctorsCallback() {
            @Override
            public void onSuccess(List<Doctor> doctors) {
                runOnUiThread(() -> {
                    showLoading(false);
                    allDoctors.clear();
                    allDoctors.addAll(doctors);
                    filterDoctors();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(DoctorListActivity.this,
                        "Failed to load doctors: " + error, Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                });
            }
        });
    }

    private void filterDoctors() {
        String searchQuery = searchEditText.getText().toString().toLowerCase().trim();
        String selectedSpecialty = getSelectedSpecialty();

        filteredDoctors.clear();

        for (Doctor doctor : allDoctors) {
            boolean matchesSearch = searchQuery.isEmpty() ||
                doctor.getName().toLowerCase().contains(searchQuery) ||
                doctor.getSpecialty().toLowerCase().contains(searchQuery);

            boolean matchesSpecialty = selectedSpecialty.equals("All Specialties") ||
                doctor.getSpecialty().equalsIgnoreCase(selectedSpecialty);

            if (matchesSearch && matchesSpecialty) {
                filteredDoctors.add(doctor);
            }
        }

        doctorAdapter.notifyDataSetChanged();
        showEmptyState(filteredDoctors.isEmpty());
    }

    private String getSelectedSpecialty() {
        int checkedId = specialtyChipGroup.getCheckedChipId();
        if (checkedId == View.NO_ID) {
            return "All Specialties";
        }

        Chip selectedChip = findViewById(checkedId);
        return selectedChip.getText().toString();
    }

    private void showLoading(boolean show) {
        loadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        doctorsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        if (show) {
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void showEmptyState(boolean show) {
        emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        doctorsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        loadingProgress.setVisibility(View.GONE);
    }

    @Override
    public void onDoctorClick(Doctor doctor) {
        // Navigate to BookAppointmentActivity with doctor data
        Intent intent = new Intent(this, BookAppointmentActivity.class);
        intent.putExtra("doctorId", doctor.getDoctorId());
        intent.putExtra("doctorName", doctor.getName());
        intent.putExtra("doctorSpecialty", doctor.getSpecialty());
        startActivity(intent);
    }
}