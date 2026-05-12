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
                    List<Hospital> hospitals = queryDocumentSnapshots.toObjects(Hospital.class);
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
}
