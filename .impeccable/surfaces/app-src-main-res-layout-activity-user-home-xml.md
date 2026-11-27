---
version: 1
slug: "app-src-main-res-layout-activity-user-home-xml"
primary_target: "app/src/main/res/layout/activity_user_home.xml"
related_targets: ["app/src/main/res/layout/activity_doctor_dashboard.xml","app/src/main/res/layout/activity_admin_dashboard.xml"]
---

## Scope

The whole MediBook Android app (patient, doctor and admin shells). Visitor mode: operate. Build path: code-led. The approved composition is `.impeccable/mocks/decision/challenger-pass.html`.

## Direction contract

THESIS: Each appointment is a pass and each list of appointments is a board ordered by time. This refuses the category default of a teal hero card over an icon grid of specialties.

OWN-WORLD: Light theme: #F1F3F5 ground, #FFFFFF panels, #111418 ink, #5F6873 secondary text, #DDE1E6 1dp hairlines. Dark theme: #0D0F12 ground, #171A1F panels, #ECEFF3 ink. One change colour, orange (#E4470F light / #FF7A45 dark), used only for a status that changed since the user last looked and for unread alerts. Statuses: pending is outlined orange, confirmed green, completed neutral ink, cancelled struck-through mute. Type: Manrope for UI. JetBrains Mono for times, dates, booking refs and counts. Uppercase tracked captions. Elevation is tonal panels plus hairlines, never drop shadows.

STORY: The patient sees their next appointment as a pass, understands its status at a glance, and books another in two taps. The doctor sees today's requests as a board and confirms or completes them in place. The admin sees counts and queues as boards and acts on the rows.

FIRST VIEWPORT: Patient Home: greeting with the user's real name, then the next-appointment pass (doctor segment, a dashed tear with notches, Date, Time and Ref segment, a status strip), then a "Your bookings" board of up to three rows ordered by date, then a black "Book an appointment" button. A fixed Material bottom navigation bar (Home, Doctors, Bookings, Alerts, Profile) sits in a single host activity, and only the content area swaps.

FORM: Challenger "Appointment Pass" (catalog vernacular-ephemera-boarding-pass-and-gate-board, competitive verdict), chosen by the user over assigned candidate 3 (Wayfinding). seed:f4ed66df6bfd97bf. Signature move: a status that changed holds its orange highlight on its row until the user opens it, and rows never reorder when a status changes.

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance

## Unresolved

- Admin and doctor shells: final tab sets, to be confirmed with the user.
- Native verification needs an emulator or device for screenshots in both themes.
