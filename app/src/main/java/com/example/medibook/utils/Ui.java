package com.example.medibook.utils;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.medibook.R;
import com.example.medibook.models.Appointment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.snackbar.Snackbar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Small view helpers shared by every screen. */
public final class Ui {
    private static final SimpleDateFormat ISO = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat TIME = new SimpleDateFormat("hh:mm a", Locale.US);

    /** Doctors store their working days by these full English names. */
    public static final String[] WEEK = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    private Ui() {}

    /** True when a doctor's stored days include this weekday ("Mon" and "Monday" both match). */
    public static boolean worksOn(@Nullable List<String> days, String weekday) {
        if (days == null) return false;
        String prefix = weekday.substring(0, 3).toLowerCase(Locale.US);
        for (String d : days) {
            if (d != null && d.trim().toLowerCase(Locale.US).startsWith(prefix)) return true;
        }
        return false;
    }

    // ---- Status -------------------------------------------------------------------------------

    public static boolean isActiveStatus(String status) {
        return "pending".equalsIgnoreCase(status) || "confirmed".equalsIgnoreCase(status);
    }

    /** Styles a TextView as the status pill used on every board and pass. */
    public static void status(TextView pill, String status) {
        Context c = pill.getContext();
        String s = status == null ? "pending" : status.toLowerCase(Locale.US);
        int text, fill, stroke, label;
        switch (s) {
            case "confirmed":
                text = R.color.mb_ok; fill = R.color.mb_ok_bg; stroke = R.color.mb_ok_bg; label = R.string.status_confirmed; break;
            case "completed":
                text = R.color.mb_ink; fill = R.color.mb_panel_alt; stroke = R.color.mb_line; label = R.string.status_completed; break;
            case "cancelled":
                text = R.color.mb_mute; fill = R.color.mb_panel; stroke = R.color.mb_line; label = R.string.status_cancelled; break;
            default:
                text = R.color.mb_hot; fill = R.color.mb_panel; stroke = R.color.mb_hot; label = R.string.status_pending;
        }
        pill(pill, c.getString(label), text, fill, stroke);
        int flags = pill.getPaintFlags();
        pill.setPaintFlags("cancelled".equals(s) ? flags | Paint.STRIKE_THRU_TEXT_FLAG : flags & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }

    /** A small tag: role, verification, or a status. Colours are resource ids. */
    public static void pill(TextView pill, String label, int textColor, int fillColor, int strokeColor) {
        Context c = pill.getContext();
        pill.setText(label);
        pill.setTextColor(ContextCompat.getColor(c, textColor));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(c, 6));
        bg.setColor(ContextCompat.getColor(c, fillColor));
        bg.setStroke(Math.round(dp(c, 1.5f)), ContextCompat.getColor(c, strokeColor));
        pill.setBackground(bg);
    }

    /** The one-line explanation shown on a pass under its status. */
    public static int statusLine(String status) {
        switch (status == null ? "" : status.toLowerCase(Locale.US)) {
            case "confirmed": return R.string.status_line_confirmed;
            case "completed": return R.string.status_line_completed;
            case "cancelled": return R.string.status_line_cancelled;
            default: return R.string.status_line_pending;
        }
    }

    public static String statusLabel(Context c, String status) {
        switch (status == null ? "" : status.toLowerCase(Locale.US)) {
            case "confirmed": return c.getString(R.string.status_confirmed);
            case "completed": return c.getString(R.string.status_completed);
            case "cancelled": return c.getString(R.string.status_cancelled);
            default: return c.getString(R.string.status_pending);
        }
    }

    // ---- Dates --------------------------------------------------------------------------------

