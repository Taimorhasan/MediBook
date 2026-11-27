package com.example.medibook.models;

import java.util.Arrays;
import java.util.List;

/**
 * Permission keys stored on roles. Firestore rules check manage_doctors, manage_hospitals,
 * manage_roles, manage_appointments, manage_schedules and view_reports; the rest describe the role.
 */
public final class Permission {
    private static final String[][] ALL = {
            {"manage_users", "Manage users"},
            {"manage_roles", "Manage roles and permissions"},
            {"manage_doctors", "Verify and manage doctors"},
            {"manage_hospitals", "Manage hospitals"},
            {"manage_appointments", "Change any appointment's status"},
            {"manage_all_appointments", "Manage all appointments"},
            {"view_all_appointments", "See all appointments"},
            {"view_reports", "See reports and all bookings"},
            {"manage_schedules", "Manage schedules"},
            {"view_doctor_appointments", "See their patients' appointments"},
            {"update_appointments", "Update their appointments"},
            {"send_notifications", "Send notifications"},
            {"view_doctors", "Browse doctors"},
            {"book_appointment", "Book appointments"},
            {"view_own_appointments", "See own appointments"},
            {"cancel_own_appointment", "Cancel own appointments"},
    };

    private Permission() {}

    public static List<String[]> getAll() {
        return Arrays.asList(ALL);
    }
}
