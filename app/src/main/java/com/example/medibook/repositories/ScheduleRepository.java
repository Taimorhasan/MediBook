package com.example.medibook.repositories;

import com.example.medibook.models.Schedule;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

public class ScheduleRepository {
    private FirebaseFirestore db;

    public ScheduleRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface ScheduleCallback {
        void onSuccess(Schedule schedule);
        void onFailure(String error);
    }

    public interface SchedulesCallback {
        void onSuccess(List<Schedule> schedules);
        void onFailure(String error);
    }

    public void createSchedule(Schedule schedule, ScheduleCallback callback) {
        db.collection("schedules").document(schedule.getScheduleId())
                .set(schedule)
                .addOnSuccessListener(aVoid -> callback.onSuccess(schedule))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getDoctorSchedules(String doctorId, SchedulesCallback callback) {
        db.collection("schedules")
                .whereEqualTo("doctorId", doctorId)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Schedule> schedules = queryDocumentSnapshots.toObjects(Schedule.class);
                    callback.onSuccess(schedules);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getScheduleByDate(String doctorId, String date, ScheduleCallback callback) {
        db.collection("schedules")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("date", date)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        callback.onSuccess(queryDocumentSnapshots.getDocuments().get(0).toObject(Schedule.class));
                    } else {
                        callback.onFailure("No schedule found for this date");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateSlotStatus(String scheduleId, String slotTime, boolean isBooked, String userId) {
        db.collection("schedules").document(scheduleId).get().addOnSuccessListener(documentSnapshot -> {
            Schedule schedule = documentSnapshot.toObject(Schedule.class);
            if (schedule != null) {
                for (Schedule.Slot slot : schedule.getSlots()) {
                    if (slot.getTime().equals(slotTime)) {
                        slot.setBooked(isBooked);
                        slot.setBookedBy(userId);
                        break;
                    }
                }
                db.collection("schedules").document(scheduleId).set(schedule);
            }
        });
    }
}
