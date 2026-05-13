package com.example.medibook.models;

import java.util.ArrayList;
import java.util.List;

public class Permission {
    public static final String MANAGE_USERS = "manage_users";
    public static final String MANAGE_DOCTORS = "manage_doctors";
    public static final String MANAGE_HOSPITALS = "manage_hospitals";
    public static final String MANAGE_ROLES = "manage_roles";
    public static final String VIEW_REPORTS = "view_reports";
    public static final String VIEW_DASHBOARD = "view_dashboard";
    public static final String ACT_AS_USER = "act_as_user";

    public static List<String> getAllPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(MANAGE_USERS);
        permissions.add(MANAGE_DOCTORS);
        permissions.add(MANAGE_HOSPITALS);
        permissions.add(MANAGE_ROLES);
        permissions.add(VIEW_REPORTS);
        permissions.add(VIEW_DASHBOARD);
        permissions.add(ACT_AS_USER);
        return permissions;
    }

    public static String getPermissionLabel(String permission) {
        switch (permission) {
            case MANAGE_USERS: return "Manage Users";
            case MANAGE_DOCTORS: return "Manage Doctors";
            case MANAGE_HOSPITALS: return "Manage Hospitals";
            case MANAGE_ROLES: return "Manage Roles & Permissions";
            case VIEW_REPORTS: return "View Reports";
            case VIEW_DASHBOARD: return "View Dashboard";
            case ACT_AS_USER: return "Act As User (Impersonation)";
            default: return permission;
        }
    }
}
