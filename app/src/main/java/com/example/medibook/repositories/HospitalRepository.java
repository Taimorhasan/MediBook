package com.example.medibook.repositories;

import com.example.medibook.models.Hospital;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class HospitalRepository {
    private FirebaseFirestore db;

    public HospitalRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface HospitalCallback {
        void onSuccess(Hospital hospital);
        void onFailure(String error);
    }

    public interface HospitalsCallback {
        void onSuccess(List<Hospital> hospitals);
        void onFailure(String error);
    }

    public void getAllHospitals(HospitalsCallback callback) {
        db.collection("hospitals").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Hospital> hospitals = new java.util.ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        Hospital h = doc.toObject(Hospital.class);
                        if (h == null) continue;
                        h.setHospitalId(doc.getId()); // older documents don't store their own id
                        hospitals.add(h);
                    }
                    callback.onSuccess(hospitals);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void addHospital(Hospital hospital, HospitalCallback callback) {
        db.collection("hospitals").document(hospital.getHospitalId())
                .set(hospital)
                .addOnSuccessListener(aVoid -> callback.onSuccess(hospital))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateHospital(Hospital hospital, AuthRepository.VoidCallback callback) {
        db.collection("hospitals").document(hospital.getHospitalId())
                .set(hospital)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteHospital(String hospitalId, AuthRepository.VoidCallback callback) {
        db.collection("hospitals").document(hospitalId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
