package com.example.medibook.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.example.medibook.R;
import com.example.medibook.models.Appointment;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.EdgeTreatment;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.ShapePath;

/** Binds view_pass.xml and cuts the pass's side notches in line with its tear. */
public final class PassView {
    private PassView() {}

    /**
     * @param forDoctorView true when the viewer is the doctor, so the pass names the patient
     * @param changed       true when the status changed since the viewer last looked
     */
    public static void bind(View root, Appointment a, boolean forDoctorView, boolean changed,
                            String photoUrl, String subtitle) {
        Context c = root.getContext();
        String name = forDoctorView ? Ui.orDash(a.getPatientName()) : Ui.doctorName(a.getDoctorName());
        ((TextView) root.findViewById(R.id.pass_name)).setText(name);
        TextView sub = root.findViewById(R.id.pass_sub);
        sub.setText(subtitle);
        sub.setVisibility(Ui.blank(subtitle) ? View.GONE : View.VISIBLE);
        View avatar = root.findViewById(R.id.pass_avatar);
        Ui.avatar((ImageView) avatar.findViewById(R.id.avatar_image),
                (TextView) avatar.findViewById(R.id.avatar_initials), photoUrl, name);

        ((TextView) root.findViewById(R.id.pass_date)).setText(Ui.dayShort(a.getDate()));
        ((TextView) root.findViewById(R.id.pass_time)).setText(Ui.orDash(a.getTime()).replace(" ", " "));
        ((TextView) root.findViewById(R.id.pass_ref)).setText(Ui.ref(a.getAppointmentId()));

        TextView line = root.findViewById(R.id.pass_status_line);
        line.setText(Ui.statusLine(a.getStatus()));
        line.setTextColor(ContextCompat.getColor(c, changed ? R.color.mb_hot : R.color.mb_ink));
        root.findViewById(R.id.pass_strip).setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(c, changed ? R.color.mb_hot_bg : R.color.mb_panel_alt)));
        Ui.status(root.findViewById(R.id.pass_status), a.getStatus());

        cutNotches((MaterialCardView) root, root.findViewById(R.id.pass_head)); // an <include> may rename the card's id
    }

    /** Cuts a pass's side notches level with the bottom of {@code head}, and keeps them there on relayout. */
    public static void cutNotches(MaterialCardView card, View head) {
        if (card.getTag(R.id.pass_head) == null) {
            card.setTag(R.id.pass_head, Boolean.TRUE);
            card.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> notch(card, head));
        }
        notch(card, head);
    }

    private static void notch(MaterialCardView card, View head) {
        if (head.getHeight() == 0) return;
        float radius = Ui.dp(card.getContext(), 11);
        float tear = head.getBottom() + Ui.dp(card.getContext(), 1.5f);
        float corner = card.getRadius(); // edges start where the rounded corners end
        Notch right = new Notch(tear - corner, radius);                        // right edge runs top to bottom
        Notch left = new Notch(card.getHeight() - tear - corner, radius);      // left edge runs bottom to top
        ShapeAppearanceModel current = card.getShapeAppearanceModel();
        if (right.equals(current.getRightEdge()) && left.equals(current.getLeftEdge())) return;
        card.setShapeAppearanceModel(current.toBuilder().setRightEdge(right).setLeftEdge(left).build());
    }

    /** A half-circle cut into an edge at a fixed distance from where the edge starts. */
    private static final class Notch extends EdgeTreatment {
        private final float position;
        private final float radius;

        Notch(float position, float radius) {
            this.position = position;
            this.radius = radius;
        }

        @Override
        public void getEdgePath(float length, float center, float interpolation, @NonNull ShapePath path) {
            float r = radius * interpolation;
            path.lineTo(position - r, 0);
            path.addArc(position - r, -r, position + r, r, 180, -180);
            path.lineTo(length, 0);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Notch && ((Notch) o).position == position && ((Notch) o).radius == radius;
        }

        @Override
        public int hashCode() {
            return Float.hashCode(position) * 31 + Float.hashCode(radius);
        }
    }
}
