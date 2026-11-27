package com.example.medibook.utils;

import android.content.Context;
import android.view.View;
import com.example.medibook.R;
import com.example.medibook.models.Appointment;
import com.example.medibook.repositories.AppointmentRepository;
import com.example.medibook.repositories.AuthRepository;
import com.example.medibook.repositories.NotificationRepository;
import com.example.medibook.repositories.UserRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin removal of a patient or doctor: cancels their upcoming appointments (telling the other
 * person), then deletes their profile. Past appointments and chats stay for the other side's records.
 */
public final class AccountRemoval {
    private AccountRemoval() {}

    public static void confirm(View anchor, String uid, String name, boolean doctor, Runnable onDone) {
        Context c = anchor.getContext();
        if (uid == null || uid.equals(FirebaseAuth.getInstance().getUid())) {
            Ui.snack(anchor, R.string.cant_delete_self);
            return;
        }
        new MaterialAlertDialogBuilder(c)
                .setTitle(c.getString(R.string.delete_account_title, name))
                .setMessage(doctor ? R.string.delete_doctor_body : R.string.delete_user_body)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) -> remove(anchor, uid, name, doctor, onDone))
                .show();
    }

    private static void remove(View anchor, String uid, String name, boolean doctor, Runnable onDone) {
        Context c = anchor.getContext().getApplicationContext();
        AppointmentRepository.AppointmentsCallback loaded = new AppointmentRepository.AppointmentsCallback() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                List<Appointment> upcoming = new ArrayList<>();
                for (Appointment a : appointments) {
                    if (Ui.isUpcoming(a)) upcoming.add(a);
                }
                cancelNext(c, anchor, upcoming, 0, doctor, () -> deleteData(anchor, uid, name, doctor, onDone));
            }

            @Override
            public void onFailure(String error) {
                // Stop here: deleting without cancelling would leave the other side waiting on a ghost booking.
                Ui.snack(anchor, c.getString(R.string.error_generic, error));
            }
        };
        AppointmentRepository repo = new AppointmentRepository();
        if (doctor) repo.getDoctorAppointments(uid, loaded);
        else repo.getPatientAppointments(uid, loaded);
    }

    /** Cancels one at a time and stops at the first failure, so nothing is deleted half-way. Each cancellation alerts the other party. */
    private static void cancelNext(Context c, View anchor, List<Appointment> list, int i, boolean doctorRemoved, Runnable then) {
        if (i >= list.size()) {
            then.run();
            return;
        }
        Appointment a = list.get(i);
        new AppointmentRepository().updateAppointmentStatus(a.getAppointmentId(), "cancelled",
                new AppointmentRepository.AppointmentCallback() {
                    @Override
                    public void onSuccess(Appointment updated) {
                        String to = doctorRemoved ? a.getPatientId() : a.getDoctorId();
                        String who = doctorRemoved ? Ui.doctorName(a.getDoctorName()) : Ui.orDash(a.getPatientName());
                        if (!Ui.blank(to)) {
                            new NotificationRepository().sendNotification(to, c.getString(R.string.notif_cancel_title),
                                    c.getString(R.string.notif_account_removed_body, who, Ui.dayShort(a.getDate()), a.getTime()),
                                    "appointment_cancelled", new NotificationRepository.NotificationCallback() {
                                        @Override public void onSuccess() {}
                                        @Override public void onFailure(String error) {}
                                    });
                        }
                        cancelNext(c, anchor, list, i + 1, doctorRemoved, then);
                    }

                    @Override
                    public void onFailure(String error) {
                        Ui.snack(anchor, c.getString(R.string.error_generic, error));
                    }
                });
    }

    private static void deleteData(View anchor, String uid, String name, boolean doctor, Runnable onDone) {
        Context c = anchor.getContext();
        new UserRepository().deleteAccountData(uid, doctor, new AuthRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                Ui.snack(anchor, c.getString(R.string.account_deleted, name));
                onDone.run();
            }

            @Override
            public void onFailure(String error) {
                Ui.snack(anchor, c.getString(R.string.error_generic, error));
            }
        });
    }
}
