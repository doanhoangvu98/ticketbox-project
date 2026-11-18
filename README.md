# TicketBox

An event ticketing platform: create events, generate a seat map from the venue layout,
search with paging, and report revenue.
A Java learning project (Java Core → Spring Boot), aiming at Junior+ → Middle level.

## Status

| Part | Content |
|---|---|
| `backend/ticketbox-core` | Pure Java domain (no framework): standard/VIP seats, seat map generation, business exceptions |
| `backend/ticketbox-api` | Spring Boot 3.5 REST API + PostgreSQL + Flyway: events, seats, venues, reports |
| `learning-demo/java-core-demo` | Standalone Java Core examples by topic: OOP, Collections, Exceptions, Generics, I/O, Threads, Lambda/Stream, JDBC, Networking, Annotations |
| `learning-demo/testing-demo` | JUnit 5, Mockito, TDD, integration testing |
| `frontend` | React UI for 4 roles, **currently running on mock data (MSW), not yet connected to the backend** |

Not implemented yet: login/JWT, booking, payment (later phases).

## Requirements

Java 21 · Maven 3.9+ · Docker (PostgreSQL) · Node 20+ and pnpm (`corepack enable`)

## Run

```bash
# Backend
docker compose up -d                       # PostgreSQL on localhost:5433
cd backend
mvn install -DskipTests                    # build core so the api module can use it
mvn -pl ticketbox-api spring-boot:run -Dspring-boot.run.profiles=dev
# Swagger: http://localhost:8080/swagger-ui.html
# The dev profile loads sample data: 3 venues, 1,000 events

# Frontend (no backend needed)
cd frontend && pnpm install && pnpm dev     # http://localhost:8000
```

## Test

```bash
cd backend && mvn verify                         # core + api
cd learning-demo/java-core-demo && mvn test      # no Docker needed (JDBC tests use H2)
cd learning-demo/testing-demo && mvn test
```

## API

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/venues` | List venues |
| POST | `/api/v1/events` | Create a DRAFT event and generate its seat map (201 + `Location`) |
| GET | `/api/v1/events?status=&keyword=&from=&to=&page=&size=&sort=` | Search with paging (max 100 rows per page) |
| GET / PUT | `/api/v1/events/{id}` | Event detail / update name and start time (DRAFT only) |
| POST | `/api/v1/events/{id}/publish`, `/cancel` | Change status |
| GET | `/api/v1/events/{id}/seats?status=` | Seat map |
| GET | `/api/v1/reports/revenue?from=&to=&limit=` | Top events by revenue (plain SQL) |

Errors follow RFC 7807 (`ProblemDetail`) with an `errorCode`: 400 invalid input, 404 not found, 409 invalid state.

## Backend structure

```
ticketbox-api/src/main/java/com/noah/ticketbox/api
├── common/   config (beans, Swagger), web (error handling, PageResponse), persistence (BaseEntity)
├── event/    Controller → Service → Repository, Entity, Mapper, Specification, dto/
├── seat/     seat map
├── venue/    venues
└── report/   revenue report with NamedParameterJdbcTemplate
```

- Controllers never return entities, only `record` DTOs.
- Business rules live in entities/domain (`publish()`, `cancel()`), not in controllers.
- The schema is owned by Flyway (`db/migration`); sample data only in the `dev` profile (`db/seed`).

## Java Core topics → files

`jcd/` = `learning-demo/java-core-demo/src/main/java/demo/javacore/`

| Topic | Files |
|---|---|
| OOP | `ticketbox-core/.../domain/Seat.java`, `VipSeat.java` (abstract, sealed, polymorphism) · `jcd/thread/SeatRegistry.java` (one interface, several implementations) |
| Collections | `jcd/collection/SeatStats.java` (`EnumMap`, `TreeMap`, `TreeSet`) · `jcd/generic/InMemoryRepository.java` |
| Exceptions | `ticketbox-core/.../exception/` (unchecked) · `jcd/io/CsvFormatException.java` (checked) · `GlobalExceptionHandler.java` |
| Generics | `jcd/generic/Repository.java` (bounded type, `? extends` / `? super`) · `PageResponse<T>` |
| I/O | `jcd/io/SeatCsv.java` (`Files`, buffered reader/writer, try-with-resources) |
| Threads | `jcd/thread/` (race condition, `synchronized`, `putIfAbsent`, `ExecutorService`, `CountDownLatch`) |
| Lambda/Stream | `jcd/collection/SeatStats.java` · `ticketbox-core/.../SeatMapFactory.java` · `EventSpecifications.java` |
| JDBC | `jcd/jdbc/EventJdbcRepository.java` (plain JDBC, transactions) · `RevenueReportRepository.java` (Spring JDBC) |
| Networking | `jcd/network/EchoServer.java`, `EchoClient.java` (TCP) · `EventApiClient.java` (`HttpClient`) |
| Annotations | `jcd/annotation/Required.java`, `SimpleValidator.java` (custom annotation + reflection) |
| Testing | `learning-demo/testing-demo` (JUnit, Mockito, TDD) · `backend/ticketbox-api/src/test` (`@WebMvcTest`, Mockito) |
