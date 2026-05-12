package com.example.medibook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import java.util.ArrayList;
import java.util.List;

public class PermissionsAdapter extends RecyclerView.Adapter<PermissionsAdapter.ViewHolder> {

    private List<String> allPermissions;
    private List<String> selectedPermissions = new ArrayList<>();

    public PermissionsAdapter(List<String> allPermissions) {
        this.allPermissions = allPermissions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_permission, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String permission = allPermissions.get(position);
        holder.nameText.setText(permission.replace("_", " "));
        
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedPermissions.contains(permission));
        
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedPermissions.contains(permission)) {
                    selectedPermissions.add(permission);
                }
            } else {
                selectedPermissions.remove(permission);
            }
        });
    }

    @Override
    public int getItemCount() {
        return allPermissions.size();
    }

    public List<String> getSelectedPermissions() {
        return selectedPermissions;
    }

    public void clearSelection() {
        selectedPermissions.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView nameText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.permission_checkbox);
            nameText = itemView.findViewById(R.id.permission_name);
        }
    }
}
