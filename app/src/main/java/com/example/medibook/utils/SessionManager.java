package com.example.medibook.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "MediBookSession";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_USER_ID = "user_id";
    
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public boolean saveUserSession(String userId, String role) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_ROLE, role);
        return editor.commit(); // ✅ Synchronous commit for critical session data
    }

    public String getUserRole() {
        return pref.getString(KEY_ROLE, "");
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, "");
    }

    public boolean clearSession() {
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        return editor.commit(); 
    }
}
