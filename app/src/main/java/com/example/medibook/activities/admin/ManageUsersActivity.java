package com.example.medibook.activities.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.activities.common.BaseActivity;
import com.example.medibook.adapters.UserAdapter;
import com.example.medibook.models.User;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.UserRepository;
import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends BaseActivity implements UserAdapter.OnUserActionListener {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private UserRepository userRepository;
    private View emptyView;
    private List<User> userList = new ArrayList<>();
    private com.example.medibook.repositories.RoleRepository roleRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);
        
        // RBAC: Verify if the current user has the 'admin' role before allowing access.
        // Redirects to PortalSelectionActivity if the role is unauthorized or session is invalid.
        if (!checkRoleAndRedirect("admin")) {
            return;
        }

        initViews();
        userRepository = new UserRepository();
        roleRepository = new com.example.medibook.repositories.RoleRepository();
        loadUsers();
    }

    private void initViews() {
        // Fix for Stability: Find the toolbar by its standard ID 'toolbar' defined in toolbar_main.xml.
        // The overriding ID 'toolbar_layout' was removed from the XML to prevent NullPointerExceptions.
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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(userList, this, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        userRepository.getAllUsers(new UserRepository.UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                progressBar.setVisibility(View.GONE);
                userList = users;
                adapter.updateList(userList);
                emptyView.setVisibility(userList.isEmpty() ? View.VISIBLE : View.GONE);
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
                    // Fallback to defaults if no custom roles exist
                    showRoleSelectionDialog(user, new String[]{"PATIENT", "DOCTOR", "ADMIN"});
                } else {
                    String[] roleNames = new String[roles.size()];
                    for (int i = 0; i < roles.size(); i++) {
                        roleNames[i] = roles.get(i).getName().toUpperCase();
                    }
                    showRoleSelectionDialog(user, roleNames);
                }
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "Error fetching roles: " + error, Toast.LENGTH_SHORT).show();
                // Fallback
                showRoleSelectionDialog(user, new String[]{"PATIENT", "DOCTOR", "ADMIN"});
            }
        });
    }

    private void showRoleSelectionDialog(User user, String[] roles) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Role for " + user.getName());
        builder.setItems(roles, (dialog, which) -> {
            String selectedRole = roles[which].toLowerCase();
            updateUserRole(user, selectedRole);
        });
        builder.show();
    }

    private void updateUserRole(User user, String newRole) {
        progressBar.setVisibility(View.VISIBLE);
        userRepository.updateUserRole(user.getUserId(), newRole, new AuthRepository.VoidCallback() {
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
