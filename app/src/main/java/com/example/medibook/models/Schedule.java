package com.example.medibook.models;

import java.util.List;

public class Schedule {
    private String scheduleId;
    private String doctorId;
    private String date; // YYYY-MM-DD
    private List<Slot> slots;
    private String createdBy; // managerId
    private long createdAt;

    public static class Slot {
        private String time;
        private boolean isBooked;
        private String bookedBy; // userId

        public Slot() {}

        public Slot(String time, boolean isBooked, String bookedBy) {
            this.time = time;
            this.isBooked = isBooked;
            this.bookedBy = bookedBy;
        }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public boolean isBooked() { return isBooked; }
        public void setBooked(boolean booked) { isBooked = booked; }
        public String getBookedBy() { return bookedBy; }
        public void setBookedBy(String bookedBy) { this.bookedBy = bookedBy; }
    }

    public Schedule() {}

    public Schedule(String scheduleId, String doctorId, String date, List<Slot> slots, String createdBy) {
        this.scheduleId = scheduleId;
        this.doctorId = doctorId;
        this.date = date;
        this.slots = slots;
        this.createdBy = createdBy;
        this.createdAt = System.currentTimeMillis();
    }

    public String getScheduleId() { return scheduleId; }
    public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public List<Slot> getSlots() { return slots; }
    public void setSlots(List<Slot> slots) { this.slots = slots; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}