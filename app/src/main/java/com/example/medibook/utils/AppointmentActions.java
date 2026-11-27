package com.example.medibook.utils;

import android.content.Context;
import androidx.annotation.Nullable;
import com.example.medibook.R;
import com.example.medibook.models.Appointment;
import com.example.medibook.models.Chat;
import com.example.medibook.repositories.ChatRepository;
import com.example.medibook.repositories.AppointmentRepository;
import com.example.medibook.repositories.NotificationRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Locale;

/** Changes an appointment's status and alerts the other party. */
public final class AppointmentActions {
    public interface Done {
        /** error is null on success. */
        void onDone(@Nullable String error);
    }

    private AppointmentActions() {}

    public static void setStatus(Context context, Appointment appointment, String status, Done done) {
        Context app = context.getApplicationContext();
        new AppointmentRepository().updateAppointmentStatus(appointment.getAppointmentId(), status,
                new AppointmentRepository.AppointmentCallback() {
                    @Override
                    public void onSuccess(Appointment updated) {
                        Appointment a = updated != null ? updated : appointment;
                        notifyOtherParty(app, a, status);
                        if ("confirmed".equals(status)) openChat(a);
                        done.onDone(null);
                    }

                    @Override
                    public void onFailure(String error) {
                        done.onDone(error);
                    }
                });
    }

    /** Confirming opens the patient–doctor conversation, so it shows in both inboxes right away. */
    private static void openChat(Appointment a) {
        new ChatRepository().open(a, new ChatRepository.ChatCallback() {
            @Override public void onSuccess(Chat chat) {}
            @Override public void onFailure(String error) {} // either side can still open it from the booking
        });
    }

    private static void notifyOtherParty(Context c, Appointment a, String status) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        String day = Ui.dayShort(a.getDate());
        String time = a.getTime();
        NotificationRepository repo = new NotificationRepository();
        NotificationRepository.NotificationCallback ignore = new NotificationRepository.NotificationCallback() {
            @Override public void onSuccess() {}
            @Override public void onFailure(String error) {}
        };
        boolean patientActed = me != null && me.getUid().equals(a.getPatientId());
        if (patientActed) {
            if (Ui.blank(a.getDoctorId())) return;
            String who = Ui.blank(a.getPatientName()) ? c.getString(R.string.label_patient) : a.getPatientName();
            repo.sendNotification(a.getDoctorId(), c.getString(R.string.notif_cancel_title),
                    c.getString(R.string.notif_cancel_body, who, day, time), "appointment_cancelled", ignore);
        } else {
            if (Ui.blank(a.getPatientId())) return;
            String label = Ui.statusLabel(c, status).toLowerCase(Locale.US);
            repo.sendNotification(a.getPatientId(), c.getString(R.string.notif_status_title, label),
                    c.getString(R.string.notif_status_body, Ui.doctorName(a.getDoctorName()), day, time, label),
                    "appointment_" + status, ignore);
        }
    }
}
