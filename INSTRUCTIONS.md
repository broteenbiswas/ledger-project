# Event Ledger API — Development Plan

This document outlines the step-by-step plan for building the Event Ledger API. Each step maps directly to a commit in the repository.

---

## Objective

Build a Spring Boot REST API that:
- Accepts financial transaction events (CREDIT / DEBIT)
- Ensures idempotency — duplicate `eventId` submissions return the original event
- Handles out-of-order event arrival correctly
- Computes account balances on demand
- Follows SOLID principles with no file exceeding 500 lines

---

## Step-by-Step Plan

### Step 1 — Initialize the project
**Commit:** `chore: initialize Spring Boot project with Maven and core dependencies`

- Bootstrap a Spring Boot 3.2.5 project using Maven
- Add dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `h2` (runtime)
- Configure `application.yml` with H2 in-memory database (`jdbc:h2:mem:ledgerdb`), Hibernate `ddl-auto: create-drop`, and server port 8080

---

### Step 2 — Define the domain model and repository
**Commit:** `feat: add domain model and JPA repository layer`

- Create `EventType` enum with `CREDIT` and `DEBIT` values
- Create `Event` JPA entity with fields: `id`, `eventId`, `accountId`, `type`, `amount`, `currency`, `eventTimestamp`, `metadata`, `receivedAt`
- Add a **unique index on `eventId`** (database-level idempotency guard) and a regular index on `accountId`
- Create `EventRepository` extending `JpaRepository` with:
  - `findByEventId(String)` — idempotency check
  - `findByAccountIdOrderByEventTimestampAsc(String)` — chronological balance computation
  - `findByAccountId(String, Pageable)` — paginated event listing
  - `existsByAccountId(String)` — account existence check

---

### Step 3 — Define request and response DTOs
**Commit:** `feat: add request and response DTOs with Bean Validation`

- Create `EventRequest` with Bean Validation annotations:
  - `@NotBlank` on `eventId`, `accountId`, `currency`
  - `@NotNull` on `type`, `amount`, `eventTimestamp`
  - `@DecimalMin(value = "0.0", inclusive = false)` on `amount`
  - Optional `Map<String, Object> metadata`
- Create response DTOs: `EventResponse`, `BalanceResponse`, `PagedEventResponse`, `ErrorResponse`
- Keep DTOs separate from the entity — never expose the JPA entity directly

---

### Step 4 — Add domain exceptions and global error handler
**Commit:** `feat: add domain exceptions and global error handler`

- Create domain exceptions: `DuplicateEventException`, `EventNotFoundException`, `AccountNotFoundException`
- `DuplicateEventException` carries the original `EventResponse` so the handler can return it directly
- Create `GlobalExceptionHandler` with `@RestControllerAdvice` to map exceptions to HTTP responses:
  - `DuplicateEventException` → 200 OK (idempotent resubmission)
  - `EventNotFoundException` / `AccountNotFoundException` → 404
  - `MethodArgumentNotValidException` → 400 with field-level details
  - `HttpMessageNotReadableException` → 400 (invalid enum values)
  - `MissingServletRequestParameterException` → 400
  - `DataIntegrityViolationException` → 409 (concurrent duplicate inserts)
  - `Exception` (catch-all) → 500

---

### Step 5 — Implement the mapper and service layer
**Commit:** `feat: add mapper and service layer with core business logic`

- Create `EventMapper` to convert between `EventRequest` → `Event` entity → `EventResponse`
  - Use Jackson `ObjectMapper` to serialize/deserialize `Map<String, Object> metadata` to/from a JSON TEXT column
- Define `EventService` and `BalanceService` interfaces (Open/Closed principle)
- Implement `EventServiceImpl`:
  - `submitEvent`: check for duplicate via `findByEventId`, throw `DuplicateEventException` if found, otherwise save
  - `getEventById`: look up by `eventId`, throw `EventNotFoundException` if missing
  - `getEventsByAccount`: paginated query sorted by `eventTimestamp` ascending
- Implement `BalanceServiceImpl`:
  - Fetch all events for account ordered by `eventTimestamp`
  - Compute balance: `Σ CREDIT amounts − Σ DEBIT amounts` using stream reduce
  - Order of arrival does not affect the result — only `eventTimestamp` is used for ordering

