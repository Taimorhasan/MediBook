package com.example.medibook.models;

import java.util.List;

public class Role {
    private String roleId;
    private String name;
    private List<String> permissions;

    public Role() {} // Firestore constructor

    public Role(String roleId, String name, List<String> permissions) {
        this.roleId = roleId;
        this.name = name;
        this.permissions = permissions;
    }

    // Getters and setters
    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}