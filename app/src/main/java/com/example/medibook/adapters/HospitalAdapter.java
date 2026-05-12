package com.example.medibook.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.models.Hospital;
import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {

    private List<Hospital> hospitalList;
    private Context context;
    private OnHospitalActionListener listener;

    public interface OnHospitalActionListener {
        void onEditHospital(Hospital hospital);
    }

    public HospitalAdapter(List<Hospital> hospitalList, Context context, OnHospitalActionListener listener) {
        this.hospitalList = hospitalList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hospital, parent, false);
        return new HospitalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
        Hospital hospital = hospitalList.get(position);
        holder.nameText.setText(hospital.getName());
        holder.addressText.setText(hospital.getAddress());
        holder.phoneText.setText(hospital.getPhone());

        holder.editBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditHospital(hospital);
            }
        });
    }

    @Override
    public int getItemCount() {
        return hospitalList.size();
    }

    public void updateList(List<Hospital> newList) {
        this.hospitalList = newList;
        notifyDataSetChanged();
    }

    static class HospitalViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, addressText, phoneText;
        Button editBtn;

        public HospitalViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.hospital_name);
            addressText = itemView.findViewById(R.id.hospital_address);
            phoneText = itemView.findViewById(R.id.hospital_phone);
            editBtn = itemView.findViewById(R.id.edit_hospital_button);
        }
    }
}
