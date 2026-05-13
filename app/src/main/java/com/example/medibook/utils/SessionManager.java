package com.example.medibook.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "MediBookSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IMPERSONATED_ID = "impersonated_user_id";
    private static final String KEY_IMPERSONATED_ROLE = "impersonated_role";
    private static final String KEY_ORIGINAL_ROLE = "original_role";
    private static final String KEY_PERMISSIONS = "permissions";
    
    private SharedPreferences pref;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean saveUserSession(String userId, String role) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_ORIGINAL_ROLE, role); // Keep track of original role
        return editor.commit(); 
    }

    public boolean startImpersonation(String targetUserId, String targetRole) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_IMPERSONATED_ID, targetUserId);
        editor.putString(KEY_IMPERSONATED_ROLE, targetRole);
        return editor.commit();
    }

    public boolean stopImpersonation() {
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(KEY_IMPERSONATED_ID);
        editor.remove(KEY_IMPERSONATED_ROLE);
        return editor.commit();
    }

    public boolean isImpersonating() {
        return !pref.getString(KEY_IMPERSONATED_ID, "").isEmpty();
    }

    public String getUserRole() {
        String impersonatedRole = pref.getString(KEY_IMPERSONATED_ROLE, "");
        if (!impersonatedRole.isEmpty()) return impersonatedRole;
        return pref.getString(KEY_ROLE, "");
    }

    public String getUserId() {
        String impersonatedId = pref.getString(KEY_IMPERSONATED_ID, "");
        if (!impersonatedId.isEmpty()) return impersonatedId;
        return pref.getString(KEY_USER_ID, "");
    }

    public String getOriginalRole() {
        return pref.getString(KEY_ORIGINAL_ROLE, "");
    }
    
    public String getOriginalUserId() {
        return pref.getString(KEY_USER_ID, "");
    }

    public void setPermissions(java.util.List<String> permissions) {
        SharedPreferences.Editor editor = pref.edit();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < permissions.size(); i++) {
            sb.append(permissions.get(i));
            if (i < permissions.size() - 1) sb.append(",");
        }
        editor.putString(KEY_PERMISSIONS, sb.toString());
        editor.apply();
    }

    public java.util.List<String> getPermissions() {
        String permissionsStr = pref.getString(KEY_PERMISSIONS, "");
        if (permissionsStr.isEmpty()) return new java.util.ArrayList<>();
        return java.util.Arrays.asList(permissionsStr.split(","));
    }

    public boolean hasPermission(String permission) {
        // Admins have all permissions for now or at least they should be able to bypass
        if ("admin".equalsIgnoreCase(getOriginalRole())) return true;
        
        java.util.List<String> permissions = getPermissions();
        return permissions.contains(permission);
    }

    public boolean clearSession() {
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        return editor.commit(); 
    }
}
