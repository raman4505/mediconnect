# Database ER Diagram

```mermaid
erDiagram
 USERS ||--o| PROFESSIONAL_PROFILES : has
 USERS ||--o| HEALTH_RECORDS : owns
 USERS ||--o{ APPOINTMENTS : patient
 USERS ||--o{ APPOINTMENTS : professional
 APPOINTMENTS ||--o| CONSULTATIONS : records
 USERS ||--o{ MESSAGES : sends
 USERS ||--o{ MESSAGES : receives
 USERS ||--o{ PROFESSIONAL_AVAILABILITY : publishes
 USERS { bigint id PK string email UK string role }
 APPOINTMENTS { bigint id PK bigint patient_id FK bigint professional_id FK datetime starts_at string status }
 HEALTH_RECORDS { bigint patient_id PK_FK text allergies text conditions_text }
 CONSULTATIONS { bigint appointment_id PK_FK text notes text advice }
 MESSAGES { bigint id PK bigint sender_id FK bigint recipient_id FK text body }
```
