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

    /** The id is the lower-cased name, because users store it and the security rules read roles/{id}. */
    public void createRole(Role role, RoleCallback callback) {
        String id = role.getRoleName().trim().toLowerCase(java.util.Locale.US).replaceAll("[^a-z0-9]+", "_");
        role.setRoleId(id);
        db.collection(COLLECTION_ROLES).document(id).set(role)
                .addOnSuccessListener(v -> callback.onSuccess(role))
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
