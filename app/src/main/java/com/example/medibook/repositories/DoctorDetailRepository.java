package com.example.medibook.repositories;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.medibook.models.Doctor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DoctorDetailRepository {
    private static final String TAG = "DoctorDetailRepository";
    private FirebaseFirestore db;

    public DoctorDetailRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface DoctorCallback {
        void onSuccess(Doctor doctor);
        void onFailure(String error);
    }

    public interface VoidCallback {
        void onSuccess();
        void onFailure(String error);
    }

    /**
     * Create or update doctor profile
     */
    public void saveDoctorProfile(String userId, Doctor doctor, VoidCallback callback) {
        doctor.setDoctorId(userId); // Ensure doctor ID matches user ID
        
        db.collection("doctors").document(userId)
                .set(doctor)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Doctor profile saved");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error saving doctor profile", e);
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Get doctor profile by user ID
     */
    public void getDoctorProfile(String userId, DoctorCallback callback) {
        db.collection("doctors").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Doctor doctor = document.toObject(Doctor.class);
                                callback.onSuccess(doctor);
                            } else {
                                callback.onFailure("Doctor profile not found");
                            }
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    /**
     * Update doctor availability
     */
    public void updateAvailability(String doctorId, java.util.List<String> availableDays, VoidCallback callback) {
        db.collection("doctors").document(doctorId)
                .update("availableDays", availableDays)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Availability updated");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating availability", e);
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Get all appointments for doctor
     */
    public void getDoctorAppointments(String doctorId, AppointmentListCallback callback) {
        db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    java.util.List<com.example.medibook.models.Appointment> appointments = querySnapshot.toObjects(com.example.medibook.models.Appointment.class);
                    callback.onSuccess(appointments);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error fetching appointments", e);
                    callback.onFailure(e.getMessage());
                });
    }

    public interface AppointmentListCallback {
        void onSuccess(java.util.List<com.example.medibook.models.Appointment> appointments);
        void onFailure(String error);
    }
}
