package com.example.medibook.utils;

import com.example.medibook.models.Role;
import com.example.medibook.repositories.RoleRepository;
import java.util.Arrays;

public class BackendInitializer {
    
    public static void initializeRoles() {
        RoleRepository roleRepo = new RoleRepository();
        
        // Admin Role
        Role admin = new Role("admin", "Administrator", Arrays.asList(
            "manage_users", "manage_roles", "manage_doctors", "manage_hospitals", 
            "view_all_appointments", "manage_all_appointments", "view_reports"
        ));
        
        // Manager Role
        Role manager = new Role("manager", "Manager", Arrays.asList(
            "manage_doctors", "manage_schedules", "manage_appointments", 
            "view_doctor_appointments", "send_notifications"
        ));

        // Doctor Role
        Role doctor = new Role("doctor", "Doctor", Arrays.asList(
            "view_doctor_appointments", "update_appointments", "manage_schedules"
        ));
        
        // Patient Role
        Role patient = new Role("patient", "Patient", Arrays.asList(
            "view_doctors", "book_appointment", "view_own_appointments", "cancel_own_appointment"
        ));
        
        roleRepo.addRole(admin);
        roleRepo.addRole(manager);
        roleRepo.addRole(doctor);
        roleRepo.addRole(patient);
    }
}
