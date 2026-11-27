package com.example.medibook;

import android.Manifest;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.medibook.models.User;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.utils.Ui;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.medibook.activities.auth.UnifiedLoginActivity;
import com.example.medibook.fragments.AdminBookingsFragment;
import com.example.medibook.fragments.AdminDoctorsFragment;
import com.example.medibook.fragments.AdminOverviewFragment;
import com.example.medibook.fragments.AdminUsersFragment;
import com.example.medibook.fragments.AlertsFragment;
import com.example.medibook.fragments.BookingsFragment;
import com.example.medibook.fragments.DoctorTodayFragment;
import com.example.medibook.fragments.DoctorsFragment;
import com.example.medibook.fragments.HomeFragment;
import com.example.medibook.fragments.ProfileFragment;
import com.example.medibook.fragments.TabFragment;
import com.example.medibook.models.Chat;
import com.example.medibook.models.Notification;
import com.example.medibook.repositories.ChatRepository;
import com.example.medibook.repositories.NotificationRepository;
import com.example.medibook.utils.SessionManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

/**
 * The signed-in shell for every role: a fixed floating nav, and tab fragments that are
 * created once and shown or hidden, so switching tabs only swaps the content.
 */
public class MainActivity extends AppCompatActivity {
    private BottomNavigationView nav;
    private String role;
    /** Arguments for the tab being selected through the nav listener. */
    private Bundle pendingArgs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        role = new SessionManager(this).getUserRole();
        if (FirebaseAuth.getInstance().getCurrentUser() == null || role.isEmpty()) {
            startActivity(new Intent(this, UnifiedLoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        nav = findViewById(R.id.nav);
        nav.inflateMenu("admin".equals(role) ? R.menu.nav_admin
                : "doctor".equals(role) ? R.menu.nav_doctor : R.menu.nav_patient);
        // Tint each icon instead of the whole bar, so the profile tab can show a real photo.
        ColorStateList tint = ContextCompat.getColorStateList(this, R.color.selector_nav_icon);
        for (int i = 0; i < nav.getMenu().size(); i++) {
            MenuItem item = nav.getMenu().getItem(i);
            Drawable icon = DrawableCompat.wrap(item.getIcon().mutate());
            DrawableCompat.setTintList(icon, tint);
            item.setIcon(icon);
        }
        nav.setItemIconTintList(null);
        nav.setOnItemSelectedListener(item -> {
            show(item.getItemId(), null);
            return true;
        });
        nav.setOnItemReselectedListener(item -> {
            Fragment f = getSupportFragmentManager().findFragmentByTag(tag(item.getItemId()));
            if (f instanceof TabFragment) ((TabFragment) f).onReselected();
        });
        if (savedInstanceState == null) {
            show(firstTab(), null);
            askNotificationPermission();
        }
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (nav.getSelectedItemId() != firstTab()) {
                    nav.setSelectedItemId(firstTab());
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nav != null) {
            refreshAlertsBadge();
            refreshProfilePhoto();
        }
    }

    /** Shows the user's photo on the Profile (or admin More) tab once they have one. */
    public void refreshProfilePhoto() {
        int tab = nav.getMenu().findItem(R.id.nav_profile) != null ? R.id.nav_profile : R.id.nav_more;
        new UserRepository().getUser(FirebaseAuth.getInstance().getUid(), new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (isFinishing() || Ui.blank(user.getProfileImage())) return;
                int size = Math.round(Ui.dp(MainActivity.this, 26));
                Glide.with(MainActivity.this).load(user.getProfileImage()).circleCrop()
                        .into(new CustomTarget<Drawable>(size, size) {
                            @Override
                            public void onResourceReady(@NonNull Drawable photo, @Nullable Transition<? super Drawable> t) {
                                MenuItem item = nav.getMenu().findItem(tab);
                                if (item != null) item.setIcon(photo);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {}
                        });
            }

            @Override
            public void onFailure(String error) {
                // Keeps the outline icon.
            }
        });
    }

    /** Switches tab, optionally handing the tab arguments (a search, a filter). */
    public void selectTab(@IdRes int id, @Nullable Bundle args) {
        Fragment existing = getSupportFragmentManager().findFragmentByTag(tag(id));
        if (existing instanceof TabFragment && args != null) {
            ((TabFragment) existing).applyArgs(args);
        }
        if (nav.getSelectedItemId() == id) {
            if (existing == null) show(id, args);
        } else {
            pendingArgs = args;
            nav.setSelectedItemId(id);
        }
    }

    private void show(@IdRes int id, @Nullable Bundle args) {
        if (args == null) {
            args = pendingArgs;
        }
        pendingArgs = null;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction().setReorderingAllowed(true)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        for (Fragment f : fm.getFragments()) {
            tx.hide(f);
        }
        Fragment target = fm.findFragmentByTag(tag(id));
        if (target == null) {
            target = create(id);
            target.setArguments(args);
            tx.add(R.id.content, target, tag(id));
        } else {
            tx.show(target);
        }
        tx.commit();
        if (nav.getSelectedItemId() != id) {
            nav.getMenu().findItem(id).setChecked(true);
        }
    }

    private Fragment create(@IdRes int id) {
        if (id == R.id.nav_home) return new HomeFragment();
        if (id == R.id.nav_doctors) return new DoctorsFragment();
        if (id == R.id.nav_bookings) return new BookingsFragment();
        if (id == R.id.nav_alerts) return new AlertsFragment();
        if (id == R.id.nav_today) return new DoctorTodayFragment();
        if (id == R.id.nav_schedule) return new BookingsFragment(); // the doctor's view of the same screen
        if (id == R.id.nav_overview) return new AdminOverviewFragment();
        if (id == R.id.nav_admin_doctors) return new AdminDoctorsFragment();
        if (id == R.id.nav_admin_appointments) return new AdminBookingsFragment();
        if (id == R.id.nav_users) return new AdminUsersFragment();
        return new ProfileFragment(); // nav_profile and nav_more
    }

    /** Android 13+ needs consent before booking updates can appear as notifications. */
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {})
                    .launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private int firstTab() {
        return nav.getMenu().getItem(0).getItemId();
    }

