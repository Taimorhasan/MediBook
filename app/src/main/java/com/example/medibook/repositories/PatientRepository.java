package com.example.medibook.repositories;

import com.example.medibook.models.User;

public class PatientRepository {
    private UserRepository userRepository;

    public PatientRepository() {
        userRepository = new UserRepository();
    }

    public interface PatientCallback {
        void onSuccess(User patient);
        void onFailure(String error);
    }

    public void getPatient(String patientId, PatientCallback callback) {
        userRepository.getUser(patientId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                // Since patients are stored as users, we can return the user directly
                callback.onSuccess(user);
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }
}