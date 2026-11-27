package com.example.medibook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.models.Appointment;
import com.example.medibook.utils.Ui;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

/** Appointment board rows for patients, doctors and admins. */
public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.Holder> {

    public enum Viewer { PATIENT, DOCTOR, ADMIN }

    public interface OnClick {
        void onClick(Appointment appointment);
    }

    /** Fills a row's inline buttons; hide both to show none. */
    public interface Actions {
        void bind(Appointment appointment, MaterialButton primary, MaterialButton secondary);
    }

    private final List<Appointment> items = new ArrayList<>();
    private final Viewer viewer;
    private final OnClick onClick;
    @Nullable private final Actions actions;
    private long seen = Long.MAX_VALUE;

    public AppointmentAdapter(Viewer viewer, OnClick onClick, @Nullable Actions actions) {
        this.viewer = viewer;
        this.onClick = onClick;
        this.actions = actions;
    }

    /** @param seen when the viewer last looked; newer changes are highlighted. */
    public void submit(List<Appointment> list, long seen) {
        items.clear();
        items.addAll(list);
        this.seen = seen;
        notifyDataSetChanged();
    }

    /** The signature move: a change the viewer hasn't seen yet keeps its highlight. */
    public static boolean changed(Appointment a, Viewer viewer, long seen) {
        if (viewer == Viewer.PATIENT) {
            // Only a status change counts for patients, not the booking they made themselves.
            return a.getUpdatedAt() - a.getCreatedAt() > 2000 && a.getUpdatedAt() > seen;
        }
        if (viewer == Viewer.DOCTOR) {
            return Math.max(a.getCreatedAt(), a.getUpdatedAt()) > seen;
        }
        return false;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Appointment a = items.get(position);
        h.day.setText(Ui.dayShort(a.getDate()));
        h.time.setText(Ui.orDash(a.getTime()));
        switch (viewer) {
            case PATIENT:
                h.title.setText(Ui.doctorName(a.getDoctorName()));
                h.sub.setText(Ui.blank(a.getDoctorSpecialty()) ? Ui.ref(a.getAppointmentId()) : a.getDoctorSpecialty());
                break;
            case DOCTOR:
                h.title.setText(Ui.orDash(a.getPatientName()));
                h.sub.setText(Ui.blank(a.getNotes()) ? Ui.ref(a.getAppointmentId()) : a.getNotes());
                break;
            default:
                h.title.setText(Ui.orDash(a.getPatientName()));
                h.sub.setText(Ui.doctorName(a.getDoctorName()));
        }
        Ui.status(h.status, a.getStatus());
        boolean changed = changed(a, viewer, seen);
        if (changed) {
            h.root.setBackgroundColor(ContextCompat.getColor(h.root.getContext(), R.color.mb_hot_bg));
        } else {
            h.root.setBackgroundResource(selectableBackground(h.root));
        }
        h.root.setOnClickListener(v -> onClick.onClick(a));

        h.primary.setVisibility(View.GONE);
        h.secondary.setVisibility(View.GONE);
        if (actions != null) actions.bind(a, h.primary, h.secondary);
        boolean anyAction = h.primary.getVisibility() == View.VISIBLE || h.secondary.getVisibility() == View.VISIBLE;
        h.actions.setVisibility(anyAction ? View.VISIBLE : View.GONE);
    }

    private static int selectableBackground(View v) {
        android.util.TypedValue tv = new android.util.TypedValue();
        v.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, tv, true);
        return tv.resourceId;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final View root, actions;
        final TextView day, time, title, sub, status;
        final MaterialButton primary, secondary;

        Holder(View v) {
            super(v);
            root = v;
            day = v.findViewById(R.id.row_day);
            time = v.findViewById(R.id.row_time);
            title = v.findViewById(R.id.row_title);
            sub = v.findViewById(R.id.row_sub);
            status = v.findViewById(R.id.row_status);
            actions = v.findViewById(R.id.row_actions);
            primary = v.findViewById(R.id.row_primary);
            secondary = v.findViewById(R.id.row_secondary);
        }
    }
}