    private static String tag(@IdRes int id) {
        return "tab_" + id;
    }

    public String getRole() {
        return role;
    }

    /** Unread alerts plus unread messages, on the Inbox tab (patients and doctors). */
    public void refreshAlertsBadge() {
        if (nav.getMenu().findItem(R.id.nav_alerts) == null) return;
        String uid = FirebaseAuth.getInstance().getUid();
        boolean patient = !"doctor".equals(role);
        int[] counts = new int[2]; // [unread alerts, unread messages]
        int[] pending = {2};
        Runnable done = () -> {
            if (--pending[0] == 0 && !isFinishing()) setAlertsBadge(counts[0] + counts[1]);
        };
        // The badge is a hint; on failure it shows what loaded, and the Inbox reports errors.
        new NotificationRepository().getUserNotifications(uid, new NotificationRepository.NotificationsCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                for (Notification n : notifications) {
                    if (!n.isRead()) counts[0]++;
                }
                done.run();
            }

            @Override
            public void onFailure(String error) {
                done.run();
            }
        });
        new ChatRepository().myChats(uid, patient, new ChatRepository.ChatsCallback() {
            @Override
            public void onSuccess(List<Chat> chats) {
                for (Chat c : chats) counts[1] += patient ? c.getUnreadPatient() : c.getUnreadDoctor();
                done.run();
            }

            @Override
            public void onFailure(String error) {
                done.run();
            }
        });
    }

    public void setAlertsBadge(int unread) {
        if (nav.getMenu().findItem(R.id.nav_alerts) == null) return;
        if (unread == 0) {
            nav.removeBadge(R.id.nav_alerts);
            return;
        }
        BadgeDrawable badge = nav.getOrCreateBadge(R.id.nav_alerts);
        badge.setNumber(unread);
        badge.setBackgroundColor(ContextCompat.getColor(this, R.color.mb_hot));
        badge.setBadgeTextColor(ContextCompat.getColor(this, R.color.mb_on_hot));
    }
}
