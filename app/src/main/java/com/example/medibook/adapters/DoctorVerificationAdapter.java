package com.example.medibook.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.models.Doctor;
import com.google.android.material.chip.Chip;
import java.util.List;

public class DoctorVerificationAdapter extends RecyclerView.Adapter<DoctorVerificationAdapter.ViewHolder> {

    private List<Doctor> doctorList;
    private Context context;
    private OnDoctorActionListener listener;

    public interface OnDoctorActionListener {
        void onVerifyDoctor(Doctor doctor, boolean verify);
        void onEditDoctor(Doctor doctor);
        void onToggleStatus(Doctor doctor, boolean active);
    }

    public DoctorVerificationAdapter(List<Doctor> doctorList, Context context, OnDoctorActionListener listener) {
        this.doctorList = doctorList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor_verification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doctor = doctorList.get(position);
        
        String name = doctor.getName() != null ? doctor.getName() : "Unknown";
        String specialty = doctor.getSpecialization() != null ? doctor.getSpecialization() : "General";
        String hospital = doctor.getHospitalName() != null ? doctor.getHospitalName() : "No Hospital";

        holder.nameText.setText("Dr. " + name);
        holder.specialtyText.setText(specialty);
        holder.hospitalText.setText(hospital);
        
        // Verification Status
        boolean isVerified = doctor.isVerified();
        holder.statusChip.setText(isVerified ? "VERIFIED" : "PENDING");
        holder.statusChip.setChipBackgroundColorResource(isVerified ? android.R.color.holo_green_dark : android.R.color.holo_orange_dark);

        // Active Status
        boolean isActive = doctor.isActive();
        holder.activeStatusChip.setText(isActive ? "ACTIVE" : "INACTIVE");
        holder.activeStatusChip.setChipBackgroundColorResource(isActive ? android.R.color.holo_blue_dark : android.R.color.darker_gray);

        // Buttons visibility and listeners
        holder.verifyBtn.setVisibility(isVerified ? View.GONE : View.VISIBLE);
        holder.verifyBtn.setOnClickListener(v -> {
            if (listener != null) listener.onVerifyDoctor(doctor, true);
        });

        holder.editBtn.setOnClickListener(v -> {
            if (listener != null) listener.onEditDoctor(doctor);
        });

        holder.activeStatusChip.setOnClickListener(v -> {
            if (listener != null) listener.onToggleStatus(doctor, !doctor.isActive());
        });
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    public void updateList(List<Doctor> newList) {
        this.doctorList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, specialtyText, hospitalText;
        Chip statusChip, activeStatusChip;
        Button verifyBtn, editBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.doctor_name);
            specialtyText = itemView.findViewById(R.id.doctor_specialty);
            hospitalText = itemView.findViewById(R.id.doctor_hospital);
            statusChip = itemView.findViewById(R.id.verification_status_chip);
            activeStatusChip = itemView.findViewById(R.id.active_status_chip);
            verifyBtn = itemView.findViewById(R.id.verify_button);
            editBtn = itemView.findViewById(R.id.edit_button);
        }
    }
}
