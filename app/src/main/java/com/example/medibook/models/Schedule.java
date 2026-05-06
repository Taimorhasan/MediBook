package com.example.medibook.models;

import java.util.List;

public class Schedule {
    private String scheduleId;
    private String doctorId;
    private String dayOfWeek; // Monday, Tuesday, etc.
    private String startTime; // HH:mm
    private String endTime; // HH:mm
    private List<String> availableSlots; // List of time slots like "09:00", "09:30"
    private boolean isActive;

    public Schedule() {} // Firestore constructor

    public Schedule(String scheduleId, String doctorId, String dayOfWeek,
                    String startTime, String endTime, List<String> availableSlots, boolean isActive) {
        this.scheduleId = scheduleId;
        this.doctorId = doctorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.availableSlots = availableSlots;
        this.isActive = isActive;
    }

    // Getters and setters
    public String getScheduleId() { return scheduleId; }
    public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public List<String> getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(List<String> availableSlots) { this.availableSlots = availableSlots; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}