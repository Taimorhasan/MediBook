package com.example.medibook.repositories;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.medibook.models.Doctor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        Doctor fallbackDoctor = findFallbackDoctor(doctorId);
        if (fallbackDoctor != null) {
            callback.onSuccess(fallbackDoctor);
            return;
        }

        db.collection("doctors").document(doctorId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Doctor doctor = document.toObject(Doctor.class);
                                if (doctor != null) {
                                    doctor.setDoctorId(document.getId());
                                }
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
                            if (doctors.isEmpty()) {
                                Log.w(TAG, "No verified active Firestore doctors found; using fallback patient booking catalog.");
                                doctors.addAll(getFallbackDoctors());
                            }
                            callback.onSuccess(doctors);
                        } else {
                            Log.w(TAG, "Failed to load Firestore doctors; using fallback patient booking catalog.", task.getException());
                            callback.onSuccess(getFallbackDoctors());
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
                            if (doctors.isEmpty()) {
                                for (Doctor doctor : getFallbackDoctors()) {
                                    if (doctor.getSpecialty() != null && doctor.getSpecialty().equalsIgnoreCase(specialty)) {
                                        doctors.add(doctor);
                                    }
                                }
                            }
                            callback.onSuccess(doctors);
                        } else {
                            List<Doctor> fallback = new ArrayList<>();
                            for (Doctor doctor : getFallbackDoctors()) {
                                if (doctor.getSpecialty() != null && doctor.getSpecialty().equalsIgnoreCase(specialty)) {
                                    fallback.add(doctor);
                                }
                            }
                            callback.onSuccess(fallback);
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

    private Doctor findFallbackDoctor(String doctorId) {
        if (doctorId == null) return null;
        for (Doctor doctor : getFallbackDoctors()) {
            if (doctorId.equals(doctor.getDoctorId())) {
                return doctor;
            }
        }
        return null;
    }

    private List<Doctor> getFallbackDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        doctors.add(createFallbackDoctor("demo-doctor-sarah-miller", "Dr. Sarah Miller", "Cardiology",
                "St. Mary's Hospital", "8 years", "4.9", Arrays.asList("Monday", "Wednesday", "Friday")));
        doctors.add(createFallbackDoctor("demo-doctor-marcus-chen", "Dr. Marcus Chen", "Pediatrics",
                "City Health Center", "10 years", "4.8", Arrays.asList("Tuesday", "Thursday", "Saturday")));
        doctors.add(createFallbackDoctor("demo-doctor-elena-rodriguez", "Dr. Elena Rodriguez", "Dermatology",
                "Skin & Laser Institute", "7 years", "5.0", Arrays.asList("Monday", "Tuesday", "Thursday")));
        doctors.add(createFallbackDoctor("demo-doctor-james-wilson", "Dr. James Wilson", "Neurology",
                "General Hospital", "12 years", "4.7", Arrays.asList("Wednesday", "Friday", "Saturday")));
        doctors.add(createFallbackDoctor("demo-doctor-aisha-khan", "Dr. Aisha Khan", "General Medicine",
                "MediBook Clinic", "6 years", "4.6", Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday")));
        return doctors;
    }

    private Doctor createFallbackDoctor(String id, String name, String specialty, String hospitalName,
                                        String experience, String rating, List<String> availableDays) {
        Doctor doctor = new Doctor(id, name, specialty, "demo-hospital", "", "",
                experience, rating, "", true);
        doctor.setDoctorId(id);
        doctor.setHospitalName(hospitalName);
        doctor.setAvailableDays(availableDays);
        doctor.setVerified(true);
        doctor.setActive(true);
        return doctor;
    }
}
