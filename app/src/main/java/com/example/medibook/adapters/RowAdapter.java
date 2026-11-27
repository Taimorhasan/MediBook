package com.example.medibook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibook.R;
import java.util.ArrayList;
import java.util.List;

/** Rows of avatar, title, subtitle and up to two tags (item_admin_row). The binder fills them. */
public class RowAdapter<T> extends RecyclerView.Adapter<RowAdapter.Row> {

    public interface Binder<T> {
        void bind(T item, Row row);
    }

    public interface OnClick<T> {
        void onClick(T item);
    }

    private final List<T> items = new ArrayList<>();
    private final Binder<T> binder;
    private final OnClick<T> onClick;

    public RowAdapter(Binder<T> binder, OnClick<T> onClick) {
        this.binder = binder;
        this.onClick = onClick;
    }

    public void submit(List<T> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Row onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Row(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Row row, int position) {
        T item = items.get(position);
        row.tag2.setVisibility(View.GONE);
        binder.bind(item, row);
        row.itemView.setOnClickListener(v -> onClick.onClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static final class Row extends RecyclerView.ViewHolder {
        public final View avatar;
        public final TextView title, sub, tag, tag2;

        Row(View v) {
            super(v);
            avatar = v.findViewById(R.id.row_avatar);
            title = v.findViewById(R.id.row_title);
            sub = v.findViewById(R.id.row_sub);
            tag = v.findViewById(R.id.row_tag);
            tag2 = v.findViewById(R.id.row_tag2);
        }
    }
}
