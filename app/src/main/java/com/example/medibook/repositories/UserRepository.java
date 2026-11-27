package com.example.medibook.repositories;

import com.example.medibook.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.ArrayList;

public class UserRepository {
    private FirebaseFirestore db;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface UsersCallback {
        void onSuccess(List<User> users);
        void onFailure(String error);
    }

    public void getAllUsers(UsersCallback callback) {
        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        User u = doc.toObject(User.class);
                        if (u == null) continue;
                        u.setUserId(doc.getId()); // delete and role changes rely on the real document id
                        users.add(u);
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateUserRole(String userId, String role, AuthRepository.VoidCallback callback) {
        db.collection("users").document(userId)
                .update("role", role, "roleIds", java.util.Arrays.asList(role))
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getUser(String userId, UserCallback callback) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("User not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Removes an account's MediBook data: users/{uid}, and doctors/{uid} for doctors, in one batch.
     * The Firebase Auth login itself can only be deleted server-side; without a profile the app blocks it.
     */
    public void deleteAccountData(String userId, boolean doctor, AuthRepository.VoidCallback callback) {
        com.google.firebase.firestore.WriteBatch batch = db.batch();
        batch.delete(db.collection("users").document(userId));
        if (doctor) batch.delete(db.collection("doctors").document(userId));
        batch.commit()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateUserFields(String userId, java.util.Map<String, Object> updates, UserCallback callback) {
        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> getUser(userId, callback))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
