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

    public void updateSlotStatus(String scheduleId, String slotTime, boolean isBooked, String userId, AuthRepository.VoidCallback callback) {
        com.google.firebase.firestore.DocumentReference scheduleRef = db.collection("schedules").document(scheduleId);
        
        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(scheduleRef);
            Schedule schedule = snapshot.toObject(Schedule.class);
            
            if (schedule != null) {
                boolean slotFound = false;
                for (Schedule.Slot slot : schedule.getSlots()) {
                    if (slot.getTime().equals(slotTime)) {
                        // Professional check: don't book if already booked by someone else
                        if (isBooked && slot.isBooked()) {
                            throw new com.google.firebase.firestore.FirebaseFirestoreException(
                                "Slot already booked", 
                                com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED
                            );
                        }
                        slot.setBooked(isBooked);
                        slot.setBookedBy(isBooked ? userId : "");
                        slotFound = true;
                        break;
                    }
                }
                if (slotFound) {
                    transaction.set(scheduleRef, schedule);
                }
            }
            return null;
        }).addOnSuccessListener(result -> {
            if (callback != null) callback.onSuccess();
        }).addOnFailureListener(e -> {
            if (callback != null) callback.onFailure(e.getMessage());
        });
    }
}
