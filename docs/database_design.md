# Database Design

`database/schema.sql` creates the `mediconnect` database and eight tables: `users`, `professional_profiles`, `appointments`, `health_records`, `consultations`, `messages`, `professional_availability`, and `app_settings`.

- `users` is the identity/role table. Email is unique; only password hashes belong here.
- `professional_profiles` and `health_records` are one-to-one extensions of users.
- `appointments` references patient and professional user IDs and is indexed by each party and start time.
- `consultations` is one-to-one with an appointment so notes persist separately from booking status.
- `messages` stores sender/recipient and read timestamps.
- `professional_availability` stores weekday time windows.
- `app_settings` stores basic key/value preferences.

The MySQL booking service uses a transaction, locks the professional and patient user rows, checks overlapping intervals, then inserts the appointment. That serializes concurrent bookings for either party across app instances. The in-memory demo service synchronizes booking checks within one process only. JDBC workflows still need verification against a configured MySQL server.
