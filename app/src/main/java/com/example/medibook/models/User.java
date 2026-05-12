package com.example.medibook.models;

import java.util.List;

public class User {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private List<String> roleIds;
    private long createdAt;
    
    // Medical fields
    private String age;
    private String gender;
    private String bloodGroup;
    private String bio;
    private String profileImage;
    private String fcmToken;
    private String role;

    public User() {} // Firestore constructor

    public User(String userId, String name, String email, String phone, List<String> roleIds) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.roleIds = roleIds;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<String> getRoleIds() { return roleIds; }
    public void setRoleIds(List<String> roleIds) { this.roleIds = roleIds; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Medical fields getters and setters
    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
