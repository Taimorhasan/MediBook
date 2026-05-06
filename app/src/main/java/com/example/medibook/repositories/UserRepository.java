package com.example.medibook.repositories;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.medibook.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private FirebaseFirestore db;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface UsersCallback {
        void onSuccess(java.util.List<User> users);
        void onFailure(String error);
    }

    public void getUser(String userId, UserCallback callback) {
        db.collection("users").document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User user = document.toObject(User.class);
                                callback.onSuccess(user);
                            } else {
                                callback.onFailure("User not found");
                            }
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void getAllUsers(UsersCallback callback) {
        db.collection("users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            java.util.List<User> users = new java.util.ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                User user = document.toObject(User.class);
                                if (user != null) {
                                    users.add(user);
                                }
                            }
                            callback.onSuccess(users);
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    public void updateUser(User user, UserCallback callback) {
        db.collection("users").document(user.getUserId())
                .set(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(user))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}