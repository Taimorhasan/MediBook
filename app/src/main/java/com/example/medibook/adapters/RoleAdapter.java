package com.example.medibook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.models.Role;
import com.example.medibook.models.Permission;
import java.util.List;
import java.util.stream.Collectors;

public class RoleAdapter extends RecyclerView.Adapter<RoleAdapter.RoleViewHolder> {

    private List<Role> roles;
    private OnRoleClickListener listener;

    public interface OnRoleClickListener {
        void onEditClick(Role role);
    }

    public RoleAdapter(List<Role> roles, OnRoleClickListener listener) {
        this.roles = roles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_role, parent, false);
        return new RoleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoleViewHolder holder, int position) {
        Role role = roles.get(position);
        holder.roleName.setText(role.getRoleName());
        
        String permissions = role.getPermissions().stream()
                .map(Permission::getPermissionLabel)
                .collect(Collectors.joining(", "));
        
        holder.permissionsList.setText("Permissions: " + (permissions.isEmpty() ? "None" : permissions));
        
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(role));
    }

    @Override
    public int getItemCount() {
        return roles.size();
    }

    public void updateList(List<Role> newList) {
        this.roles = newList;
        notifyDataSetChanged();
    }

    static class RoleViewHolder extends RecyclerView.ViewHolder {
        TextView roleName, permissionsList;
        ImageView btnEdit;

        public RoleViewHolder(@NonNull View itemView) {
            super(itemView);
            roleName = itemView.findViewById(R.id.role_name);
            permissionsList = itemView.findViewById(R.id.permissions_list);
            btnEdit = itemView.findViewById(R.id.btn_edit);
        }
    }
}
