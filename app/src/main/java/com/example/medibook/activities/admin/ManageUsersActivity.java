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
    private ProgressBar progressBar;
    private List<User> userList = new ArrayList<>();

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
        String[] roles = {"PATIENT", "DOCTOR", "ADMIN"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Role for " + user.getName());
        builder.setItems(roles, (dialog, which) -> {
            String selectedRole = roles[which];
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
