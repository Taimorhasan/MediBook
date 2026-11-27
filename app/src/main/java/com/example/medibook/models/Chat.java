package com.example.medibook.models;

/**
 * One conversation per patient and doctor: chats/{patientId}_{doctorId}.
 * appointmentId points at a confirmed appointment, which the security rules check.
 */
public class Chat {
    private String chatId;
    private String patientId;
    private String doctorId;
    private String patientName;
    private String doctorName;
    private String appointmentId;
    private String lastMessage;
    private long lastAt;
    private String lastSenderId;
    private long unreadPatient;
    private long unreadDoctor;

    public Chat() {} // Firestore

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public long getLastAt() { return lastAt; }
    public void setLastAt(long lastAt) { this.lastAt = lastAt; }
    public String getLastSenderId() { return lastSenderId; }
    public void setLastSenderId(String lastSenderId) { this.lastSenderId = lastSenderId; }
    public long getUnreadPatient() { return unreadPatient; }
    public void setUnreadPatient(long unreadPatient) { this.unreadPatient = unreadPatient; }
    public long getUnreadDoctor() { return unreadDoctor; }
    public void setUnreadDoctor(long unreadDoctor) { this.unreadDoctor = unreadDoctor; }
}
