package com.example.medibook.utils;

import android.content.Context;
import android.content.SharedPreferences;

/** The signed-in user's id and role, plus small per-user UI state. */
public class SessionManager {
    private static final String PREF_NAME = "MediBookSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE = "role";
    private static final String KEY_SEEN_PREFIX = "seen_";

    private final SharedPreferences pref;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserSession(String userId, String role) {
        pref.edit().putString(KEY_USER_ID, userId).putString(KEY_ROLE, role).apply();
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, "");
    }

    /** "admin", "doctor" or "patient"; empty when signed out. */
    public String getUserRole() {
        return pref.getString(KEY_ROLE, "");
    }

    /** When this user last looked at their bookings; drives the "changed" highlight. */
    public long getLastSeen() {
        return pref.getLong(KEY_SEEN_PREFIX + getUserId(), 0);
    }

    public void markSeen() {
        pref.edit().putLong(KEY_SEEN_PREFIX + getUserId(), System.currentTimeMillis()).apply();
    }

    public void clearSession() {
        pref.edit().remove(KEY_USER_ID).remove(KEY_ROLE).apply();
    }
}
