# MediBook – Smart Appointment Booking System

## 📌 Overview

MediBook is a mobile-based appointment booking system that allows patients to book doctor appointments while enabling administrators and managers to manage schedules and system operations efficiently.

---

## 🧠 System Architecture

### Frontend

* Android (XML + Kotlin)

### Backend

* Firebase Services:

    * Firebase Authentication
    * Cloud Firestore
    * Firebase Cloud Messaging (FCM)

---

## 🔐 Authentication Flow

### Signup

1. User enters:

    * Name
    * Email
    * Phone
    * Password
2. Firebase Authentication creates account
3. User data stored in Firestore (`users` collection)
4. Default role assigned: `patient`

---

### Login

1. User logs in using email & password
2. Fetch user document from Firestore
3. Identify role using `roleIds`
4. Redirect to appropriate dashboard

---

### Logout

* FirebaseAuth.signOut()
* Redirect to Login Screen

---

## 🧩 Role-Based Access Control (RBAC)

### Roles:

* Admin
* Manager
* Patient

### Role Structure:

```
roles:
  roleId:
    name: string
    permissions: string[]
```

### Example Permissions:

* view_doctors
* book_appointment
* manage_schedules
* update_appointments

---

## 🗂️ Firestore Database Schema

### users

* userId
* name
* email
* phone
* roleIds (array)
* createdAt

---

### doctors

* doctorId
* name
* specialty
* hospitalId
* experience
* isActive

---

### hospitals

* hospitalId
* name
* address
* phone

---

### schedules

* scheduleId
* doctorId
* date
* slots:

    * time
    * isBooked
    * bookedBy
* createdBy

---

### appointments

* appointmentId
* patientId
* doctorId
* scheduleId
* date
* slotTime
* status
* createdBy
* assignedBy

---

### notifications

* notificationId
* userId
* title
* message
* read

---

## 🔄 System Workflow

### Appointment Booking (Patient)

1. Select Doctor
2. View Schedule
3. Select Time Slot
4. Create Appointment
5. Status = Pending/Confirmed

---

### Appointment Management (Manager)

* View all appointments
* Assign appointments
* Update status:

    * confirmed
    * completed
    * cancelled

---

## 📱 Screens

### Common Screens:

* Splash Screen
* Login Screen
* Signup Screen

### Patient:

* Dashboard
* Doctor List
* Schedule View
* My Appointments

### Manager:

* Dashboard
* Manage Schedules
* View Appointments

### Admin:

* Dashboard
* Manage Users
* Manage Roles

---

## 🔔 Notifications (FCM)

### Triggers:

* Appointment booked
* Appointment confirmed
* Appointment cancelled

---

## ⚙️ Firebase Setup Guide

### Step 1: Create Project

* Go to Firebase Console
* Create new project

---

### Step 2: Add Android App

* Register app with package name
* Download `google-services.json`
* Place inside `/app` folder

---

### Step 3: Enable Services

#### Authentication:

* Enable Email/Password

#### Firestore:

* Create database (test mode)

#### FCM:

* Enable Cloud Messaging

---

### Step 4: Add Dependencies

In `build.gradle`:

```
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-firestore'
implementation 'com.google.firebase:firebase-messaging'
```

---

## 🧱 App Architecture

### Structure:

* Activities (UI)
* Repository Layer (Firebase operations)
* Model Classes

---

## 📌 Best Practices

* Use RecyclerView for lists
* Avoid heavy logic in Activities
* Validate inputs
* Use proper naming conventions
* Keep Firestore reads optimized

---

## 🚀 Future Enhancements

* Doctor login panel
* Online payments
* Video consultation
* Prescription upload
* Ratings & reviews

---

## 📊 Conclusion

MediBook provides a scalable and efficient solution for appointment booking using Firebase backend and Android frontend. The RBAC-based system ensures flexibility and security, making it suitable for real-world healthcare applications.
