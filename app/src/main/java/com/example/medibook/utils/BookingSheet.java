package com.example.medibook.utils;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import com.example.medibook.R;
import com.example.medibook.activities.common.ChatActivity;
import com.example.medibook.activities.user.DoctorProfileActivity;
import com.example.medibook.adapters.AppointmentAdapter.Viewer;
import com.example.medibook.models.Appointment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** The detail sheet for one appointment, with the actions this viewer may take. */
public final class BookingSheet {
    private BookingSheet() {}

    /** @param onChanged runs after a status change succeeds, so the caller can reload. */
    public static void show(View anchor, Appointment a, Viewer viewer, Runnable onChanged) {
        Context c = anchor.getContext();
        View pass = LayoutInflater.from(c).inflate(R.layout.view_pass, null, false);
        boolean doctorView = viewer != Viewer.PATIENT;
        PassView.bind(pass, a, doctorView, false, null,
                doctorView ? Ui.doctorName(a.getDoctorName()) : a.getDoctorSpecialty());

        String subtitle = Ui.blank(a.getNotes()) ? null : c.getString(R.string.reason) + ": " + a.getNotes();
        Ui.Sheet sheet = new Ui.Sheet(c, Ui.dayLong(a.getDate()), subtitle).content(pass);
        String status = a.getStatus() == null ? "pending" : a.getStatus();
        boolean upcoming = Ui.isUpcoming(a);
        // Chat opens once the doctor has confirmed, and stays open after the visit.
        boolean canChat = viewer != Viewer.ADMIN && ("confirmed".equals(status) || "completed".equals(status));
        if (canChat) {
            sheet.action(R.drawable.ic_chat_circle_text,
                    c.getString(viewer == Viewer.PATIENT ? R.string.message_doctor : R.string.message_patient), false,
                    () -> ChatActivity.open(c, a));
        }

        switch (viewer) {
            case PATIENT:
                if (upcoming) {
                    sheet.action(R.drawable.ic_calendar_x, c.getString(R.string.cancel_appointment), true,
                            () -> confirmCancel(anchor, a, onChanged));
                }
                sheet.action(R.drawable.ic_stethoscope, c.getString(upcoming ? R.string.view_doctor : R.string.book_again), false,
                        () -> c.startActivity(new Intent(c, DoctorProfileActivity.class)
                                .putExtra(DoctorProfileActivity.EXTRA_DOCTOR_ID, a.getDoctorId())));
                break;
            case DOCTOR:
                if ("pending".equals(status)) {
                    sheet.action(R.drawable.ic_check_circle, c.getString(R.string.confirm), false,
                            () -> change(anchor, a, "confirmed", onChanged));
                    sheet.action(R.drawable.ic_x_circle, c.getString(R.string.decline), true,
                            () -> change(anchor, a, "cancelled", onChanged));
                } else if ("confirmed".equals(status)) {
                    sheet.action(R.drawable.ic_check_circle, c.getString(R.string.mark_completed), false,
                            () -> change(anchor, a, "completed", onChanged));
                    sheet.action(R.drawable.ic_calendar_x, c.getString(R.string.cancel_appointment), true,
                            () -> change(anchor, a, "cancelled", onChanged));
                }
                break;
            case ADMIN:
                for (String s : new String[]{"pending", "confirmed", "completed", "cancelled"}) {
                    if (s.equals(status)) continue;
                    sheet.action(icon(s), c.getString(R.string.change_status) + ": " + Ui.statusLabel(c, s),
                            "cancelled".equals(s), () -> change(anchor, a, s, onChanged));
                }
                break;
        }
        sheet.show();
    }

    private static int icon(String status) {
        switch (status) {
            case "confirmed": return R.drawable.ic_check_circle;
            case "completed": return R.drawable.ic_check;
            case "cancelled": return R.drawable.ic_x_circle;
            default: return R.drawable.ic_hourglass_medium;
        }
    }

    private static void confirmCancel(View anchor, Appointment a, Runnable onChanged) {
        Context c = anchor.getContext();
        new MaterialAlertDialogBuilder(c)
                .setTitle(R.string.cancel_confirm_title)
                .setMessage(c.getString(R.string.cancel_confirm_body, Ui.doctorName(a.getDoctorName()),
                        Ui.dayShort(a.getDate()), a.getTime()))
                .setNegativeButton(R.string.keep_it, null)
                .setPositiveButton(R.string.cancel_appointment, (d, w) -> change(anchor, a, "cancelled", onChanged))
                .show();
    }

    public static void change(View anchor, Appointment a, String status, Runnable onChanged) {
        Context c = anchor.getContext();
        AppointmentActions.setStatus(c, a, status, error -> {
            if (error == null) {
                Ui.snack(anchor, c.getString(R.string.appointment_updated, Ui.statusLabel(c, status).toLowerCase(java.util.Locale.US)));
                onChanged.run();
            } else {
                Ui.snack(anchor, c.getString(R.string.error_generic, error));
            }
        });
    }
}
