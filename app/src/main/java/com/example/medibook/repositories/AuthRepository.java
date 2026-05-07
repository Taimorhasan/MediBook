package com.example.medibook.repositories;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.medibook.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // 🔹 Callback interface for auth operations
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String error);
    }

    // 🔹 Callback interfaces for additional operations
    public interface UserExistsCallback {
        void onResult(boolean exists);
    }
    
    public interface RoleCallback {
        void onResult(String role);
    }
    
    public interface VoidCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // 🔹 Sign up with email/password + save user to Firestore
    public void signUp(String name, String email, String phone, String password, String role, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // Create user document in Firestore
                        createUserProfile(user.getUid(), name, email, phone, role, new VoidCallback() {
                            @Override
                            public void onSuccess() {
                                callback.onSuccess(user);
                            }
                            @Override
                            public void onFailure(String error) {
                                // Profile creation failed, but auth succeeded
                                callback.onSuccess(user);
                            }
                        });
                    } else {
                        callback.onFailure("User is null after successful signup");
                    }
                } else {
                    callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Unknown error");
                }
            });
    }

    // 🔹 Sign in with email/password
    public void signIn(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("User is null after successful sign-in");
                    }
                } else {
                    callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Unknown error");
                }
            });
    }

    // 🔹 Sign in with Google Credential
    public void signInWithCredential(AuthCredential credential, AuthCallback callback) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("Google sign-in succeeded but no user found");
                    }
                } else {
                    callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Unknown error");
                }
            });
    }

    // 🔹 Create user profile in Firestore
    public void createUserProfile(String uid, String name, String email, String phone, String role, VoidCallback callback) {
        Map<String, Object> user = new HashMap<>();
        user.put("userId", uid);
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);
        user.put("role", role);
        user.put("roleIds", Arrays.asList(role));
        user.put("createdAt", System.currentTimeMillis());
        user.put("updatedAt", System.currentTimeMillis());

        db.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // 🔹 Check if user document exists in Firestore
    public void checkUserExists(String userId, UserExistsCallback callback) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                callback.onResult(documentSnapshot.exists());
            })
            .addOnFailureListener(e -> callback.onResult(false));
    }

    // 🔹 Get user role from Firestore
    public void getUserRole(String userId, RoleCallback callback) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String role = documentSnapshot.getString("role");
                    callback.onResult(role != null ? role : "patient");
                } else {
                    callback.onResult("patient"); // Default role
                }
            })
            .addOnFailureListener(e -> callback.onResult("patient"));
    }

    // 🔹 Create patient profile (for Google Sign-Up auto-registration)
    public void createPatientProfile(String uid, String name, String email, String phone, VoidCallback callback) {
        createUserProfile(uid, name, email, phone, "patient", callback);
    }

    // 🔹 Sign out
    public void signOut() {
        mAuth.signOut();
    }

    // 🔹 Get current user
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    // 🔹 Check if user is logged in
    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }
}