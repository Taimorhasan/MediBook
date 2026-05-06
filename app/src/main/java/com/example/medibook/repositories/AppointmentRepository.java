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

public class AppointmentRepository {
    private static final String TAG = "AppointmentRepository";
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
        db.collection("appointments").document(appointment.getAppointmentId())
                .set(appointment)
                .addOnSuccessListener(aVoid -> callback.onSuccess(appointment))
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
        db.collection("appointments").document(appointmentId)
                .update("status", status, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    // Fetch updated appointment
                    getAppointment(appointmentId, callback);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void cancelAppointment(String appointmentId, AppointmentCallback callback) {
        updateAppointmentStatus(appointmentId, "cancelled", callback);
    }
}