package com.example.medibook.repositories;

import com.example.medibook.models.Doctor;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface DoctorCallback {
        void onSuccess(Doctor doctor);
        void onFailure(String error);
    }

    public interface DoctorsCallback {
        void onSuccess(List<Doctor> doctors);
        void onFailure(String error);
    }

    public void getDoctor(String doctorId, DoctorCallback callback) {
        if (doctorId == null || doctorId.trim().isEmpty()) {
            callback.onFailure("Doctor not found");
            return;
        }
        db.collection("doctors").document(doctorId).get()
                .addOnSuccessListener(document -> {
                    Doctor doctor = toDoctor(document);
                    if (doctor == null) {
                        callback.onFailure("Doctor not found");
                    } else {
                        callback.onSuccess(doctor);
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Doctors patients may book: verified and active. */
    public void getAllDoctors(DoctorsCallback callback) {
        // isActive is filtered here, not in the query: older documents have no isActive field.
        db.collection("doctors")
                .whereEqualTo("isVerified", true)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Doctor> doctors = new ArrayList<>();
                    for (Doctor d : toDoctors(snapshot)) {
                        if (d.isActive()) doctors.add(d);
                    }
                    callback.onSuccess(doctors);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Every doctor, for admins. */
    public void getAllDoctorsAdmin(DoctorsCallback callback) {
        db.collection("doctors").get()
                .addOnSuccessListener(snapshot -> callback.onSuccess(toDoctors(snapshot)))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void verifyDoctor(String doctorId, boolean isVerified, AuthRepository.VoidCallback callback) {
        update(doctorId, "isVerified", isVerified, callback);
    }

    public void setActive(String doctorId, boolean isActive, AuthRepository.VoidCallback callback) {
        update(doctorId, "isActive", isActive, callback);
    }

    public void assignHospital(String doctorId, String hospitalId, String hospitalName, AuthRepository.VoidCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("hospitalId", hospitalId);
        updates.put("hospitalName", hospitalName);
        db.collection("doctors").document(doctorId).update(updates)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Creates a verified doctor profile for a user an admin made a doctor, unless one exists.
     * Without it the account could never sign in as a doctor.
     */
    public void ensureDoctorProfile(String uid, String name, String email, String phone, AuthRepository.VoidCallback callback) {
        db.collection("doctors").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.onSuccess();
                        return;
                    }
                    Map<String, Object> doctor = new HashMap<>();
                    doctor.put("doctorId", uid);
                    doctor.put("name", name);
                    doctor.put("email", email);
                    doctor.put("phone", phone);
                    doctor.put("isVerified", true);
                    doctor.put("isActive", true);
                    doctor.put("createdAt", System.currentTimeMillis());
                    db.collection("doctors").document(uid).set(doctor)
                            .addOnSuccessListener(v -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private void update(String doctorId, String field, Object value, AuthRepository.VoidCallback callback) {
        db.collection("doctors").document(doctorId).update(field, value)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private static Doctor toDoctor(DocumentSnapshot document) {
        if (!document.exists()) return null;
        Doctor doctor = document.toObject(Doctor.class);
        if (doctor != null) doctor.setDoctorId(document.getId());
        return doctor;
    }

    private static List<Doctor> toDoctors(QuerySnapshot snapshot) {
        List<Doctor> doctors = new ArrayList<>();
        for (DocumentSnapshot document : snapshot) {
            Doctor d = toDoctor(document);
            if (d != null) doctors.add(d);
        }
        return doctors;
    }
}
