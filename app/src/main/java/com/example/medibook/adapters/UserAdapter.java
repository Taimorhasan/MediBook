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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onChangeRole(User user);
    }

    public UserAdapter(List<User> userList, Context context, OnUserActionListener listener) {
        this.userList = userList;
        this.context = context;
        this.listener = listener;
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
        holder.nameText.setText(user.getName());
        holder.emailText.setText(user.getEmail());
        
        String role = "PATIENT";
        if (user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
            role = user.getRoleIds().get(0);
        }
        holder.roleChip.setText(role);

        holder.changeRoleBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChangeRole(user);
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
        Button changeRoleBtn;
        ImageView userImage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.user_name);
            emailText = itemView.findViewById(R.id.user_email);
            roleChip = itemView.findViewById(R.id.role_chip);
            changeRoleBtn = itemView.findViewById(R.id.change_role_button);
            userImage = itemView.findViewById(R.id.user_image);
        }
    }
}
