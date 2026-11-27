package com.example.medibook.activities.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.adapters.RowAdapter;
import com.example.medibook.models.Permission;
import com.example.medibook.models.Role;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.RoleRepository;
import com.example.medibook.utils.StateView;
import com.example.medibook.utils.Ui;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Admins edit roles: their name, which shell they open, and their permissions. */
public class ManageRolesActivity extends AppCompatActivity {
    private static final String[] DASHBOARDS = {"patient", "doctor", "admin"};

    private final RoleRepository repo = new RoleRepository();
    private StateView state;
    private View board;
    private RowAdapter<Role> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_list);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.manage_roles);
        toolbar.setNavigationOnClickListener(v -> finish());
        state = new StateView(findViewById(R.id.manage_state));
        board = findViewById(R.id.manage_board);
        adapter = new RowAdapter<>((r, row) -> {
            row.title.setText(name(r));
            int count = r.getPermissions() == null ? 0 : r.getPermissions().size();
            row.sub.setText(getString(R.string.permissions_count, count));
            Ui.avatar(row.avatar.findViewById(R.id.avatar_image), row.avatar.findViewById(R.id.avatar_initials), null, name(r));
            row.tag.setVisibility(View.VISIBLE);
            Ui.pill(row.tag, dashboardLabel(dashboard(r)), R.color.mb_ink, R.color.mb_panel_alt, R.color.mb_line);
        }, this::actions);
        Ui.board((RecyclerView) board.findViewById(R.id.board_list), adapter);

        MaterialButton add = findViewById(R.id.manage_bar).findViewById(R.id.bar_button);
        add.setText(R.string.add_role);
        add.setIconResource(R.drawable.ic_plus);
        add.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        add.setOnClickListener(v -> edit(null));
        load();
    }

    private static String name(Role r) {
        return Ui.blank(r.getRoleName()) ? Ui.orDash(r.getRoleId()) : r.getRoleName();
    }

    /** Roles saved before dashboardType existed fall back to their id. */
    private static String dashboard(Role r) {
        String d = !Ui.blank(r.getDashboardType()) ? r.getDashboardType() : r.getRoleId();
        d = d == null ? "" : d.toLowerCase(Locale.US);
        return d.contains("admin") ? "admin" : d.contains("doctor") ? "doctor" : "patient";
    }

    private String dashboardLabel(String d) {
        return getString("admin".equals(d) ? R.string.role_admin : "doctor".equals(d) ? R.string.role_doctor : R.string.role_patient);
    }

    private void load() {
        state.loading();
        board.setVisibility(View.GONE);
        repo.getAllRoles(new RoleRepository.RolesCallback() {
            @Override
            public void onSuccess(List<Role> roles) {
                if (isFinishing()) return;
                Collections.sort(roles, (a, b) -> name(a).compareToIgnoreCase(name(b)));
                adapter.submit(roles);
                state.hide();
                board.setVisibility(roles.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onFailure(String error) {
                if (!isFinishing()) state.error(error, ManageRolesActivity.this::load);
            }
        });
    }

    private void actions(Role r) {
        Ui.Sheet sheet = new Ui.Sheet(this, name(r), getString(R.string.permissions_count,
                r.getPermissions() == null ? 0 : r.getPermissions().size()))
                .action(R.drawable.ic_pencil_simple, getString(R.string.edit), false, () -> edit(r));
        boolean builtIn = java.util.Arrays.asList(DASHBOARDS).contains(r.getRoleId());
        if (!builtIn) { // the three built-in roles back every account and the security rules
            sheet.action(R.drawable.ic_trash, getString(R.string.delete), true, () -> new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.delete_role_confirm, name(r)))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete, (d, w) -> repo.deleteRole(r.getRoleId(), reload()))
                    .show());
        }
        sheet.show();
    }

    private void edit(@Nullable Role existing) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_role, null);
        EditText nameInput = form.findViewById(R.id.role_name);
        AutoCompleteTextView dashboard = form.findViewById(R.id.role_dashboard);
        String[] labels = new String[DASHBOARDS.length];
        for (int i = 0; i < labels.length; i++) labels[i] = dashboardLabel(DASHBOARDS[i]);
        dashboard.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels));
        dashboard.setText(dashboardLabel(existing == null ? "patient" : dashboard(existing)), false);

        List<String> current = existing == null || existing.getPermissions() == null
                ? new ArrayList<>() : existing.getPermissions();
        LinearLayout boxes = form.findViewById(R.id.role_permissions);
        List<MaterialCheckBox> checks = new ArrayList<>();
        for (String[] p : Permission.getAll()) {
            MaterialCheckBox box = new MaterialCheckBox(this);
            box.setText(p[1]);
            box.setTag(p[0]);
            box.setChecked(current.contains(p[0]));
            boxes.addView(box);
            checks.add(box);
        }
        if (existing != null) nameInput.setText(existing.getRoleName());

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(existing == null ? R.string.add_role : R.string.edit_role)
                .setView(form)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String n = nameInput.getText() == null ? "" : nameInput.getText().toString().trim();
            TextInputLayout nameLayout = form.findViewById(R.id.role_name_layout);
            nameLayout.setError(n.isEmpty() ? getString(R.string.error_required) : null);
            if (n.isEmpty()) return;

            // Keep permissions this screen doesn't list, so saving never drops them.
            List<String> permissions = new ArrayList<>(current);
            for (MaterialCheckBox box : checks) {
                String key = (String) box.getTag();
                permissions.remove(key);
                if (box.isChecked()) permissions.add(key);
            }
            String chosen = DASHBOARDS[java.util.Arrays.asList(labels).indexOf(dashboard.getText().toString())];
            Role r = existing != null ? existing : new Role(null, n, permissions);
            r.setRoleName(n);
            r.setDashboardType(chosen);
            r.setPermissions(permissions);
            dialog.dismiss();
            if (existing == null) {
                repo.createRole(r, new RoleRepository.RoleCallback() {
                    @Override public void onSuccess(Role role) { load(); }
                    @Override public void onFailure(String error) { Ui.snack(board, getString(R.string.error_generic, error)); }
                });
            } else {
                repo.updateRole(r, reload());
            }
        });
    }

    private AuthRepository.VoidCallback reload() {
        return new AuthRepository.VoidCallback() {
            @Override public void onSuccess() { load(); }
            @Override public void onFailure(String error) { Ui.snack(board, getString(R.string.error_generic, error)); }
        };
    }
}
