# Review 1 — Project Overview

## Problem statement and proposed solution

Patients need a clear way to request consultations and keep personal records, while professionals need to review assigned visits and record consultation summaries. MediConnect is a Java website that demonstrates these workflows with role-based access and a MySQL schema design.

## Requirements

Functional requirements include role-aware login, patient registration, appointment booking/cancellation, professional consultation notes, personal health records, admin analytics, currency preferences, and MySQL connectivity setup. Quality requirements include input validation, hashed passwords, prepared persistence design, clear dark UI, and maintainable separation of models, service logic, and UI.

## Roles and permissions

| Role | Current demo permissions |
|---|---|
| Patient | Register, book/cancel/reschedule own appointments, view/update own health record, message assigned professionals |
| Professional | Manage weekly availability, view assigned visits, save consultation notes, view authorized records, message assigned patients |
| Admin | Create/deactivate accounts, manage appointment status, update system settings, view users and analytics |

## Architecture and JDBC

Spring MVC routes render Thymeleaf pages and call the `MediConnectService` abstraction. `PlatformService` provides locked in-memory demo storage. `JdbcPlatformService` uses prepared JDBC statements and database transactions when `MEDICONNECT_MODE=mysql` is explicitly selected. MySQL mode fails during startup if the database settings or schema are unavailable.

## GUI screens

Browser home/login/registration, role-specific dashboard, analytics, booking/rescheduling, consultation, health-record, weekly availability, messaging, admin user/settings tools, profile, and currency forms.

## Stack and OOP

Java 17, Spring Boot, Spring MVC, Thymeleaf, Maven, JDBC, MySQL Connector/J, BCrypt, and JUnit 5. `User` inherits identity and contact fields from `Person`. The `MediConnectService` interface is implemented by both `PlatformService` and `JdbcPlatformService`, demonstrating interface-based polymorphism. Typed collections store demo data, enums represent roles and appointment states, and controllers delegate validation and actions to services. Spring MVC routes requests through the servlet web stack and renders Thymeleaf views.

## Collections, generics, concurrency

The demo service uses generic maps and typed lists. A `ReentrantReadWriteLock` protects concurrent access and serializes booking checks/updates in demo memory. MySQL booking uses a transaction and locks provider/patient rows before checking conflicts and inserting. HTTP operations are synchronous; there are no external slow service calls in the current site.

## Testing plan and status

JUnit tests cover authentication, validation, booking dates/conflicts, cancellation authorization, consultation authorization, record access, availability, messaging, profiles, settings, and rescheduling. Eight service tests plus two template-render tests passed (10 total, zero failures). No MySQL server is configured here, so database operations have not been exercised against a live server.
