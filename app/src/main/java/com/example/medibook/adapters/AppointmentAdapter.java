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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment_home, parent, false);
        return new AppointmentViewHolder(view, listener, appointmentList);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);

        // Set doctor name
        String doctorName = appointment.getDoctorName();
        holder.doctorNameTextView.setText(doctorName != null ? "Dr. " + doctorName : "Dr. Name");

        // Set specialty (placeholder for now - would need to get from doctor data)
        holder.specialtyTextView.setText("Specialty");

        // Set date and time
        String dateTime = formatDateTime(appointment.getDate(), appointment.getTime());
        holder.dateTimeTextView.setText(dateTime);

        // Set status
        String status = appointment.getStatus();
        if (status != null) {
            holder.statusTextView.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
            updateStatusColor(holder.statusTextView, status);
        } else {
            holder.statusTextView.setText("Pending");
            updateStatusColor(holder.statusTextView, "pending");
        }

        // Set month and day
        if (appointment.getDate() != null) {
            String[] dateParts = appointment.getDate().split("-");
            if (dateParts.length >= 3) {
                try {
                    int month = Integer.parseInt(dateParts[1]);
                    int day = Integer.parseInt(dateParts[2]);
                    holder.monthTextView.setText(getMonthAbbrev(month));
                    holder.dayTextView.setText(String.valueOf(day));
                } catch (NumberFormatException e) {
                    holder.monthTextView.setText("---");
                    holder.dayTextView.setText("--");
                }
            }
    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView doctorNameTextView, specialtyTextView, dateTimeTextView, statusTextView, monthTextView, dayTextView;

        public AppointmentViewHolder(@NonNull View itemView, OnAppointmentClickListener listener, List<Appointment> appointments) {
            super(itemView);
            doctorNameTextView = itemView.findViewById(R.id.doctor_name_text);
            specialtyTextView = itemView.findViewById(R.id.specialty_text);
            dateTimeTextView = itemView.findViewById(R.id.date_time_text);
            statusTextView = itemView.findViewById(R.id.status_text);
            monthTextView = itemView.findViewById(R.id.month_text);
            dayTextView = itemView.findViewById(R.id.day_text);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAppointmentClick(appointments.get(position));
                }
            });
        }
    }

    private String formatDateTime(String date, String time) {
        if (date == null || time == null) return "Date & Time";

        try {
            // Parse date (assuming YYYY-MM-DD format)
            String[] dateParts = date.split("-");
            if (dateParts.length >= 3) {
                int year = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]);
                int day = Integer.parseInt(dateParts[2]);

                // Format as "MMM dd, yyyy • HH:mm"
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
                java.util.Date parsedDate = new java.util.Date(year - 1900, month - 1, day);
                String formattedDate = sdf.format(parsedDate);

                return formattedDate + " • " + time;
            }
        } catch (Exception e) {
            // Fallback
        }

        return date + " • " + time;
    }

    private String getMonthAbbrev(int month) {
        String[] months = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                         "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
        if (month >= 1 && month <= 12) {
            return months[month - 1];
        }
        return "---";
    }

    private void updateStatusColor(TextView statusTextView, String status) {
        switch (status.toLowerCase()) {
            case "confirmed":
                statusTextView.setBackgroundResource(R.color.status_confirmed_bg);
                statusTextView.setTextColor(statusTextView.getContext().getColor(R.color.status_confirmed_text));
                break;
            case "pending":
                statusTextView.setBackgroundResource(R.color.status_pending_bg);
                statusTextView.setTextColor(statusTextView.getContext().getColor(R.color.status_pending_text));
                break;
            case "cancelled":
                statusTextView.setBackgroundResource(R.color.status_cancelled_bg);
                statusTextView.setTextColor(statusTextView.getContext().getColor(R.color.status_cancelled_text));
                break;
            case "completed":
                statusTextView.setBackgroundResource(R.color.status_completed_bg);
                statusTextView.setTextColor(statusTextView.getContext().getColor(R.color.status_completed_text));
                break;
            default:
                statusTextView.setBackgroundResource(R.color.status_pending_bg);
                statusTextView.setTextColor(statusTextView.getContext().getColor(R.color.status_pending_text));
                break;
        }
    }
}