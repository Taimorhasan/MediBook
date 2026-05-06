package com.example.medibook.models;

public class Hospital {
    private String hospitalId;
    private String name;
    private String address;
    private String phone;
    private String email;
    private double latitude;
    private double longitude;

    public Hospital() {} // Firestore constructor

    public Hospital(String hospitalId, String name, String address, String phone,
                    String email, double latitude, double longitude) {
        this.hospitalId = hospitalId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}