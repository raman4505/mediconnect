# Java Web Project Rubric Mapping

| Rubric area | MediConnect evidence | Current verification |
|---|---|---|
| Problem understanding and solution design | Patient booking and records, professional consultations, admin workflows, role-based architecture | Described in `project_overview.md`; web templates render in tests |
| Core Java and OOP | `User extends Person`; `MediConnectService` has `PlatformService` and `JdbcPlatformService` implementations; typed collections; enums; validation and authorization exceptions | 10 JUnit tests pass; inheritance and service polymorphism are visible in source |
| Collections, generics, and synchronization | Typed `Map`, `List`, and `Set` collections; `ReentrantReadWriteLock` protects in-memory operations | Business logic tests pass |
| Database classes and JDBC | `JdbcPlatformService`, `Database`, prepared statements, schema, and transactional booking logic | Compiled and covered through service-level checks; live MySQL has not been configured here |
| Servlet and web integration | Spring Boot embedded servlet stack; Spring MVC `@Controller` routes; `HttpServletRequest` and `HttpSession`; Thymeleaf pages | Home page and role dashboards pass template-render tests; a live browser smoke test remains unverified in this environment |
| Code structure and setup | Maven project, service interface, role models, README, SQL setup, launcher, and JUnit tests | `mvn test package` succeeds |

The presentation in `deliverables/MediConnect-Project-Review-Rubric.pptx` follows these areas and includes architecture, user-flow, and data-model diagrams. It does not include screenshots of a live browser session.
