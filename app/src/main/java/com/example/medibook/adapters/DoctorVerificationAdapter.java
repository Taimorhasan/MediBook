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
        
        boolean isVerified = doctor.isVerified();
        holder.statusChip.setText(isVerified ? "VERIFIED" : "PENDING");
        holder.statusChip.setChipBackgroundColorResource(isVerified ? android.R.color.holo_green_light : android.R.color.holo_orange_light);

        holder.verifyBtn.setVisibility(isVerified ? View.GONE : View.VISIBLE);
        holder.verifyBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVerifyDoctor(doctor, true);
            }
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
        Chip statusChip;
        Button verifyBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.doctor_name);
            specialtyText = itemView.findViewById(R.id.doctor_specialty);
            hospitalText = itemView.findViewById(R.id.doctor_hospital);
            statusChip = itemView.findViewById(R.id.verification_status_chip);
            verifyBtn = itemView.findViewById(R.id.verify_button);
        }
    }
}
