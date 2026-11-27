package com.example.medibook.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.medibook.R;
import com.example.medibook.models.Message;
import com.example.medibook.utils.Ui;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Chat bubbles: mine on the right in ink, theirs on the left on a panel, a day label when the day changes. */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.Holder> {

    public interface OnImage {
        void open(String url);
    }

    private final List<Message> items = new ArrayList<>();
    private final String me;
    private final OnImage onImage;
    private final SimpleDateFormat time = new SimpleDateFormat("h:mm a", Locale.US);
    private final SimpleDateFormat day = new SimpleDateFormat("EEE d MMM", Locale.US);

    public MessageAdapter(String me, OnImage onImage) {
        this.me = me;
        this.onImage = onImage;
    }

    public void submit(List<Message> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Message m = items.get(position);
        boolean mine = me.equals(m.getSenderId());

        String thisDay = day.format(new Date(m.getCreatedAt()));
        boolean newDay = position == 0 || !thisDay.equals(day.format(new Date(items.get(position - 1).getCreatedAt())));
        h.day.setVisibility(newDay ? View.VISIBLE : View.GONE);
        h.day.setText(thisDay);

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) h.bubble.getLayoutParams();
        lp.gravity = mine ? Gravity.END : Gravity.START;
        h.bubble.setLayoutParams(lp);
        h.bubble.setBackgroundResource(mine ? R.drawable.bg_bubble_mine : R.drawable.bg_bubble_theirs);
        int text = ContextCompat.getColor(h.itemView.getContext(), mine ? R.color.mb_on_ink : R.color.mb_ink);
        h.text.setTextColor(text);
        h.time.setTextColor(text);
        h.time.setAlpha(0.7f);

        boolean hasText = !Ui.blank(m.getText());
        h.text.setVisibility(hasText ? View.VISIBLE : View.GONE);
        h.text.setText(m.getText());
        h.time.setText(time.format(new Date(m.getCreatedAt())));

        boolean hasImage = !Ui.blank(m.getImageUrl());
        h.image.setVisibility(hasImage ? View.VISIBLE : View.GONE);
        if (hasImage) {
            int radius = Math.round(Ui.dp(h.itemView.getContext(), 14));
            Glide.with(h.image).load(m.getImageUrl()).transform(new CenterCrop(), new RoundedCorners(radius)).into(h.image);
            h.image.setOnClickListener(v -> onImage.open(m.getImageUrl()));
        } else {
            Glide.with(h.image).clear(h.image);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView day, text, time;
        final View bubble;
        final ImageView image;

        Holder(View v) {
            super(v);
            day = v.findViewById(R.id.msg_day);
            bubble = v.findViewById(R.id.msg_bubble);
            image = v.findViewById(R.id.msg_image);
            text = v.findViewById(R.id.msg_text);
            time = v.findViewById(R.id.msg_time);
        }
    }
}
