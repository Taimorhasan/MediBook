package com.example.medibook.models;

public class Doctor {
    private String name;
    private String specialty;
    private String rating;
    private int imageResId;

    public Doctor(String name, String specialty, String rating, int imageResId) {
        this.name = name;
        this.specialty = specialty;
        this.rating = rating;
        this.imageResId = imageResId;
    }

    public String getName() { return name; }
    public String getSpecialty() { return specialty; }
    public String getRating() { return rating; }
    public int getImageResId() { return imageResId; }
}
