package com.example.medibook.models;

import java.util.List;

public class User {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private List<String> roleIds;
    private long createdAt;

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
}