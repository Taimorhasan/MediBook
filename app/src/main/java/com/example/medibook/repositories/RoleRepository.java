package com.example.medibook.repositories;

import com.example.medibook.models.Role;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class RoleRepository {
    private FirebaseFirestore db;

    public RoleRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface RolesCallback {
        void onSuccess(List<Role> roles);
        void onFailure(String error);
    }

    public void getAllRoles(RolesCallback callback) {
        db.collection("roles").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Role> roles = queryDocumentSnapshots.toObjects(Role.class);
                    callback.onSuccess(roles);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void addRole(Role role) {
        db.collection("roles").document(role.getRoleId()).set(role);
    }
}
