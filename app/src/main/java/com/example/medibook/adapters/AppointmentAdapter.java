package com.example.medibook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.material.chip.Chip;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.models.Appointment;
import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointmentList;
    private OnAppointmentClickListener listener;

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
        void onCancelAppointment(Appointment appointment);
    }

    public AppointmentAdapter(List<Appointment> appointmentList, OnAppointmentClickListener listener) {
        this.appointmentList = appointmentList;
        this.listener = listener;
    }

    public void updateList(List<Appointment> newList) {
        this.appointmentList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view, listener, appointmentList);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);
        
        String doctorId = appointment.getDoctorId() != null ? appointment.getDoctorId() : "Unknown";
        String date = appointment.getDate() != null ? appointment.getDate() : "N/A";
        String time = appointment.getTime() != null ? appointment.getTime() : "N/A";
        String status = appointment.getStatus() != null ? appointment.getStatus() : "pending";

        // Display Doctor Name if available, otherwise show ID.
        String doctorDisplay = (appointment.getDoctorName() != null && !appointment.getDoctorName().isEmpty()) 
                               ? appointment.getDoctorName() 
                               : "ID: " + doctorId;
        
        holder.doctorNameTextView.setText("Doctor: " + doctorDisplay);
        holder.dateTextView.setText("Date: " + date);
        holder.timeTextView.setText("Time: " + time);
        holder.statusTextView.setText(status.toUpperCase());

        // Show cancel button only for pending/confirmed appointments
        if ("pending".equalsIgnoreCase(status) || "confirmed".equalsIgnoreCase(status)) {
            holder.cancelButton.setVisibility(View.VISIBLE);
        } else {
            holder.cancelButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView doctorNameTextView, dateTextView, timeTextView;
        Chip statusTextView;
        Button cancelButton;

        public AppointmentViewHolder(@NonNull View itemView, OnAppointmentClickListener listener, List<Appointment> appointments) {
            super(itemView);
            doctorNameTextView = itemView.findViewById(R.id.appointment_doctor_name);
            dateTextView = itemView.findViewById(R.id.appointment_date);
            timeTextView = itemView.findViewById(R.id.appointment_time);
            statusTextView = itemView.findViewById(R.id.appointment_status);
            cancelButton = itemView.findViewById(R.id.cancel_appointment_button);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAppointmentClick(appointments.get(position));
                }
            });

            cancelButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCancelAppointment(appointments.get(position));
                }
            });
        }
    }
}