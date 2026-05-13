package com.example.medibook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.medibook.R;
import com.example.medibook.models.Doctor;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private List<Doctor> doctorList;
    private OnDoctorClickListener listener;

    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }

    public DoctorAdapter(List<Doctor> doctorList, OnDoctorClickListener listener) {
        this.doctorList = doctorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view, listener, doctorList);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctorList.get(position);
        holder.nameTextView.setText(doctor.getName() != null ? doctor.getName() : "Dr. Name");
        holder.specialtyTextView.setText(doctor.getSpecialty() != null ? doctor.getSpecialty() : "Specialty");

        // Handle rating display
        String rating = doctor.getRating();
        if (rating != null && !rating.isEmpty()) {
            holder.ratingTextView.setText(rating + " ⭐");
        } else {
            holder.ratingTextView.setText("4.5 ⭐");
        }

        // Load profile image using Glide
        if (doctor.getProfileImage() != null && !doctor.getProfileImage().isEmpty()) {
            Glide.with(holder.imageView.getContext())
                .load(doctor.getProfileImage())
                .placeholder(R.drawable.ic_medical_kit)
                .error(R.drawable.ic_medical_kit)
                .circleCrop()
                .into(holder.imageView);
        } else {
            // Use default placeholder
            Glide.with(holder.imageView.getContext())
                .load(R.drawable.ic_medical_kit)
                .circleCrop()
                .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    public static class DoctorViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView, specialtyTextView, ratingTextView;

        public DoctorViewHolder(@NonNull View itemView, OnDoctorClickListener listener, List<Doctor> doctors) {
            super(itemView);
            imageView = itemView.findViewById(R.id.doctor_image);
            nameTextView = itemView.findViewById(R.id.doctor_name);
            specialtyTextView = itemView.findViewById(R.id.doctor_specialty);
            ratingTextView = itemView.findViewById(R.id.doctor_rating);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDoctorClick(doctors.get(position));
                }
            });
        }
    }
}