---

### Step 6 — Implement REST controllers
**Commit:** `feat: add REST controllers for events and accounts`

- Create `EventController` with:
  - `POST /events` → 201 Created
  - `GET /events/{id}` → 200 OK
  - `GET /events?account={id}&page=0&size=20` → paginated response
- Create `AccountController` with:
  - `GET /accounts/{accountId}/balance` → balance response
- All controllers depend on service interfaces, not concrete implementations (Liskov / Dependency Inversion)
- Clamp `size` parameter to a maximum of 100

---

### Step 7 — Add OpenAPI / Swagger documentation
**Commit:** `feat: add OpenAPI/Swagger configuration`

- Add `springdoc-openapi-starter-webmvc-ui:2.5.0` dependency
- Create `OpenApiConfig` with project title, description, contact, and license
- Annotate all controllers with `@Tag`, `@Operation`, `@ApiResponses`, and `@Parameter`
- Swagger UI available at `/swagger-ui.html`, raw spec at `/api-docs`

---

### Step 8 — Write unit and integration tests
**Commit:** `test: add unit and integration tests covering all core requirements`

- Unit tests with Mockito:
  - `EventServiceImplTest` — 5 tests covering submit, duplicate, getById, pagination
  - `BalanceServiceImplTest` — 5 tests covering mixed events, only credits, only debits, unknown account, out-of-order
- Integration tests with MockMvc + real H2:
  - `EventControllerIntegrationTest` — POST/GET scenarios including idempotency and chronological ordering
  - `AccountControllerIntegrationTest` — balance correctness, 404, duplicate guard, out-of-order, negative balance
- Use `@DirtiesContext(BEFORE_EACH_TEST_METHOD)` to reset H2 state between tests

---

### Step 9 — Add pagination, Docker support, and README
**Commit:** `feat: add pagination, Dockerized setup, and README`

- Implement pagination on `GET /events?account=` using Spring Data `PageRequest` with `Sort.by("eventTimestamp").ascending()`
- Add `Dockerfile` with a multi-stage build:
  - Stage 1: `maven:3.9.6-eclipse-temurin-17` — compile and package
  - Stage 2: `eclipse-temurin:17-jre-jammy` — slim runtime image
- Add `docker-compose.yml` with health check
- Write `README.md` covering setup, endpoints, design decisions, and SOLID table

---

### Step 10 — Expand test coverage to 99%
**Commit:** `test: expand coverage to 99% with exception, mapper, and entity tests`

- Add JaCoCo Maven plugin for coverage reporting
- Add `GlobalExceptionHandlerTest` — unit tests for `DataIntegrityViolationException`, generic `Exception`, and `MissingServletRequestParameterException` handlers
- Add `EventMapperTest` — cover Jackson serialize/deserialize failure catch branches
- Add `EventTest` — cover entity `getId()`/`setId()` and all remaining getters/setters
- Add missing `account` param test to `EventControllerIntegrationTest`
- Final coverage: **99% instructions, 100% branches**

---

### Step 11 — Add structured logging
**Commit:** `feat: add SLF4J logging to service, mapper, and exception handler layers`

- Add SLF4J `Logger` to `EventServiceImpl`, `BalanceServiceImpl`, `GlobalExceptionHandler`, and `EventMapper`
- Log levels:
  - `INFO` — successful event submission and balance computation
  - `WARN` — duplicate events, not-found errors, missing params, unreadable request body
  - `ERROR` — data integrity violations and unhandled exceptions (with full stack trace)
  - `DEBUG` — fetch operations (`getEventById`, `getEventsByAccount`, `getBalance`)

---

## SOLID Principles Applied

| Principle | How |
|---|---|
| **Single Responsibility** | `EventMapper`, `EventServiceImpl`, `BalanceServiceImpl`, `GlobalExceptionHandler` each have one job |
| **Open/Closed** | `EventService` and `BalanceService` interfaces allow new implementations without touching controllers |
| **Liskov Substitution** | Controllers depend on interfaces, not concrete classes |
| **Interface Segregation** | `EventService` and `BalanceService` are separate, focused interfaces |
| **Dependency Inversion** | All dependencies injected via constructor — no `new` in business logic |
