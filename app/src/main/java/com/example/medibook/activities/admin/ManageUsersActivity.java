package com.example.medibook.activities.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.activities.common.BaseActivity;
import com.example.medibook.adapters.UserAdapter;
import com.example.medibook.models.Role;
import com.example.medibook.models.User;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageUsersActivity extends BaseActivity implements UserAdapter.OnUserActionListener {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private UserRepository userRepository;
    private View emptyView;
    private List<User> userList = new ArrayList<>();
    private List<User> filteredList = new ArrayList<>();
    private com.example.medibook.repositories.RoleRepository roleRepository;
    private ProgressBar progressBar;
    private android.widget.EditText searchEditText;
    private android.widget.Spinner roleFilterSpinner;
    private List<Role> availableRoles = new ArrayList<>();
    private Map<String, String> roleIdToName = new HashMap<>();
    private Map<String, String> roleNameToId = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);
        
        // RBAC: Verify if the current user has the 'admin' role before allowing access.
        if (!checkRoleAndRedirect("admin")) {
            return;
        }

        initViews();
        userRepository = new UserRepository();
        roleRepository = new com.example.medibook.repositories.RoleRepository();
        loadRoles();
        loadUsers();
    }

    private void initViews() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("User Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.users_recycler_view);
        progressBar = findViewById(R.id.loading_progress);
        emptyView = findViewById(R.id.empty_view);
        
        searchEditText = findViewById(R.id.et_search);
        roleFilterSpinner = findViewById(R.id.role_filter_spinner);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(filteredList, this, this);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.fab_add_user).setOnClickListener(v -> showAddUserDialog());

        setupFilters();
    }

    private void showAddUserDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_user, null);
        android.widget.EditText nameET = dialogView.findViewById(R.id.et_name);
        android.widget.EditText emailET = dialogView.findViewById(R.id.et_email);
        android.widget.EditText phoneET = dialogView.findViewById(R.id.et_phone);
        android.widget.EditText passwordET = dialogView.findViewById(R.id.et_password);
        android.widget.Spinner roleSpinner = dialogView.findViewById(R.id.role_spinner);

        if (!availableRoles.isEmpty()) {
            String[] roleNames = new String[availableRoles.size()];
            for (int i = 0; i < availableRoles.size(); i++) {
                roleNames[i] = availableRoles.get(i).getRoleName();
            }
            ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roleNames);
            roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            roleSpinner.setAdapter(roleAdapter);
        }

        new AlertDialog.Builder(this)
            .setTitle("Add New User")
            .setView(dialogView)
            .setPositiveButton("Add", (dialog, which) -> {
                String name = nameET.getText().toString().trim();
                String email = emailET.getText().toString().trim();
                String phone = phoneET.getText().toString().trim();
                String password = passwordET.getText().toString().trim();
                String selectedRole = roleSpinner.getSelectedItem() != null ? roleSpinner.getSelectedItem().toString() : "";
                String roleId = roleNameToId.getOrDefault(selectedRole, selectedRole.toLowerCase());

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                createNewUser(name, email, phone, password, roleId);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    private void loadRoles() {
        roleRepository.getAllRoles(new com.example.medibook.repositories.RoleRepository.RolesCallback() {
            @Override
            public void onSuccess(List<com.example.medibook.models.Role> roles) {
                availableRoles = roles != null ? roles : new ArrayList<>();
                roleIdToName.clear();
                roleNameToId.clear();

                List<String> roleNames = new ArrayList<>();
                roleNames.add("All Roles");
                for (Role role : availableRoles) {
                    String roleName = role.getRoleName() != null ? role.getRoleName() : role.getRoleId();
                    roleNames.add(roleName);
                    roleIdToName.put(role.getRoleId().toLowerCase(), roleName);
                    roleNameToId.put(roleName, role.getRoleId().toLowerCase());
                }

                ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(ManageUsersActivity.this, android.R.layout.simple_spinner_item, roleNames);
                roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                roleFilterSpinner.setAdapter(roleAdapter);
                adapter.setRoleIdToName(roleIdToName);
                adapter.notifyDataSetChanged();
                performFiltering();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ManageUsersActivity.this, "Error loading roles: " + error, Toast.LENGTH_SHORT).show();
                List<String> fallback = new ArrayList<>();
                fallback.add("All Roles");
                fallback.add("Admin");
                fallback.add("Manager");
                fallback.add("Patient");
                ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(ManageUsersActivity.this, android.R.layout.simple_spinner_item, fallback);
                roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                roleFilterSpinner.setAdapter(roleAdapter);
                performFiltering();
            }
        });
    }
    private void createNewUser(String name, String email, String phone, String password, String role) {
        progressBar.setVisibility(View.VISIBLE);
        AuthRepository authRepository = new AuthRepository();
        authRepository.createUserByAdmin(this, name, email, phone, password, role, new AuthRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                loadUsers();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "Failed to create user: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }


    private void setupFilters() {
        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performFiltering();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        roleFilterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                performFiltering();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void performFiltering() {
        String query = searchEditText.getText().toString().toLowerCase().trim();
        String rawFilter = roleFilterSpinner.getSelectedItem() != null ? roleFilterSpinner.getSelectedItem().toString() : "All Roles";
        String roleFilterId = "";

        if (!rawFilter.equalsIgnoreCase("All Roles")) {
            roleFilterId = roleNameToId.getOrDefault(rawFilter, rawFilter.toLowerCase());
        }

        filteredList.clear();
        for (User user : userList) {
            boolean matchesQuery = (user.getName() != null && user.getName().toLowerCase().contains(query)) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(query)) ||
                    (user.getPhone() != null && user.getPhone().contains(query));

            String userRoleId = "patient";
            if (user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
                userRoleId = user.getRoleIds().get(0).toLowerCase();
            } else if (user.getRole() != null) {
                userRoleId = user.getRole().toLowerCase();
            }

            boolean matchesRole = roleFilterId.isEmpty() || userRoleId.equals(roleFilterId);

            if (matchesQuery && matchesRole) {
                filteredList.add(user);
            }
        }

        adapter.updateList(new ArrayList<>(filteredList));
        emptyView.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        userRepository.getAllUsers(new UserRepository.UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                progressBar.setVisibility(View.GONE);
                userList = users;
                performFiltering();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChangeRole(User user) {
        progressBar.setVisibility(View.VISIBLE);
        roleRepository.getAllRoles(new com.example.medibook.repositories.RoleRepository.RolesCallback() {
            @Override
            public void onSuccess(List<com.example.medibook.models.Role> roles) {
                progressBar.setVisibility(View.GONE);
                if (roles.isEmpty()) {
                    showRoleSelectionDialog(user, new ArrayList<>());
                } else {
                    showRoleSelectionDialog(user, roles);
                }
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "Error fetching roles: " + error, Toast.LENGTH_SHORT).show();
                showRoleSelectionDialog(user, new ArrayList<>());
            }
        });
    }

    @Override
    public void onImpersonateUser(User user) {
        String targetRole = "patient";
        if (user.getRole() != null) {
            targetRole = user.getRole();
        } else if (user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
            targetRole = user.getRoleIds().get(0);
        }

        sessionManager.startImpersonation(user.getUserId(), targetRole);
        
        Toast.makeText(this, "Now acting as " + user.getName() + " (" + targetRole + ")", Toast.LENGTH_SHORT).show();
        
        // Redirect to Portal Selection to see the new perspective
        android.content.Intent intent = new android.content.Intent(this, com.example.medibook.activities.auth.UnifiedLoginActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showRoleSelectionDialog(User user, List<Role> roles) {
        if (roles.isEmpty()) {
            roles = availableRoles;
        }

        if (roles.isEmpty()) {
            Toast.makeText(this, "No roles available to assign", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] roleNames = new String[roles.size()];
        for (int i = 0; i < roles.size(); i++) {
            roleNames[i] = roles.get(i).getRoleName();
        }

        final List<Role> dialogRoles = new ArrayList<>(roles);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Role for " + user.getName());
        builder.setItems(roleNames, (dialog, which) -> {
            String selectedRoleId = dialogRoles.get(which).getRoleId();
            updateUserRole(user, selectedRoleId);
        });
        builder.show();
    }

    private void updateUserRole(User user, String newRole) {
        progressBar.setVisibility(View.VISIBLE);
        userRepository.updateUserRole(user.getUserId(), newRole, new com.example.medibook.repositories.AuthRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "Role updated successfully", Toast.LENGTH_SHORT).show();
                loadUsers(); // Refresh list
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
