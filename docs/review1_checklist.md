# Review 1 Checklist — Website

| Review item | Status | Evidence |
|---|---|---|
| Maven/Java project structure | Implemented | `pom.xml`, packages |
| Spring Boot servlet-based web application | Implemented; local server smoke test not verified in this host | `Main`, `WebController`, `jakarta.servlet`, Thymeleaf |
| MySQL schema and relationship design | Implemented | `database/schema.sql`, ER diagram |
| JDBC-backed site persistence | Implemented; live MySQL run not verified | `JdbcPlatformService`, explicit `MEDICONNECT_MODE=mysql` |
| Responsive black/green UI | Implemented | `static/style.css` |
| Patient registration and role-aware sessions | Implemented in code | `WebController` |
| Appointment booking, cancellation, and rescheduling | Implemented in demo/JDBC services and web forms | `MediConnectService`, dashboard template |
| Professional consultation notes | Implemented in demo service and web form | `PlatformService`, dashboard template |
| Patient health records and professional access checks | Implemented in demo service and web forms | `PlatformService`, dashboard template |
| Admin user access, appointment status, analytics, and settings | Implemented | Dashboard and admin routes |
| Professional availability and internal messages | Implemented in demo and JDBC services | Dashboard and messaging routes |
| Generic collections and synchronization | Implemented | `PlatformService` |
| JUnit tests | 10 passed (8 service, 2 template-render) | `src/test/java`, Surefire report |
| Architecture and database visuals | Included | Updated rubric-aligned presentation |
| Actual browser screenshots | Not captured | Add if the instructor specifically requires them |
| PowerPoint deck | Created and rubric-aligned | `deliverables/MediConnect-Project-Review-Rubric.pptx` |
| GitHub repository | Not published yet | GitHub sign-in is required before publishing |

## Suggested slides

1. MediConnect title and project objective.
2. Problem statement and proposed solution.
3. Roles and permissions.
4. Spring MVC servlet web architecture.
5. Patient and professional workflows.
6. MySQL data model.
7. Inheritance, interfaces, polymorphism, generics, exceptions, and synchronization.
8. Security and data access controls.
9. Test status and verification limits.
10. Run instructions and next steps.
