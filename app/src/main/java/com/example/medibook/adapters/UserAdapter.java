package com.example.medibook.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.models.User;
import com.google.android.material.chip.Chip;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;
    private OnUserActionListener listener;
    private Map<String, String> roleIdToName = new HashMap<>();

    public interface OnUserActionListener {
        void onChangeRole(User user);
        void onImpersonateUser(User user);
    }

    public UserAdapter(List<User> userList, Context context, OnUserActionListener listener) {
        this.userList = userList;
        this.context = context;
        this.listener = listener;
    }

    public void setRoleIdToName(Map<String, String> roleIdToName) {
        if (roleIdToName != null) {
            this.roleIdToName = roleIdToName;
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        
        String name = user.getName() != null ? user.getName() : "Anonymous User";
        String email = user.getEmail() != null ? user.getEmail() : "No Email";
        
        holder.nameText.setText(name);
        holder.emailText.setText(email);
        
String roleId = "patient";
        if (user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
            roleId = user.getRoleIds().get(0).toLowerCase();
        } else if (user.getRole() != null) {
            roleId = user.getRole().toLowerCase();
        }

        String displayRole = roleIdToName.getOrDefault(roleId, roleId).toUpperCase();
        holder.roleChip.setText(displayRole);

        holder.changeRoleBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChangeRole(user);
            }
        });

        holder.actAsBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImpersonateUser(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateList(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, emailText;
        Chip roleChip;
        Button changeRoleBtn, actAsBtn;
        ImageView userImage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.user_name);
            emailText = itemView.findViewById(R.id.user_email);
            roleChip = itemView.findViewById(R.id.role_chip);
            changeRoleBtn = itemView.findViewById(R.id.change_role_button);
            actAsBtn = itemView.findViewById(R.id.act_as_button);
            userImage = itemView.findViewById(R.id.user_image);
        }
    }
}
