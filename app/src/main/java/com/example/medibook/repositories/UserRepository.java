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
                    List<User> users = queryDocumentSnapshots.toObjects(User.class);
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

    public void updateUserFields(String userId, java.util.Map<String, Object> updates, UserCallback callback) {
        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> getUser(userId, callback))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
