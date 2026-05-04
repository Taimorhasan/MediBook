package com.example.medibook.activities.user;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.adapters.DoctorAdapter;
import com.example.medibook.models.Doctor;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;

public class UserHomeActivity extends AppCompatActivity implements DoctorAdapter.OnDoctorClickListener {

    private RecyclerView doctorsRecyclerView;
    private DoctorAdapter doctorAdapter;
    private List<Doctor> doctorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        doctorsRecyclerView = findViewById(R.id.doctors_recycler_view);
        doctorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        prepareDoctorData();
        doctorAdapter = new DoctorAdapter(doctorList, this);
        doctorsRecyclerView.setAdapter(doctorAdapter);
    }

    @Override
    public void onDoctorClick(Doctor doctor) {
        Intent intent = new Intent(this, DoctorProfileActivity.class);
        // Passing data could be done here if needed (e.g. Parcelable)
        startActivity(intent);
    }

    private void prepareDoctorData() {
        doctorList = new ArrayList<>();
        doctorList.add(new Doctor("Dr. Sarah Wilson", "Cardiologist", "4.8", android.R.drawable.ic_menu_myplaces));
        doctorList.add(new Doctor("Dr. James Miller", "Dentist", "4.5", android.R.drawable.ic_menu_myplaces));
        doctorList.add(new Doctor("Dr. Emily Brown", "General Physician", "4.9", android.R.drawable.ic_menu_myplaces));
        doctorList.add(new Doctor("Dr. Michael Chen", "Orthopedic", "4.7", android.R.drawable.ic_menu_myplaces));
    }
}