    @Nullable
    public static Date parseDay(String iso) {
        if (iso == null) return null;
        try {
            return ISO.parse(iso);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String isoDay(Date date) {
        return ISO.format(date);
    }

    public static String today() {
        return ISO.format(new Date());
    }

    /** "Thu 4 Dec" */
    public static String dayShort(String iso) {
        Date d = parseDay(iso);
        return d == null ? (iso == null ? "" : iso) : new SimpleDateFormat("EEE d MMM", Locale.US).format(d);
    }

    /** "Thursday, 4 December 2026" */
    public static String dayLong(String iso) {
        Date d = parseDay(iso);
        return d == null ? (iso == null ? "" : iso) : new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US).format(d);
    }

    /** Sort key for an appointment's date and slot; unknown values sort last. */
    public static long when(Appointment a) {
        Date d = parseDay(a.getDate());
        if (d == null) return Long.MAX_VALUE;
        long t = d.getTime();
        try {
            Date time = TIME.parse(a.getTime() == null ? "" : a.getTime().toUpperCase(Locale.US));
            Calendar cal = Calendar.getInstance();
            cal.setTime(time);
            t += (cal.get(Calendar.HOUR_OF_DAY) * 60L + cal.get(Calendar.MINUTE)) * 60_000L;
        } catch (ParseException ignored) {
            // No valid slot: keep the start of day.
        }
        return t;
    }

    /** Upcoming = still active and not on a past day. */
    public static boolean isUpcoming(Appointment a) {
        return isActiveStatus(a.getStatus()) && a.getDate() != null && a.getDate().compareTo(today()) >= 0;
    }

    public static String relativeTime(Context c, long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < 60_000) return c.getString(R.string.just_now);
        if (diff < 3_600_000) return (diff / 60_000) + "m";
        if (diff < 86_400_000) return (diff / 3_600_000) + "h";
        if (diff < 7 * 86_400_000L) return (diff / 86_400_000) + "d";
        return new SimpleDateFormat("d MMM", Locale.US).format(new Date(timestamp));
    }

    // ---- Text ---------------------------------------------------------------------------------

