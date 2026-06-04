package com.example.medibook.repositories;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.medibook.models.Appointment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AppointmentRepository {
    private static final String TAG = "AppointmentRepository";
    private static final Set<String> VALID_STATUSES = new HashSet<>(
            Arrays.asList("pending", "confirmed", "cancelled", "completed"));
    private FirebaseFirestore db;

    public AppointmentRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface AppointmentCallback {
        void onSuccess(Appointment appointment);
        void onFailure(String error);
    }

    public interface AppointmentsCallback {
        void onSuccess(java.util.List<Appointment> appointments);
        void onFailure(String error);
    }

    public void bookAppointment(Appointment appointment, AppointmentCallback callback) {
        String validationError = validateAppointmentForBooking(appointment);
        if (validationError != null) {
            callback.onFailure(validationError);
            return;
        }

        db.collection("appointments")
                .whereEqualTo("doctorId", appointment.getDoctorId())
                .whereEqualTo("date", appointment.getDate())
                .whereEqualTo("slotTime", appointment.getSlotTime())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot) {
                        Appointment existing = document.toObject(Appointment.class);
                        if (existing != null && isActiveBookingStatus(existing.getStatus())) {
                            callback.onFailure("This time slot is already booked. Please choose another slot.");
                            return;
                        }
                    }

                    db.collection("appointments").document(appointment.getAppointmentId())
                            .set(appointment)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(appointment))
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getAppointment(String appointmentId, AppointmentCallback callback) {
        db.collection("appointments").document(appointmentId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Appointment appointment = document.toObject(Appointment.class);
                                callback.onSuccess(appointment);
                            } else {
                                callback.onFailure("Appointment not found");
                            }
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void getPatientAppointments(String patientId, AppointmentsCallback callback) {
        db.collection("appointments")
                .whereEqualTo("patientId", patientId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            java.util.List<Appointment> appointments = new java.util.ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                Appointment appointment = document.toObject(Appointment.class);
                                if (appointment != null) {
                                    appointments.add(appointment);
                                }
                            }
                            callback.onSuccess(appointments);
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void getDoctorAppointments(String doctorId, AppointmentsCallback callback) {
        db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            java.util.List<Appointment> appointments = new java.util.ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                Appointment appointment = document.toObject(Appointment.class);
                                if (appointment != null) {
                                    appointments.add(appointment);
                                }
                            }
                            callback.onSuccess(appointments);
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void getAllAppointments(AppointmentsCallback callback) {
        db.collection("appointments")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            java.util.List<Appointment> appointments = new java.util.ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                Appointment appointment = document.toObject(Appointment.class);
                                if (appointment != null) {
                                    appointments.add(appointment);
                                }
                            }
                            callback.onSuccess(appointments);
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void updateAppointmentStatus(String appointmentId, String status, AppointmentCallback callback) {
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            callback.onFailure("Appointment ID is required");
            return;
        }

        if (status == null || !VALID_STATUSES.contains(status.toLowerCase())) {
            callback.onFailure("Invalid appointment status");
            return;
        }

        db.collection("appointments").document(appointmentId)
                .update("status", status.toLowerCase(), "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    // Fetch updated appointment
                    getAppointment(appointmentId, callback);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void cancelAppointment(String appointmentId, AppointmentCallback callback) {
        updateAppointmentStatus(appointmentId, "cancelled", callback);
    }

    public void confirmAppointment(String appointmentId, AppointmentCallback callback) {
        updateAppointmentStatus(appointmentId, "confirmed", callback);
    }

    public void completeAppointment(String appointmentId, AppointmentCallback callback) {
        updateAppointmentStatus(appointmentId, "completed", callback);
    }

    private String validateAppointmentForBooking(Appointment appointment) {
        if (appointment == null) return "Appointment details are required";
        if (isBlank(appointment.getAppointmentId())) return "Appointment ID is required";
        if (isBlank(appointment.getPatientId())) return "Patient is required";
        if (isBlank(appointment.getPatientName())) return "Patient name is required";
        if (isBlank(appointment.getDoctorId())) return "Doctor is required";
        if (isBlank(appointment.getDoctorName())) return "Doctor name is required";
        if (isBlank(appointment.getDate())) return "Appointment date is required";
        if (isBlank(appointment.getSlotTime())) return "Appointment time is required";
        if (!appointment.getDate().matches("\\d{4}-\\d{2}-\\d{2}")) return "Appointment date must use YYYY-MM-DD format";
        if (!appointment.getSlotTime().matches("(?i)^(0?[1-9]|1[0-2]):[0-5][0-9]\\s?(AM|PM)$")) return "Appointment time must use HH:MM AM/PM format";
        if (isBlank(appointment.getStatus())) return "Appointment status is required";
        if (!VALID_STATUSES.contains(appointment.getStatus().toLowerCase())) return "Invalid appointment status";
        return null;
    }

    private boolean isActiveBookingStatus(String status) {
        return status == null || "pending".equalsIgnoreCase(status) || "confirmed".equalsIgnoreCase(status);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
