package com.example.medibook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.models.Doctor;
import com.example.medibook.utils.Ui;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

/** Doctor rows for patients: who, what, experience and fee, and a Book button. */
public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.Holder> {

    public interface Listener {
        void onOpen(Doctor doctor);
        void onBook(Doctor doctor);
    }

    private final List<Doctor> items = new ArrayList<>();
    private final Listener listener;

    public DoctorAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Doctor> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    /** "8 yrs · Rs 2,500", skipping parts the doctor hasn't filled in. */
    public static String meta(View v, Doctor d) {
        List<String> parts = new ArrayList<>();
        String years = d.getExperience() == null ? "" : d.getExperience().replaceAll("[^0-9]", "");
        if (!years.isEmpty()) parts.add(v.getContext().getString(R.string.years_experience, years));
        String fee = d.getConsultationFee() == null ? "" : d.getConsultationFee().replaceAll("[^0-9]", "");
        // Over 7 digits is a typo, not a fee; leave it out rather than show a broken amount.
        if (!fee.isEmpty() && fee.length() <= 7) {
            parts.add(v.getContext().getString(R.string.fee_pkr, String.format(java.util.Locale.US, "%,d", Long.parseLong(fee))));
        }
        return android.text.TextUtils.join("  ·  ", parts);
    }

    public static String specialtyLine(Doctor d) {
        String s = Ui.orDash(d.getSpecialty());
        return Ui.blank(d.getHospitalName()) ? s : s + " · " + d.getHospitalName();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Doctor d = items.get(position);
        String name = Ui.doctorName(d.getName());
        h.name.setText(name);
        h.specialty.setText(specialtyLine(d));
        String meta = meta(h.itemView, d);
        h.meta.setText(meta);
        h.meta.setVisibility(meta.isEmpty() ? View.GONE : View.VISIBLE);
        Ui.avatar(h.avatar.findViewById(R.id.avatar_image), h.avatar.findViewById(R.id.avatar_initials),
                d.getProfileImage(), name);
        h.itemView.setOnClickListener(v -> listener.onOpen(d));
        h.book.setOnClickListener(v -> listener.onBook(d));
        h.book.setContentDescription(h.itemView.getContext().getString(R.string.book) + " " + name);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final View avatar;
        final TextView name, specialty, meta;
        final MaterialButton book;

        Holder(View v) {
            super(v);
            avatar = v.findViewById(R.id.doctor_avatar);
            name = v.findViewById(R.id.doctor_name);
            specialty = v.findViewById(R.id.doctor_specialty);
            meta = v.findViewById(R.id.doctor_meta);
            book = v.findViewById(R.id.doctor_book);
        }
    }
}