    public static boolean blank(@Nullable String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String orDash(@Nullable String s) {
        return blank(s) ? "—" : s.trim();
    }

    /** "Dr. Ayesha Malik" whether or not the stored name already has the prefix. */
    public static String doctorName(@Nullable String name) {
        if (blank(name)) return "Doctor";
        String n = name.trim();
        return n.toLowerCase(Locale.US).startsWith("dr") ? n : "Dr. " + n;
    }

    public static String firstName(@Nullable String name) {
        return blank(name) ? "" : name.trim().split("\\s+")[0];
    }

    public static String initials(@Nullable String name) {
        if (blank(name)) return "?";
        StringBuilder sb = new StringBuilder();
        for (String part : name.trim().replaceFirst("(?i)^dr\\.?\\s*", "").split("\\s+")) {
            if (!part.isEmpty() && sb.length() < 2) sb.append(Character.toUpperCase(part.charAt(0)));
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }

    /** Short booking reference shown on passes, derived from the appointment id. */
    public static String ref(@Nullable String id) {
        if (blank(id)) return "—";
        return id.replace("-", "").substring(0, Math.min(4, id.replace("-", "").length())).toUpperCase(Locale.US);
    }

    // ---- Views --------------------------------------------------------------------------------

    /** Shows a profile photo when there is one, otherwise the initials. */
    public static void avatar(ImageView image, TextView initials, @Nullable String url, @Nullable String name) {
        initials.setText(initials(name));
        if (blank(url)) {
            image.setVisibility(View.GONE);
            initials.setVisibility(View.VISIBLE);
        } else {
            image.setVisibility(View.VISIBLE);
            initials.setVisibility(View.INVISIBLE);
            Glide.with(image).load(url).circleCrop().into(image);
        }
    }

    /** A snackbar that sits above the floating nav when there is one. */
    public static void snack(View anchor, String message) {
        Snackbar bar = Snackbar.make(anchor, message, Snackbar.LENGTH_LONG);
        View nav = anchor.getRootView().findViewById(R.id.nav_card);
        if (nav != null) bar.setAnchorView(nav);
        bar.show();
    }

    public static void snack(View anchor, int messageRes) {
        snack(anchor, anchor.getContext().getString(messageRes));
    }

    /** Appends a "label · value" row to a board container. */
    public static void detailRow(LinearLayout board, String label, @Nullable String value) {
        View row = LayoutInflater.from(board.getContext()).inflate(R.layout.item_detail_row, board, false);
        ((TextView) row.findViewById(R.id.detail_label)).setText(label);
        ((TextView) row.findViewById(R.id.detail_value)).setText(blank(value) ? board.getContext().getString(R.string.not_set) : value);
        addRow(board, row);
    }

    /** Appends a tappable row with an icon, a title, an optional body and an optional count. */
    public static View linkRow(LinearLayout board, @DrawableRes int icon, String title, @Nullable String body,
                               @Nullable String value, Runnable onClick) {
        View row = LayoutInflater.from(board.getContext()).inflate(R.layout.item_link_row, board, false);
        ((ImageView) row.findViewById(R.id.link_icon)).setImageResource(icon);
        ((TextView) row.findViewById(R.id.link_title)).setText(title);
        TextView b = row.findViewById(R.id.link_body);
        b.setText(body);
        b.setVisibility(blank(body) ? View.GONE : View.VISIBLE);
        TextView v = row.findViewById(R.id.link_value);
        v.setText(value);
        v.setVisibility(blank(value) ? View.GONE : View.VISIBLE);
        row.setOnClickListener(x -> onClick.run());
        addRow(board, row);
        return row;
    }

    private static void addRow(LinearLayout board, View row) {
        if (board.getChildCount() > 0) {
            View line = new View(board.getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    Math.max(1, Math.round(dp(board.getContext(), 1))));
            lp.setMarginStart(Math.round(dp(board.getContext(), 16)));
            line.setLayoutParams(lp);
            line.setBackgroundColor(ContextCompat.getColor(board.getContext(), R.color.mb_line));
            board.addView(line);
        }
        board.addView(row);
    }

    /** A list drawn as a board: stacked rows separated by hairlines, scrolled by its parent. */
    public static void board(RecyclerView list, RecyclerView.Adapter<?> adapter) {
        Context c = list.getContext();
        list.setLayoutManager(new LinearLayoutManager(c));
        list.setNestedScrollingEnabled(false);
        MaterialDividerItemDecoration divider = new MaterialDividerItemDecoration(c, LinearLayoutManager.VERTICAL);
        divider.setDividerColor(ContextCompat.getColor(c, R.color.mb_line));
        divider.setLastItemDecorated(false);
        list.addItemDecoration(divider);
        list.setAdapter(adapter);
    }

    /** The one entrance motion: rise and fade in. Follows the system animation scale, so "remove animations" skips it. */
    public static void rise(View v) {
        v.setAlpha(0f);
        v.setTranslationY(dp(v.getContext(), 24));
        v.animate().alpha(1f).translationY(0f).setDuration(520)
                .setInterpolator(new android.view.animation.DecelerateInterpolator(2.2f)).start();
    }

    public static float dp(Context c, float value) {
        return value * c.getResources().getDisplayMetrics().density;
    }

    // ---- Action sheet -------------------------------------------------------------------------

    /** A bottom sheet with a title, an optional subtitle, optional content and a list of actions. */
    public static final class Sheet {
        private final BottomSheetDialog dialog;
        private final LinearLayout actions;
        private final LinearLayout content;

        public Sheet(Context c, String title, @Nullable String subtitle) {
            dialog = new BottomSheetDialog(c);
            View root = LayoutInflater.from(c).inflate(R.layout.sheet_actions, null);
            ((TextView) root.findViewById(R.id.sheet_title)).setText(title);
            TextView sub = root.findViewById(R.id.sheet_subtitle);
            sub.setVisibility(blank(subtitle) ? View.GONE : View.VISIBLE);
            sub.setText(subtitle);
            actions = root.findViewById(R.id.sheet_actions);
            content = root.findViewById(R.id.sheet_content);
            dialog.setContentView(root);
        }

        public Sheet content(View view) {
            content.addView(view);
            content.setVisibility(View.VISIBLE);
            return this;
        }

        public Sheet action(@DrawableRes int icon, String label, boolean danger, Runnable onClick) {
            View row = LayoutInflater.from(actions.getContext()).inflate(R.layout.item_sheet_action, actions, false);
            ImageView iv = row.findViewById(R.id.action_icon);
            TextView tv = row.findViewById(R.id.action_label);
            iv.setImageResource(icon);
            tv.setText(label);
            if (danger) {
                int red = ContextCompat.getColor(row.getContext(), R.color.mb_error);
                tv.setTextColor(red);
                iv.setImageTintList(android.content.res.ColorStateList.valueOf(red));
            }
            row.setOnClickListener(v -> {
                dialog.dismiss();
                onClick.run();
            });
            actions.addView(row);
            return this;
        }

        public void show() {
            dialog.show();
        }
    }
}
