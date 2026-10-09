# MediConnect — Java Health Consultation Website

MediConnect is a browser-based healthcare consultation demo built with Java 17, Spring Boot, Spring MVC, and Thymeleaf. It runs at `http://localhost:8080` and presents patient, professional, and administrator workflows.

## Features

- Browser sign-in and patient registration; public registration cannot create admin or professional accounts.
- BCrypt password hashes, per-session login, role checks, and CSRF tokens on form submissions.
- Patient appointment booking and cancellation with future-date and double-booking validation.
- Professional availability management, appointment list, consultation notes/advice, and record access limited to patients with assigned visits.
- Patient-owned health record editing, appointment rescheduling, and internal messaging with assigned professionals.
- Admin analytics, user creation and access management, appointment status management, system settings, and display-currency preference.
- Responsive dark green website and sample account data.

## Current data mode and limitations

The default `demo` mode uses `PlatformService`, an in-memory repository. Its data resets whenever the server restarts and is shared among users connected to the same server. Set `MEDICONNECT_MODE=mysql` to use `JdbcPlatformService`; that mode requires the MySQL schema and credentials and fails at startup if they are missing. Demo and MySQL mode are never switched automatically. MySQL integration has compile/test coverage but has not been connected to a live MySQL server in this workspace. Use fictional data only; this is not a medical service.

## Requirements and run

- JDK 17 or newer
- Maven 3.6.3 or newer

In PowerShell, from the project folder:

```powershell
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). Stop the server with `Ctrl+C`. If Maven is unavailable, install Maven and make sure `mvn -version` reports JDK 17+.

On Windows, you can also double-click `run-website.bat`. It uses the packaged JAR when present, builds it if needed, starts the server, and opens the site in your browser. Set `JAVA_HOME` to a JDK 17+ installation if `java` on your PATH points to an older Java version.

## Sample accounts

| Role | Email | Password |
|---|---|---|
| Admin | admin@demo.local | Admin123! |
| Healthcare professional | doctor@demo.local | Doctor123! |
| Patient | patient@demo.local | Patient123! |

These accounts are for local demonstration only. Patient registration is available from the home page. Sample users and changes are recreated when the server restarts.

## MySQL mode setup

1. Install MySQL 8+ and run `mysql -u root -p < database/schema.sql`.
2. Create a least-privilege database user and grant it access to `mediconnect`.
3. In PowerShell, configure the environment for the current terminal session:

   ```powershell
   $env:MEDICONNECT_MODE = "mysql"
   $env:MEDICONNECT_DB_URL = "jdbc:mysql://localhost:3306/mediconnect?useSSL=false&serverTimezone=UTC"
   $env:MEDICONNECT_DB_USER = "mediconnect"
   $env:MEDICONNECT_DB_PASSWORD = "your-local-password"
   $env:MEDICONNECT_INITIAL_ADMIN_NAME = "Project Admin"
   $env:MEDICONNECT_INITIAL_ADMIN_EMAIL = "admin@example.local"
   $env:MEDICONNECT_INITIAL_ADMIN_PASSWORD = "choose-a-long-local-password"
   mvn spring-boot:run
   ```

   On first startup, the app creates an initial administrator only when the database has no admin account. After it is created, remove the three `MEDICONNECT_INITIAL_ADMIN_*` variables from your environment. The administrator can create professional and patient accounts from the dashboard.
4. Keep `.env` and real credentials out of Git. The app never falls back to demo mode after a MySQL failure.

## Tests

Run `mvn test`. Eight service tests cover authentication, validation, booking dates/conflicts, cancellation permissions, consultation authorization, record access, availability, messaging, admin restrictions, profiles, settings, and rescheduling. Two additional template-render tests cover the home page and dashboards for all roles (10 tests total). MySQL integration requires a configured MySQL server and has not been exercised against a live server here.

## Project layout

- `src/main/java/com/mediconnect/Main.java` — Spring Boot entry point.
- `src/main/java/com/mediconnect/web` — browser routes, session role checks, CSRF validation.
- `src/main/java/com/mediconnect/service` and `model` — demo business logic and data types.
- `src/main/resources/templates` — Thymeleaf pages; `static/style.css` — responsive theme.
- `database/schema.sql` — MySQL schema; `docs/` — architecture, diagrams, rubric mapping, Review 1 checklist.

## Review 1 and GitHub

Use the overview, database design, Mermaid diagrams, rubric mapping, and checklist under `docs/`. The presentation is in `deliverables/`. It contains architecture, workflow, and database diagrams; capture actual browser screenshots too if your instructor specifically requires them. `MEDICONNECT-Submission.zip` packages the source, documentation, presentation, and runnable JAR.

To publish the source on GitHub, create an empty repository (for example, `mediconnect`) in your account. Choose public visibility if reviewers should access it without an invitation. Then run these commands from the project folder, replacing the URL with the one GitHub gives you:

```bash
git init
git add .
git commit -m "Build MediConnect Java web app"
git branch -M main
git remote add origin https://github.com/<your-user>/mediconnect.git
git push -u origin main
```

The `.gitignore` excludes build output, temporary deck-authoring files, and local environment files. Never commit secrets or real medical data.
