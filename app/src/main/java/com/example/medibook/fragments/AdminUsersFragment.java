package com.example.medibook.fragments;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.adapters.RowAdapter;
import com.example.medibook.models.Role;
import com.example.medibook.models.User;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.DoctorRepository;
import com.example.medibook.repositories.RoleRepository;
import com.example.medibook.repositories.UserRepository;
import com.example.medibook.utils.AccountRemoval;
import com.example.medibook.utils.Ui;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Admins see every account, change roles, and add users. */
public class AdminUsersFragment extends AdminListFragment<User> {
    private static final String[] ROLES = {"patient", "doctor", "admin"};
    private RowAdapter<User> adapter;

    @Override
    protected int title() {
        return R.string.users_title;
    }

    @Override
    protected String[][] filterOptions() {
        return new String[][]{
                {"all", getString(R.string.filter_all)},
                {"patient", getString(R.string.role_patient)},
                {"doctor", getString(R.string.role_doctor)},
                {"admin", getString(R.string.role_admin)}};
    }

    @Override
    protected String searchHint() {
        return getString(R.string.search_users);
    }

    @Override
    protected String addLabel() {
        return getString(R.string.add);
    }

    private static String roleOf(User u) {
        String r = u.getRole();
        if (Ui.blank(r) && u.getRoleIds() != null && !u.getRoleIds().isEmpty()) r = u.getRoleIds().get(0);
        return Ui.blank(r) ? "patient" : r.toLowerCase(Locale.US);
    }

    private String roleLabel(String role) {
        switch (role) {
            case "admin": return getString(R.string.role_admin);
            case "doctor": return getString(R.string.role_doctor);
            case "patient": return getString(R.string.role_patient);
            default: return role;
        }
    }

    @Override
    protected RecyclerView.Adapter<?> createAdapter() {
        adapter = new RowAdapter<>((u, row) -> {
            row.title.setText(Ui.orDash(u.getName()));
            row.sub.setText(Ui.orDash(u.getEmail()));
            Ui.avatar(row.avatar.findViewById(R.id.avatar_image), row.avatar.findViewById(R.id.avatar_initials),
                    u.getProfileImage(), u.getName());
            String role = roleOf(u);
            boolean admin = "admin".equals(role);
            Ui.pill(row.tag, roleLabel(role), admin ? R.color.mb_on_ink : R.color.mb_ink,
                    admin ? R.color.mb_ink : R.color.mb_panel_alt, admin ? R.color.mb_ink : R.color.mb_line);
        }, this::actions);
        return adapter;
    }

