# MediBook Booking Flow Notes

## What Was Empty

The patient doctor-list screen loaded doctors through `DoctorRepository.getAllDoctors()`.
That query only returns doctors where:

- `isActive == true`
- `isVerified == true`

Doctor signup creates doctors as active, but unverified. Until an admin verifies doctors, patients see an empty booking list.

## What Was Updated

- Patient `BOOKINGS` navigation now opens `DoctorListActivity`, the doctor selection screen.
- Patient home specialty chips now open `DoctorListActivity` with the selected specialty.
- `DoctorRepository` now returns a fallback catalog of bookable demo doctors when Firestore has no active verified doctors or the doctor query fails.
- `DoctorRepository.getDoctor()` can resolve those fallback doctor IDs, so `BookAppointmentActivity` can still confirm appointment requests.
- `DoctorListActivity` reads an optional `specialty` intent extra and pre-selects that filter.
- Dashboard search now opens the doctor listing with the typed query applied.
- Dashboard search now shows inline suggestions for doctors and specialties while typing.
- Home specialty categories now navigate directly to doctor listing and support common naming differences like `Cardiology` vs `Cardiologist`.
- Appointment booking now creates the appointment even when Firestore denies the duplicate-slot pre-check query.
- Doctor profile bio text is aligned from the top/start so longer bios read properly.
- Alerts screen now loads notifications through `NotificationRepository`, shows local fallback alerts when no server alerts exist, marks alerts as read, and routes appointment/booking alerts to the right screens.

## Access Denied Booking Fix

`AppointmentRepository.bookAppointment()` tried to query existing appointments by doctor/date/slot before creating a new appointment. Firestore rules only allow patients to read their own appointments, so that duplicate-slot query can fail with permission denied.

The repository now logs that pre-check failure and then attempts the appointment create directly. The create still uses Firestore rules and requires:

- signed-in patient
- `patientId == request.auth.uid`
- string `doctorId`
- string `date`
- string `slotTime`
- status `pending` or `confirmed`

## Real Production Path

For real doctors from Firebase:

1. Doctor signs up.
2. Doctor document is saved in `doctors`.
3. Admin opens Manage Doctors.
4. Admin verifies the doctor.
5. Patient doctor listing shows that doctor.
6. Patient taps `Book`.
7. `BookAppointmentActivity` creates a pending appointment in `appointments`.

## Files Changed

- `app/src/main/java/com/example/medibook/repositories/DoctorRepository.java`
- `app/src/main/java/com/example/medibook/repositories/AppointmentRepository.java`
- `app/src/main/java/com/example/medibook/activities/user/UserHomeActivity.java`
- `app/src/main/java/com/example/medibook/activities/user/DoctorListActivity.java`
- `app/src/main/res/layout/activity_user_home.xml`
- `app/src/main/res/layout/activity_doctor_profile.xml`
- `app/src/main/java/com/example/medibook/activities/user/AlertsActivity.java`
- `app/src/main/java/com/example/medibook/adapters/AlertsAdapter.java`
- `app/src/main/java/com/example/medibook/repositories/NotificationRepository.java`
