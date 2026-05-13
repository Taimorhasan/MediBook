package com.example.medibook.repositories;

import com.example.medibook.models.Role;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class RoleRepository {
    private FirebaseFirestore db;
    private static final String COLLECTION_ROLES = "roles";

    public RoleRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface RolesCallback {
        void onSuccess(List<Role> roles);
        void onFailure(String error);
    }

    public interface RoleCallback {
        void onSuccess(Role role);
        void onFailure(String error);
    }

    public void getAllRoles(RolesCallback callback) {
        db.collection(COLLECTION_ROLES).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Role> roles = new ArrayList<>();
                    queryDocumentSnapshots.forEach(doc -> {
                        Role role = doc.toObject(Role.class);
                        role.setRoleId(doc.getId());
                        roles.add(role);
                    });
                    callback.onSuccess(roles);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void createRole(Role role, RoleCallback callback) {
        db.collection(COLLECTION_ROLES).add(role)
                .addOnSuccessListener(documentReference -> {
                    role.setRoleId(documentReference.getId());
                    callback.onSuccess(role);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateRole(Role role, AuthRepository.VoidCallback callback) {
        db.collection(COLLECTION_ROLES).document(role.getRoleId()).set(role)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteRole(String roleId, AuthRepository.VoidCallback callback) {
        db.collection(COLLECTION_ROLES).document(roleId).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void addRole(Role role) {
        if (role.getRoleId() != null && !role.getRoleId().isEmpty()) {
            db.collection(COLLECTION_ROLES).document(role.getRoleId()).set(role);
        } else {
            db.collection(COLLECTION_ROLES).add(role);
        }
    }

    public void getRolesByIds(List<String> roleIds, RolesCallback callback) {
        if (roleIds == null || roleIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        db.collection(COLLECTION_ROLES)
                .whereIn(com.google.firebase.firestore.FieldPath.documentId(), roleIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Role> roles = new ArrayList<>();
                    queryDocumentSnapshots.forEach(doc -> {
                        Role role = doc.toObject(Role.class);
                        role.setRoleId(doc.getId());
                        roles.add(role);
                    });
                    callback.onSuccess(roles);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
