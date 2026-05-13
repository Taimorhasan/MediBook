package com.example.medibook.activities.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.adapters.RoleAdapter;
import com.example.medibook.models.Permission;
import com.example.medibook.models.Role;
import com.example.medibook.repositories.RoleRepository;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManageRolesActivity extends AppCompatActivity implements RoleAdapter.OnRoleClickListener {

    private RecyclerView recyclerView;
    private RoleAdapter adapter;
    private RoleRepository roleRepository;
    private List<Role> fullRoleList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView emptyText;
    private TextInputEditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_roles);

        roleRepository = new RoleRepository();
        
        recyclerView = findViewById(R.id.recycler_view_roles);
        progressBar = findViewById(R.id.progress_bar);
        emptyText = findViewById(R.id.empty_text);
        searchEditText = findViewById(R.id.search_edit_text);
        
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_role).setOnClickListener(v -> showRoleDialog(null));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoleAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        loadRoles();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRoles(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadRoles() {
        progressBar.setVisibility(View.VISIBLE);
        roleRepository.getAllRoles(new RoleRepository.RolesCallback() {
            @Override
            public void onSuccess(List<Role> roles) {
                progressBar.setVisibility(View.GONE);
                fullRoleList = roles;
                updateUI(roles);
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageRolesActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(List<Role> roles) {
        if (roles.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.updateList(roles);
        }
    }

    private void filterRoles(String query) {
        List<Role> filtered = fullRoleList.stream()
                .filter(r -> r.getRoleName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        updateUI(filtered);
    }

    @Override
    public void onEditClick(Role role) {
        showRoleDialog(role);
    }

    private void showRoleDialog(Role roleToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_role, null);
        
        EditText etRoleName = view.findViewById(R.id.et_role_name);
        Spinner spinnerDashboardType = view.findViewById(R.id.spinner_dashboard_type);
        LinearLayout permissionsContainer = view.findViewById(R.id.permissions_container);
        
        // Setup dashboard type spinner
        String[] dashboardTypes = {"", "admin", "doctor", "patient"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dashboardTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDashboardType.setAdapter(adapter);
        
        List<String> allPermissions = Permission.getAllPermissions();
        List<CheckBox> checkBoxes = new ArrayList<>();

        for (String perm : allPermissions) {
            CheckBox cb = new CheckBox(this);
            cb.setText(Permission.getPermissionLabel(perm));
            cb.setTag(perm);
            if (roleToEdit != null && roleToEdit.getPermissions().contains(perm)) {
                cb.setChecked(true);
            }
            permissionsContainer.addView(cb);
            checkBoxes.add(cb);
        }

        if (roleToEdit != null) {
            etRoleName.setText(roleToEdit.getRoleName());
            // Set dashboard type selection
            String currentDashboardType = roleToEdit.getDashboardType() != null ? roleToEdit.getDashboardType() : "";
            for (int i = 0; i < dashboardTypes.length; i++) {
                if (dashboardTypes[i].equals(currentDashboardType)) {
                    spinnerDashboardType.setSelection(i);
                    break;
                }
            }
            builder.setTitle("Edit Role");
        } else {
            builder.setTitle("Add New Role");
        }

        builder.setView(view);
        builder.setPositiveButton(roleToEdit != null ? "Update" : "Add", (dialog, which) -> {
            String name = etRoleName.getText().toString().trim();
            String selectedDashboardType = spinnerDashboardType.getSelectedItem().toString();
            
            if (name.isEmpty()) {
                Toast.makeText(this, "Role name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> selectedPermissions = checkBoxes.stream()
                    .filter(CheckBox::isChecked)
                    .map(cb -> (String) cb.getTag())
                    .collect(Collectors.toList());

            if (roleToEdit != null) {
                roleToEdit.setRoleName(name);
                roleToEdit.setDashboardType(selectedDashboardType.isEmpty() ? null : selectedDashboardType);
                roleToEdit.setPermissions(selectedPermissions);
                updateRole(roleToEdit);
            } else {
                Role newRole = new Role(null, name, selectedPermissions);
                newRole.setDashboardType(selectedDashboardType.isEmpty() ? null : selectedDashboardType);
                createRole(newRole);
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        if (roleToEdit != null) {
            builder.setNeutralButton("Delete", (dialog, which) -> deleteRole(roleToEdit.getRoleId()));
        }
        
        builder.show();
    }

    private void createRole(Role role) {
        roleRepository.createRole(role, new RoleRepository.RoleCallback() {
            @Override
            public void onSuccess(Role createdRole) {
                Toast.makeText(ManageRolesActivity.this, "Role created", Toast.LENGTH_SHORT).show();
                loadRoles();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ManageRolesActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRole(Role role) {
        roleRepository.updateRole(role, new com.example.medibook.repositories.AuthRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ManageRolesActivity.this, "Role updated", Toast.LENGTH_SHORT).show();
                loadRoles();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ManageRolesActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteRole(String roleId) {
        roleRepository.deleteRole(roleId, new com.example.medibook.repositories.AuthRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ManageRolesActivity.this, "Role deleted", Toast.LENGTH_SHORT).show();
                loadRoles();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ManageRolesActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
