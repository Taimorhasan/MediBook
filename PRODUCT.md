# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

A university final-year project, graded and demoed. It has three roles, each signed in with an email/password or Google account:

- **Patient:** finds a verified doctor by specialty or name, picks a date and time slot, and tracks the request from pending to confirmed, completed or cancelled.
- **Doctor:** signs up, waits for an admin to verify them, then reviews incoming appointment requests (confirm, complete, cancel) and keeps their profile and availability current.
- **Admin:** verifies doctors, manages users and their roles, hospitals and permissions, and sees every appointment.

Users are in Pakistan. The app is in English only.

## Product Purpose

Lets patients book doctor appointments without phoning the clinic, and gives doctors and admins one place to run the appointment workflow. It succeeds when a patient can go from opening the app to a confirmed appointment request in a few taps, and doctors and admins can act on requests without hunting for them.

## Operating Context

- Each role has a fixed bottom navigation bar inside one host screen. Switching tabs changes only the content area.
- Appointment status vocabulary: pending, confirmed, completed, cancelled.
- In-app alerts are stored in Firestore and shown in an Alerts list. Device push notifications are not delivered yet.
- Doctors set the weekdays they are available. Patients book a date and one of the doctor's time slots.

## Capabilities and Constraints

- Android, Java, Android Views (XML layouts), Material Components. Firebase Auth, Cloud Firestore, Firebase Cloud Messaging. Profile images are stored on Cloudinary.
- Firestore collections: users, roles, doctors, hospitals, appointments, notifications, schedules.
- Role-based access is enforced by Firestore security rules (`firestore.rules`).
- Requires both a light and a dark theme.
- Every value shown must come from the signed-in user's real data. No placeholder or hardcoded figures, such as fake vitals.
- Prices are in PKR. Phone numbers use the Pakistani format.

## Brand Commitments

- Name: MediBook.

## Evidence on Hand

- Live Firestore project `medi-book-62e9d` holds test accounts: 7 patients, 6 doctors (5 with profiles, all verified), 3 admins, 2 hospitals, and appointments.
- There are no real doctor photos, logos, testimonials or ratings. Do not invent ratings or reviews. Show a rating only when the data has one.

## Product Principles

1. Booking comes first: the shortest path from opening the app to a booked appointment wins over everything else.
2. Show only what is true for this user: their real name, real appointments and real statuses, with honest empty states.
3. One consistent shell per role: the navigation never moves, and only the content changes.
4. Status is always legible: pending, confirmed, completed and cancelled read differently at a glance, in both themes.

## Accessibility & Inclusion

Material 3 baselines: touch targets of at least 48dp, text in sp that follows the system font size, sufficient contrast in light and dark themes.
