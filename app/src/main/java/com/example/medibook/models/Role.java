package com.example.medibook.models;

import java.util.ArrayList;
import java.util.List;

public class Role {
    private String roleId;
    private String roleName;
    private List<String> permissions;

    public Role() {
        this.permissions = new ArrayList<>();
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