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
        void onBookDoctorClick(Doctor doctor);
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
        holder.locationTextView.setText(getLocationText(doctor));
        holder.availableTextView.setText(getAvailabilityText(doctor));

        String rating = doctor.getRating();
        if (rating != null && !rating.isEmpty()) {
            holder.ratingTextView.setText("★ " + rating);
        } else {
            holder.ratingTextView.setText("★ 4.5");
        }

        if (doctor.getProfileImage() != null && !doctor.getProfileImage().isEmpty()) {
            Glide.with(holder.imageView.getContext())
                .load(doctor.getProfileImage())
                .placeholder(R.drawable.ic_medical_kit)
                .error(R.drawable.ic_medical_kit)
                .into(holder.imageView);
        } else {
            Glide.with(holder.imageView.getContext())
                .load(R.drawable.ic_medical_kit)
                .into(holder.imageView);
        }
    }

    private String getLocationText(Doctor doctor) {
        if (doctor.getHospitalName() != null && !doctor.getHospitalName().trim().isEmpty()) {
            return doctor.getHospitalName().trim().toUpperCase();
        }
        return "HOSPITAL DETAILS AVAILABLE IN PROFILE";
    }

    private String getAvailabilityText(Doctor doctor) {
        if (doctor.getAvailableDays() != null && !doctor.getAvailableDays().isEmpty()) {
            String day = doctor.getAvailableDays().get(0);
            if (day != null && !day.trim().isEmpty()) {
                return "Next available:\n" + day.trim();
            }
        }
        if (doctor.isActive() && doctor.isVerified()) {
            return "Next available:\nToday";
        }
        return "Booking unavailable";
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    public static class DoctorViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView, specialtyTextView, locationTextView, availableTextView, ratingTextView, bookButton;

        public DoctorViewHolder(@NonNull View itemView, OnDoctorClickListener listener, List<Doctor> doctors) {
            super(itemView);
            imageView = itemView.findViewById(R.id.doctor_image);
            nameTextView = itemView.findViewById(R.id.doctor_name);
            specialtyTextView = itemView.findViewById(R.id.doctor_specialty);
            locationTextView = itemView.findViewById(R.id.doctor_location);
            availableTextView = itemView.findViewById(R.id.doctor_available);
            ratingTextView = itemView.findViewById(R.id.doctor_rating);
            bookButton = itemView.findViewById(R.id.book_button);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDoctorClick(doctors.get(position));
                }
            });

            bookButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBookDoctorClick(doctors.get(position));
                }
            });
        }
    }
}
