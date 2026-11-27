<div align="center">

# MediBook

**Book a verified doctor in a few taps. Track the request. Chat once it's confirmed.**

An Android appointment booking app for patients, doctors and admins, built on Firebase.

![Android](https://img.shields.io/badge/Android-24%2B-111418?logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-111418?logo=openjdk&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%C2%B7%20Firestore%20%C2%B7%20FCM-111418?logo=firebase&logoColor=white)
![Material 3](https://img.shields.io/badge/Material-3-111418?logo=materialdesign&logoColor=white)

</div>

---

## What it does

MediBook has three roles. Each signs in to its own shell with a fixed, floating navigation bar, where switching tabs only swaps the content.

| Role | Tabs | What they can do |
|---|---|---|
| **Patient** | Home · Doctors · Bookings · Inbox · Profile | Search verified doctors by name, specialty or hospital, book a day and time slot, track each request, cancel, and chat with the doctor once it's confirmed |
| **Doctor** | Today · Schedule · Inbox · Profile | Confirm or decline requests, mark visits completed, set working days, fee and bio, and chat with patients |
| **Admin** | Overview · Doctors · Bookings · Users · More | Verify and deactivate doctors, assign hospitals, change any booking's status, change user roles, add users, and manage hospitals, roles and permissions |

**Highlights**

- **Appointments as passes.** The next appointment is drawn as a pass: doctor, a notched tear, then date, time and booking ref, and its status.
- **Changes you haven't seen stay highlighted.** When a status changes, the row keeps an orange highlight until you look.
- **Chat after confirmation.** Patient and doctor can exchange text and photos in real time once an appointment is confirmed. Firestore rules enforce this, not just the UI.
- **Light and dark themes.** You can follow the system or pick one in Profile.
- **Real data only.** Every name, count and status comes from Firestore. There are no placeholder figures.

## Design

The visual direction is called **Appointment Pass**. It uses a light grey ground with white panels, near-black ink and hairline dividers. One orange colour is reserved for things that changed: new statuses, unread alerts and pending requests.

Text is set in [Manrope](https://fonts.google.com/specimen/Manrope). Times, dates and references are set in [JetBrains Mono](https://www.jetbrains.com/lp/mono/) so figures line up. Icons are from [Phosphor](https://phosphoricons.com).

## Tech stack

| Area | Choice |
|---|---|
| App | Android, Java 17, Android Views (XML), Material Components 3, minSdk 24, targetSdk 34 |
| Auth | Firebase Authentication (email/password and Google) |
| Data | Cloud Firestore, protected by [`firestore.rules`](firestore.rules) |
| Images | Cloudinary (unsigned upload) and Glide |
| Notifications | In-app alerts stored in Firestore, plus an FCM receiver |

## Project structure

```
app/src/main/java/com/example/medibook/
├── MainActivity.java        # signed-in shell for every role: floating nav + tab fragments
├── fragments/               # one per tab (Home, Doctors, Bookings, Inbox, Profile, Today, admin lists)
├── activities/              # full screens opened over the shell (sign-in, booking, doctor profile, chat, editors)
├── adapters/                # RecyclerView adapters for boards, doctors, alerts and chat bubbles
├── repositories/            # every Firestore / Auth call lives here
├── models/                  # Firestore documents: User, Doctor, Appointment, Chat, Message, ...
├── services/                # Cloudinary upload, FCM receiver
└── utils/                   # RoleRouter, PassView, BookingSheet, StateView, Ui helpers
```

Screens never talk to Firebase directly; they go through `repositories/`. `RoleRouter` decides where a signed-in user lands. `AppointmentActions` changes a status and alerts the other party in one place.

## Data model (Firestore)

| Collection | Holds |
|---|---|
| `users` | Profile, `role` and `roleIds` for every account |
| `roles` | Role name, which shell it opens, and its permissions |
| `doctors` | Public doctor profile, `isVerified`, `isActive`, working days, fee, hospital |
| `hospitals` | Hospitals that doctors can be assigned to |
| `appointments` | Patient, doctor, date, slot, status (`pending`, `confirmed`, `completed`, `cancelled`) |
| `notifications` | In-app alerts per user |
| `chats/{patientId}_{doctorId}/messages` | Conversations, opened by a confirmed appointment |

## Setup

You need Android Studio (or JDK 17 with the Android SDK), and access to a Firebase project and a Cloudinary account.

### 1. Firebase

1. Create a Firebase project, or ask the owner to add you to the existing one.
2. Add an Android app with package name `com.example.medibook`, and add your debug SHA-1. Get it with `./gradlew signingReport`. Google sign-in needs this.
3. Download **`google-services.json`** and put it in `app/`. It is git-ignored on purpose, so every developer uses their own copy.
4. Enable **Authentication** → Email/Password and Google.
5. Create a **Firestore** database, then deploy the rules and indexes:

   ```bash
   firebase deploy --only firestore:rules,firestore:indexes --project <your-project-id>
   ```

6. Create the `roles` documents `patient`, `doctor` and `admin`. Then give your own account admin access by setting `role: "admin"` and `roleIds: ["admin"]` on your `users` document in the Firebase console.

### 2. Cloudinary (photos)

Create an **unsigned upload preset** in Cloudinary. Then add these lines to `local.properties` in the project root. This file is git-ignored.

```properties
cloudinary.cloudName=<your-cloud-name>
cloudinary.uploadPreset=<your-unsigned-preset>
```

The build stops with a clear message if either value is missing.

### 3. Run

```bash
./gradlew assembleDebug        # build
./gradlew installDebug         # install on a connected phone or emulator
```

## Security notes

- **Firestore rules decide who can do what.** Self sign-up can only create a patient or doctor account, and users can't change their own role. Only the two people in a chat can read it. Doctors can't verify themselves.
- **Secrets stay out of git.** `google-services.json`, `local.properties`, keystores and IDE settings are all ignored.
- **Lock the API key down.** Firebase's Android API key identifies the app; it doesn't grant access. Still, restrict it to the Android app and its SHA-1 fingerprints in the Google Cloud console.

## Known limitations

- **Push notifications don't reach other devices yet.** Alerts appear in the Inbox, but sending pushes needs a server or a Cloud Function, which requires Firebase's Blaze plan.
- **Chat sends text and photos only.** Documents such as PDFs aren't supported yet.
- **Time slots are a fixed daily list.** The app doesn't prevent two patients from requesting the same slot; the doctor confirms one.

## License

This is a university final-year project. Fonts (SIL Open Font License) and icons (Phosphor, MIT) keep their own licenses.
