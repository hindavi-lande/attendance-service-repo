# Attendance Service

Spring Boot 3 / Java 17 microservice for tracking employee attendance, independent of the
Employee Management System. Schema changes are managed by [Flyway](https://flywaydb.org/).

## Stack

- Spring Boot 3.3 (Web, Data JPA, Validation)
- PostgreSQL
- Flyway (schema migrations)
- Lombok
- Testcontainers (integration test spins up a real Postgres and applies migrations)

## Running locally

1. Start Postgres (runs on port 5434 so it doesn't collide with the Employee Management
   System's own database on 5432):
   bash
   docker compose up -d
   
2. Run the app (Flyway migrates the schema automatically on startup):
   bash
   ./mvnw spring-boot:run
   
   Override connection details with env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

The service listens on port `8081`.

## API

| Method | Path                              | Description                              |
|--------|------------------------------------|-------------------------------------------|
| POST   | `/api/v1/attendance/clock-in`      | Clock in for today (`{ "employeeId": 1 }`) |
| POST   | `/api/v1/attendance/clock-out`     | Clock out for today (`{ "employeeId": 1 }`) |
| GET    | `/api/v1/attendance/{employeeId}?month=YYYY-MM` | Monthly attendance history |

This is the foundational slice of the service — every other attendance capability
(leave requests, manager visibility, holiday calendar, reporting, EMS sync) builds on
the `attendance_entries` table and `AttendanceService` introduced here.

## Tests

bash
./mvnw test


`AttendanceServiceImplTest` covers clock-in/clock-out business rules with mocks.
`AttendanceServiceApplicationTests` uses Testcontainers to verify the Flyway migrations
apply cleanly against a real Postgres instance and the Spring context starts.
