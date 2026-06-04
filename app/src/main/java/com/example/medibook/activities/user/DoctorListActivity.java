package com.example.medibook.activities.user;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DoctorListActivity extends AppCompatActivity implements DoctorAdapter.OnDoctorClickListener {

    private RecyclerView doctorsRecyclerView;
    private DoctorAdapter doctorAdapter;
    private List<Doctor> allDoctors;
    private List<Doctor> filteredDoctors;
    private ProgressBar loadingProgress;
    private View emptyStateLayout;
    private TextInputEditText searchEditText;
    private ChipGroup specialtyChipGroup;
    private String initialSpecialtyFilter;
    private String initialSearchQuery;

    private DoctorRepository doctorRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_list);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // Initialize repository
        doctorRepository = new DoctorRepository();
        initialSpecialtyFilter = getIntent().getStringExtra("specialty");
        initialSearchQuery = getIntent().getStringExtra("searchQuery");

        // Initialize lists
        allDoctors = new ArrayList<>();
        filteredDoctors = new ArrayList<>();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        doctorsRecyclerView = findViewById(R.id.doctors_recycler_view);
        loadingProgress = findViewById(R.id.loading_progress);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        searchEditText = findViewById(R.id.search_edit_text);
        specialtyChipGroup = findViewById(R.id.specialty_chip_group);
        if (initialSearchQuery != null && !initialSearchQuery.trim().isEmpty()) {
            searchEditText.setText(initialSearchQuery.trim());
        }

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
                updateFilterChipStyles();
                filterDoctors();
            }
        });

        setupBottomNavigation();

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
                    buildSpecialtyFilters();
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
            String doctorName = doctor.getName() != null ? doctor.getName() : "";
            String doctorSpecialty = doctor.getSpecialty() != null ? doctor.getSpecialty() : "";
            boolean matchesSearch = searchQuery.isEmpty() ||
                doctorName.toLowerCase().contains(searchQuery) ||
                doctorSpecialty.toLowerCase().contains(searchQuery);

            boolean matchesSpecialty = selectedSpecialty.equals("⚕ Specialty") ||
                selectedSpecialty.equals("★ Top Rated") ||
                selectedSpecialty.equals("☐ Available") ||
                doctorSpecialty.equalsIgnoreCase(selectedSpecialty);
            boolean matchesAvailability = !selectedSpecialty.equals("☐ Available") ||
                (doctor.isActive() && doctor.isVerified());

            if (matchesSearch && matchesSpecialty && matchesAvailability) {
                filteredDoctors.add(doctor);
            }
        }

        if (selectedSpecialty.equals("★ Top Rated")) {
            Collections.sort(filteredDoctors, (d1, d2) -> parseRating(d2.getRating()) - parseRating(d1.getRating()));
        } else {
            Collections.sort(filteredDoctors, (d1, d2) -> {
                String name1 = d1.getName() != null ? d1.getName() : "";
                String name2 = d2.getName() != null ? d2.getName() : "";
                return name1.compareToIgnoreCase(name2);
            });
        }

        doctorAdapter.notifyDataSetChanged();
        showEmptyState(filteredDoctors.isEmpty());
    }

    private void buildSpecialtyFilters() {
        specialtyChipGroup.removeAllViews();
        addFilterChip("⚕ Specialty", true);
        addFilterChip("★ Top Rated", false);
        addFilterChip("☐ Available", false);

        Set<String> specialties = new HashSet<>();
        for (Doctor doctor : allDoctors) {
            if (doctor.getSpecialty() != null && !doctor.getSpecialty().trim().isEmpty()) {
                specialties.add(doctor.getSpecialty().trim());
            }
        }

        List<String> sortedSpecialties = new ArrayList<>(specialties);
        Collections.sort(sortedSpecialties, String::compareToIgnoreCase);
        for (String specialty : sortedSpecialties) {
            addFilterChip(specialty, false);
        }

        applyInitialSpecialtyFilter();
    }

    private void addFilterChip(String text, boolean checked) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCheckable(true);
        chip.setChecked(checked);
        chip.setTextSize(12);
        chip.setChipMinHeight(40);
        chip.setMinHeight(40);
        chip.setEnsureMinTouchTargetSize(false);
        chip.setCheckedIconVisible(false);
        chip.setChipCornerRadius(22);
        styleFilterChip(chip);
        specialtyChipGroup.addView(chip);
    }

    private void applyInitialSpecialtyFilter() {
        if (initialSpecialtyFilter == null || initialSpecialtyFilter.trim().isEmpty()) {
            return;
        }

        for (int i = 0; i < specialtyChipGroup.getChildCount(); i++) {
            View child = specialtyChipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (initialSpecialtyFilter.equalsIgnoreCase(chip.getText().toString())) {
                    chip.setChecked(true);
                    updateFilterChipStyles();
                    return;
                }
            }
        }
    }

    private void updateFilterChipStyles() {
        for (int i = 0; i < specialtyChipGroup.getChildCount(); i++) {
            View child = specialtyChipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                styleFilterChip(chip);
            }
        }
    }

    private void styleFilterChip(Chip chip) {
        chip.setTextColor(chip.isChecked() ? Color.WHITE : Color.rgb(15, 23, 42));
        chip.setChipBackgroundColor(ColorStateList.valueOf(
                chip.isChecked() ? Color.rgb(0, 122, 255) : Color.WHITE));
    }

    private String getSelectedSpecialty() {
        int checkedId = specialtyChipGroup.getCheckedChipId();
        if (checkedId == View.NO_ID) {
            return "⚕ Specialty";
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

    private int parseRating(String rating) {
        if (rating == null) return 0;
        try {
            return Math.round(Float.parseFloat(rating.trim()) * 10);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private void setupBottomNavigation() {
        TextView navHome = findViewById(R.id.nav_home);
        TextView navBookings = findViewById(R.id.nav_bookings);
        TextView navAlerts = findViewById(R.id.nav_alerts);
        TextView navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, UserHomeActivity.class));
            finish();
        });
        navBookings.setOnClickListener(v -> startActivity(new Intent(this, BookingsListActivity.class)));
        navAlerts.setOnClickListener(v -> startActivity(new Intent(this, AlertsActivity.class)));
        navProfile.setOnClickListener(v -> startActivity(new Intent(this, PatientProfileViewActivity.class)));
    }

    @Override
    public void onDoctorClick(Doctor doctor) {
        Intent intent = new Intent(this, DoctorProfileActivity.class);
        intent.putExtra("doctorId", doctor.getDoctorId());
        intent.putExtra("doctorName", doctor.getName());
        intent.putExtra("doctorSpecialty", doctor.getSpecialty());
        startActivity(intent);
    }

    @Override
    public void onBookDoctorClick(Doctor doctor) {
        Intent intent = new Intent(this, BookAppointmentActivity.class);
        intent.putExtra("doctorId", doctor.getDoctorId());
        intent.putExtra("doctorName", doctor.getName());
        intent.putExtra("doctorSpecialty", doctor.getSpecialty());
        startActivity(intent);
    }
}
