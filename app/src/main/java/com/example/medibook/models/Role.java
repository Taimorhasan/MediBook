package com.example.medibook.models;

import java.util.ArrayList;
import java.util.List;

public class Role {
    private String roleId;
    private String roleName;
    private String dashboardType; // admin, doctor, patient
    private List<String> permissions;

    public Role() {
        this.permissions = new ArrayList<>();
    }

    public Role(String roleId, String roleName, String dashboardType, List<String> permissions) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.dashboardType = dashboardType;
        this.permissions = permissions;
    }

    public String getDashboardType() {
        return dashboardType;
    }

    public void setDashboardType(String dashboardType) {
        this.dashboardType = dashboardType;
    }

    public Role(String roleId, String roleName, List<String> permissions) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.permissions = permissions;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}