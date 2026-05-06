package com.example.medibook.models;

import java.util.List;

public class Doctor {
    private String doctorId;
    private String name;
    private String specialty;
    private String hospitalId;
    private String phone;
    private String email;
    private String experience;
    private String rating;
    private int imageResId;
    private List<String> availableDays;
    private String bio;

    public Doctor() {} // Firestore constructor

    public Doctor(String doctorId, String name, String specialty, String hospitalId,
                  String phone, String email, String experience, String rating, String bio) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialty = specialty;
        this.hospitalId = hospitalId;
        this.phone = phone;
        this.email = email;
        this.experience = experience;
        this.rating = rating;
        this.bio = bio;
    }

    // Legacy constructor for existing code
    public Doctor(String name, String specialty, String rating, int imageResId) {
        this.name = name;
        this.specialty = specialty;
        this.rating = rating;
        this.imageResId = imageResId;
    }

    // Getters and setters
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public List<String> getAvailableDays() { return availableDays; }
    public void setAvailableDays(List<String> availableDays) { this.availableDays = availableDays; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
