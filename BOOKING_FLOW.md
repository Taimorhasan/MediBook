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
- `app/src/main/java/com/example/medibook/activities/user/UserHomeActivity.java`
- `app/src/main/java/com/example/medibook/activities/user/DoctorListActivity.java`
