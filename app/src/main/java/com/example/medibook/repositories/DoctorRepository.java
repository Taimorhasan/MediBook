package com.example.medibook.repositories;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.medibook.models.Doctor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class DoctorRepository {
    private static final String TAG = "DoctorRepository";
    private FirebaseFirestore db;

    public DoctorRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface DoctorCallback {
        void onSuccess(Doctor doctor);
        void onFailure(String error);
    }

    public interface DoctorsCallback {
        void onSuccess(java.util.List<Doctor> doctors);
        void onFailure(String error);
    }

    public void getDoctor(String doctorId, DoctorCallback callback) {
        db.collection("doctors").document(doctorId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Doctor doctor = document.toObject(Doctor.class);
                                callback.onSuccess(doctor);
                            } else {
                                callback.onFailure("Doctor not found");
                            }
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void getAllDoctors(DoctorsCallback callback) {
        db.collection("doctors")
                .whereEqualTo("isActive", true)
                .whereEqualTo("isVerified", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            java.util.List<Doctor> doctors = new java.util.ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                Doctor doctor = document.toObject(Doctor.class);
                                if (doctor != null) {
                                    doctor.setDoctorId(document.getId());
                                    doctors.add(doctor);
                                }
                            }
                            callback.onSuccess(doctors);
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void getAllDoctorsAdmin(DoctorsCallback callback) {
        db.collection("doctors").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            java.util.List<Doctor> doctors = new java.util.ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                Doctor doctor = document.toObject(Doctor.class);
                                if (doctor != null) {
                                    doctor.setDoctorId(document.getId());
                                    doctors.add(doctor);
                                }
                            }
                            callback.onSuccess(doctors);
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }


    public void getDoctorsBySpecialty(String specialty, DoctorsCallback callback) {
        db.collection("doctors")
                .whereEqualTo("specialty", specialty)
                .whereEqualTo("isActive", true)
                .whereEqualTo("isVerified", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            java.util.List<Doctor> doctors = new java.util.ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                Doctor doctor = document.toObject(Doctor.class);
                                if (doctor != null) {
                                    doctor.setDoctorId(document.getId());
                                    doctors.add(doctor);
                                }
                            }
                            callback.onSuccess(doctors);
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void addDoctor(Doctor doctor, DoctorCallback callback) {
        db.collection("doctors").document(doctor.getDoctorId())
                .set(doctor)
                .addOnSuccessListener(aVoid -> callback.onSuccess(doctor))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateDoctor(Doctor doctor, DoctorCallback callback) {
        db.collection("doctors").document(doctor.getDoctorId())
                .set(doctor)
                .addOnSuccessListener(aVoid -> callback.onSuccess(doctor))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void verifyDoctor(String doctorId, boolean isVerified, AuthRepository.VoidCallback callback) {
        db.collection("doctors").document(doctorId)
                .update("isVerified", isVerified)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}