    @Override
    protected void fetch(Result<User> result) {
        new UserRepository().getAllUsers(new UserRepository.UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                Collections.sort(users, (a, b) -> Ui.orDash(a.getName()).compareToIgnoreCase(Ui.orDash(b.getName())));
                result.ok(users);
            }

            @Override
            public void onFailure(String error) {
                result.fail(error);
            }
        });
    }

    @Override
    protected boolean matches(User u, String filter, String query) {
        String hay = (Ui.orDash(u.getName()) + " " + Ui.orDash(u.getEmail())).toLowerCase(Locale.US);
        return ("all".equals(filter) || filter.equals(roleOf(u))) && (query.isEmpty() || hay.contains(query));
    }

    @Override
    protected void show(List<User> items) {
        adapter.submit(items);
    }

    @Override
    protected String emptyText() {
        return getString(R.string.no_users);
    }

    private void actions(User u) {
        new Ui.Sheet(requireContext(), Ui.orDash(u.getName()), u.getEmail())
                .action(R.drawable.ic_user_gear, getString(R.string.change_role), false, () -> changeRole(u))
                .action(R.drawable.ic_trash, getString(R.string.delete_user), true,
                        () -> AccountRemoval.confirm(requireView(), u.getUserId(), Ui.orDash(u.getName()),
                                "doctor".equals(roleOf(u)), this::load))
                .show();
    }

    private void changeRole(User u) {
        new RoleRepository().getAllRoles(new RoleRepository.RolesCallback() {
            @Override
            public void onSuccess(List<Role> roles) {
                if (!alive()) return;
                String[] names = new String[roles.size()];
                for (int i = 0; i < names.length; i++) {
                    Role r = roles.get(i);
                    names[i] = Ui.blank(r.getRoleName()) ? r.getRoleId() : r.getRoleName();
                }
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.change_role)
                        .setItems(names, (d, which) -> applyRole(u, roles.get(which)))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }

            @Override
            public void onFailure(String error) {
                if (alive()) Ui.snack(requireView(), getString(R.string.error_generic, error));
            }
        });
    }

    private void applyRole(User u, Role role) {
        String roleId = role.getRoleId();
        new UserRepository().updateUserRole(u.getUserId(), roleId, new AuthRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                AuthRepository.VoidCallback finish = new AuthRepository.VoidCallback() {
                    @Override
                    public void onSuccess() {
                        if (!alive()) return;
                        Ui.snack(requireView(), getString(R.string.role_changed, Ui.orDash(role.getRoleName())));
                        load();
                    }

                    @Override
                    public void onFailure(String error) {
                        if (alive()) Ui.snack(requireView(), getString(R.string.error_generic, error));
                    }
                };
                if ("doctor".equalsIgnoreCase(roleId)) {
                    new DoctorRepository().ensureDoctorProfile(u.getUserId(), u.getName(), u.getEmail(), u.getPhone(), finish);
                } else {
                    finish.onSuccess();
                }
            }

            @Override
            public void onFailure(String error) {
                if (alive()) Ui.snack(requireView(), getString(R.string.error_generic, error));
            }
        });
    }

    @Override
    protected void onAdd() {
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_user, null);
        AutoCompleteTextView role = form.findViewById(R.id.add_role);
        String[] labels = new String[ROLES.length];
        for (int i = 0; i < ROLES.length; i++) labels[i] = roleLabel(ROLES[i]);
        role.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, labels));
        role.setText(labels[0], false);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.add_user)
                .setView(form)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.add, null)
                .show();
        // Validate before closing: override the positive button so errors keep the dialog open.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(x -> {
            String name = text(form, R.id.add_name), email = text(form, R.id.add_email);
            String phone = text(form, R.id.add_phone), password = text(form, R.id.add_password);
            boolean ok = require(form, R.id.add_name_layout, !name.isEmpty(), R.string.error_required)
                    & require(form, R.id.add_email_layout, Patterns.EMAIL_ADDRESS.matcher(email).matches(), R.string.error_email)
                    & require(form, R.id.add_phone_layout, phone.isEmpty() || phone.matches("03\\d{9}"), R.string.error_phone)
                    & require(form, R.id.add_password_layout, password.length() >= 6, R.string.error_password_short);
            if (!ok) return;
            String chosen = ROLES[java.util.Arrays.asList(labels).indexOf(role.getText().toString())];
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            new AuthRepository().createUserByAdmin(requireContext(), name, email, phone, password, chosen,
                    new AuthRepository.VoidCallback() {
                        @Override
                        public void onSuccess() {
                            dialog.dismiss();
                            if (!alive()) return;
                            Ui.snack(requireView(), R.string.user_added);
                            load();
                        }

                        @Override
                        public void onFailure(String error) {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            ((TextInputLayout) form.findViewById(R.id.add_email_layout)).setError(error);
                        }
                    });
        });
    }

    private static String text(View form, int id) {
        CharSequence s = ((EditText) form.findViewById(id)).getText();
        return s == null ? "" : s.toString().trim();
    }

    private boolean require(View form, int layoutId, boolean valid, int errorRes) {
        ((TextInputLayout) form.findViewById(layoutId)).setError(valid ? null : getString(errorRes));
        return valid;
    }
}
