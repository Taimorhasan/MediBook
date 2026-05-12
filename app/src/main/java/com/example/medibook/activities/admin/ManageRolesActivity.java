package com.example.medibook.activities.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.activities.common.BaseActivity;
import com.example.medibook.adapters.PermissionsAdapter;
import com.example.medibook.adapters.RolesAdapter;
import com.example.medibook.models.Permission;
import com.example.medibook.models.Role;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.RoleRepository;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ManageRolesActivity extends BaseActivity implements RolesAdapter.OnRoleActionListener {

    private EditText roleNameEditText;
    private RecyclerView permissionsRecyclerView, rolesRecyclerView;
    private MaterialButton createRoleButton;
    private ProgressBar progressBar;
    private View emptyStateView;

    private PermissionsAdapter permissionsAdapter;
    private RolesAdapter rolesAdapter;
    private RoleRepository roleRepository;
    private List<Role> rolesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_roles);

        if (!checkRoleAndRedirect("admin")) {
            return;
        }

        initViews();
        setupToolbar();
        roleRepository = new RoleRepository();
        loadRoles();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Roles");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        roleNameEditText = findViewById(R.id.role_name_edit_text);
        permissionsRecyclerView = findViewById(R.id.permissions_recycler_view);
        rolesRecyclerView = findViewById(R.id.roles_recycler_view);
        createRoleButton = findViewById(R.id.create_role_button);
        progressBar = findViewById(R.id.loading_progress);
        emptyStateView = findViewById(R.id.empty_state_view);

        // Setup Permissions List
        permissionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        permissionsAdapter = new PermissionsAdapter(Permission.getAllPermissions());
        permissionsRecyclerView.setAdapter(permissionsAdapter);

        // Setup Roles List
        rolesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rolesAdapter = new RolesAdapter(rolesList, this);
        rolesRecyclerView.setAdapter(rolesAdapter);

        createRoleButton.setOnClickListener(v -> createRole());
    }

    private void loadRoles() {
        progressBar.setVisibility(View.VISIBLE);
        roleRepository.getAllRoles(new RoleRepository.RolesCallback() {
            @Override
            public void onSuccess(List<Role> roles) {
                progressBar.setVisibility(View.GONE);
                rolesList = roles;
                rolesAdapter.updateList(rolesList);
                emptyStateView.setVisibility(rolesList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageRolesActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createRole() {
        String roleName = roleNameEditText.getText().toString().trim();
        List<String> selectedPermissions = permissionsAdapter.getSelectedPermissions();

        if (roleName.isEmpty()) {
            roleNameEditText.setError("Role name required");
            return;
        }

        if (selectedPermissions.isEmpty()) {
            Toast.makeText(this, "Please select at least one permission", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String roleId = UUID.randomUUID().toString();
        Role newRole = new Role(roleId, roleName, selectedPermissions);

        roleRepository.addRole(newRole, new RoleRepository.RoleCallback() {
            @Override
            public void onSuccess(Role role) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageRolesActivity.this, "Role created successfully", Toast.LENGTH_SHORT).show();
                roleNameEditText.setText("");
                permissionsAdapter.clearSelection();
                loadRoles();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageRolesActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteRole(Role role) {
        if (role.getName().equalsIgnoreCase("admin")) {
            Toast.makeText(this, "Cannot delete system admin role", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        roleRepository.deleteRole(role.getRoleId(), new AuthRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageRolesActivity.this, "Role deleted", Toast.LENGTH_SHORT).show();
                loadRoles();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageRolesActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
