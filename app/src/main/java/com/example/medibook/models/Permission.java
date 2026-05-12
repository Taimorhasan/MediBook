package com.example.medibook.models;

import java.util.ArrayList;
import java.util.List;

public class Permission {
    // Admin Module Permissions
    public static final String MANAGE_DOCTORS = "manage_doctors";
    public static final String MANAGE_HOSPITALS = "manage_hospitals";
    public static final String MANAGE_USERS = "manage_users";
    public static final String MANAGE_ROLES = "manage_roles";
    public static final String VIEW_REPORTS = "view_reports";
    
    // Doctor Module Permissions
    public static final String VIEW_APPOINTMENTS = "view_appointments";
    public static final String MANAGE_SCHEDULE = "manage_schedule";
    public static final String UPDATE_PROFILE = "update_profile";

    // Patient Module Permissions
    public static final String BOOK_APPOINTMENT = "book_appointment";
    public static final String VIEW_MY_HISTORY = "view_my_history";

    public static List<String> getAllPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(MANAGE_DOCTORS);
        permissions.add(MANAGE_HOSPITALS);
        permissions.add(MANAGE_USERS);
        permissions.add(MANAGE_ROLES);
        permissions.add(VIEW_REPORTS);
        permissions.add(VIEW_APPOINTMENTS);
        permissions.add(MANAGE_SCHEDULE);
        permissions.add(UPDATE_PROFILE);
        permissions.add(BOOK_APPOINTMENT);
        permissions.add(VIEW_MY_HISTORY);
        return permissions;
    }
}
