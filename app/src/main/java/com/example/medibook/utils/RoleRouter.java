package com.example.medibook.utils;

import android.app.Activity;
import android.content.Intent;
import com.example.medibook.MainActivity;
import com.example.medibook.R;
import com.example.medibook.activities.auth.UnifiedLoginActivity;
import com.example.medibook.models.Doctor;
import com.example.medibook.models.Role;
import com.example.medibook.models.User;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.DoctorRepository;
import com.example.medibook.repositories.NotificationRepository;
import com.example.medibook.repositories.RoleRepository;
import com.example.medibook.repositories.UserRepository;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Decides which shell a signed-in user lands in, and signs users out. */
public final class RoleRouter {
    public interface Listener {
        /** The user can't continue; they have been signed out. */
        void onBlocked(String message);
    }

    private RoleRouter() {}

    public static void route(Activity activity, String uid, Listener listener) {
        new UserRepository().getUser(uid, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                List<String> roleIds = user.getRoleIds();
                if ((roleIds == null || roleIds.isEmpty()) && !Ui.blank(user.getRole())) {
                    roleIds = Collections.singletonList(user.getRole());
                }
                if (roleIds == null || roleIds.isEmpty()) {
                    block(activity, listener, activity.getString(R.string.profile_missing));
                    return;
                }
                new RoleRepository().getRolesByIds(roleIds, new RoleRepository.RolesCallback() {
                    @Override
                    public void onSuccess(List<Role> roles) {
                        open(activity, uid, kind(roles, user), listener);
                    }

                    @Override
                    public void onFailure(String error) {
                        open(activity, uid, kind(Collections.emptyList(), user), listener);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                block(activity, listener, activity.getString(R.string.profile_missing));
            }
        });
    }

    /** Admin wins over doctor, doctor over patient. Custom roles fall back to their name. */
    static String kind(List<Role> roles, User user) {
        StringBuilder all = new StringBuilder(user.getRole() == null ? "" : user.getRole());
        for (Role r : roles) {
            all.append(' ').append(r.getDashboardType()).append(' ').append(r.getRoleName()).append(' ').append(r.getRoleId());
        }
        String s = all.toString().toLowerCase(Locale.US);
        if (s.contains("admin")) return "admin";
        if (s.contains("doctor")) return "doctor";
        return "patient";
    }

    private static void open(Activity activity, String uid, String kind, Listener listener) {
        if (!"doctor".equals(kind)) {
            go(activity, uid, kind);
            return;
        }
        new DoctorRepository().getDoctor(uid, new DoctorRepository.DoctorCallback() {
            @Override
            public void onSuccess(Doctor doctor) {
                if (doctor != null && doctor.isVerified()) {
                    go(activity, uid, kind);
                } else {
                    block(activity, listener, activity.getString(R.string.doctor_pending));
                }
            }

            @Override
            public void onFailure(String error) {
                block(activity, listener, activity.getString(R.string.doctor_pending));
            }
        });
    }

    private static void go(Activity activity, String uid, String kind) {
        new SessionManager(activity).saveUserSession(uid, kind);
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token ->
                new NotificationRepository().updateFCMToken(uid, token));
        activity.startActivity(new Intent(activity, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        activity.finish();
    }

    private static void block(Activity activity, Listener listener, String message) {
        new AuthRepository().signOut();
        new SessionManager(activity).clearSession();
        listener.onBlocked(message);
    }

    public static void signOut(Activity activity) {
        String uid = new SessionManager(activity).getUserId();
        GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
        new AuthRepository().signOutWithTokenCleanup(uid, new AuthRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                finish();
            }

            @Override
            public void onFailure(String error) {
                finish();
            }

            private void finish() {
                new SessionManager(activity).clearSession();
                activity.startActivity(new Intent(activity, UnifiedLoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                activity.finish();
            }
        });
    }
}
