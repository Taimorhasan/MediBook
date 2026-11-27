package com.example.medibook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import com.example.medibook.models.Notification;
import com.example.medibook.utils.Ui;
import java.util.ArrayList;
import java.util.List;

/** Alert rows: an icon for the kind of update, the message, its age and an unread dot. */
public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.Holder> {

    public interface OnClick {
        void onClick(Notification notification);
    }

    private final List<Notification> items = new ArrayList<>();
    private final OnClick onClick;

    public AlertsAdapter(OnClick onClick) {
        this.onClick = onClick;
    }

    public void submit(List<Notification> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    private static int icon(String type) {
        String t = type == null ? "" : type;
        if (t.endsWith("confirmed")) return R.drawable.ic_check_circle;
        if (t.endsWith("completed")) return R.drawable.ic_check;
        if (t.endsWith("cancelled")) return R.drawable.ic_calendar_x;
        if (t.endsWith("request")) return R.drawable.ic_calendar_plus;
        if (t.endsWith("booked")) return R.drawable.ic_hourglass_medium;
        return R.drawable.ic_bell;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Notification n = items.get(position);
        h.icon.setImageResource(icon(n.getType()));
        h.title.setText(Ui.orDash(n.getTitle()));
        h.body.setText(n.getMessage());
        h.time.setText(Ui.relativeTime(h.itemView.getContext(), n.getTimestamp()));
        h.dot.setVisibility(n.isRead() ? View.INVISIBLE : View.VISIBLE);
        h.title.setTextColor(ContextCompat.getColor(h.itemView.getContext(), n.isRead() ? R.color.mb_mute : R.color.mb_ink));
        h.itemView.setOnClickListener(v -> onClick.onClick(n));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView title, body, time;
        final View dot;

        Holder(View v) {
            super(v);
            icon = v.findViewById(R.id.alert_icon);
            title = v.findViewById(R.id.alert_title);
            body = v.findViewById(R.id.alert_body);
            time = v.findViewById(R.id.alert_time);
            dot = v.findViewById(R.id.alert_dot);
        }
    }
}
