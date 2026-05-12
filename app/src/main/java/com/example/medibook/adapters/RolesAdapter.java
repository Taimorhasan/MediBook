package com.example.medibook.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.models.Role;
import java.util.List;

public class RolesAdapter extends RecyclerView.Adapter<RolesAdapter.ViewHolder> {

    private List<Role> roles;
    private OnRoleActionListener listener;

    public interface OnRoleActionListener {
        void onDeleteRole(Role role);
    }

    public RolesAdapter(List<Role> roles, OnRoleActionListener listener) {
        this.roles = roles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_role, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Role role = roles.get(position);
        holder.roleName.setText(role.getName());
        
        if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
            holder.permissionsSummary.setText("Permissions: " + TextUtils.join(", ", role.getPermissions()));
        } else {
            holder.permissionsSummary.setText("No permissions assigned");
        }

        holder.deleteBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteRole(role);
            }
        });
    }

    @Override
    public int getItemCount() {
        return roles.size();
    }

    public void updateList(List<Role> newList) {
        this.roles = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView roleName, permissionsSummary;
        ImageView deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            roleName = itemView.findViewById(R.id.role_name);
            permissionsSummary = itemView.findViewById(R.id.permissions_summary);
            deleteBtn = itemView.findViewById(R.id.delete_role);
        }
    }
}
