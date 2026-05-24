# Event Ledger API

A Spring Boot REST API for tracking financial transaction events with built-in idempotency and out-of-order event tolerance.

---

## Prerequisites

### Without Docker
| Tool | Version |
|------|---------|
| Java | 17+     |
| Maven | 3.8+   |

### With Docker
| Tool | Version |
|------|---------|
| Docker | 24+  |
| Docker Compose | 2.x |

---

## Setup & Run

### Option A — Maven (local)

```bash
git clone <repository-url>
cd LedgerProject
mvn spring-boot:run
```

The API starts at **http://localhost:8080**.

> **First-time build** downloads dependencies (~30 s on a fresh machine). Subsequent starts are fast.

### Option B — Docker Compose

```bash
git clone <repository-url>
cd LedgerProject
docker-compose up --build
```

The API starts at **http://localhost:8080**. The first build takes ~2 minutes to download the Maven image and compile. The multi-stage `Dockerfile` produces a slim JRE-only runtime image (~250 MB).

---

## Run Tests

```bash
mvn test
```

All unit tests and integration tests run against an in-memory H2 database — no external setup needed.

---

## API Documentation

| URL | Description |
|-----|-------------|
| http://localhost:8080/swagger-ui.html | Interactive Swagger UI |
| http://localhost:8080/api-docs        | Raw OpenAPI JSON spec  |
| http://localhost:8080/h2-console      | H2 database console (JDBC URL: `jdbc:h2:mem:ledgerdb`, user: `sa`, no password) |

---

## Endpoints

| Method | Endpoint                          | Description                                         |
|--------|-----------------------------------|-----------------------------------------------------|
| POST   | `/events`                         | Submit a transaction event                          |
| GET    | `/events/{id}`                    | Retrieve a single event by its ID                   |
| GET    | `/events?account={accountId}&page=0&size=20` | List events for an account, paginated, chronological |
| GET    | `/accounts/{accountId}/balance`   | Get the computed net balance for an account         |

---

## Example Requests

### Submit an event
```bash
curl -X POST http://localhost:8080/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "evt-001",
    "accountId": "acct-123",
    "type": "CREDIT",
    "amount": 150.00,
    "currency": "USD",
    "eventTimestamp": "2026-05-15T14:02:11Z",
    "metadata": { "source": "mainframe-batch", "batchId": "B-9042" }
  }'
```

### Get account balance
```bash
curl http://localhost:8080/accounts/acct-123/balance
```

### List events (chronological)
```bash
curl "http://localhost:8080/events?account=acct-123"
```

---

## Key Design Decisions

### Idempotency
Submitting the same `eventId` twice returns the **original event with HTTP 200** — no duplicate is stored and the balance is unaffected. A unique database index on `eventId` provides a second safety net for concurrent duplicate inserts (returns HTTP 409 in that edge case).

### Out-of-Order Tolerance
Events are always listed and balanced in chronological order by `eventTimestamp`, not by arrival time. Ordering is delegated to the database query (`ORDER BY eventTimestamp ASC`), so it is correct regardless of when events arrive.

### Balance Computation
```
balance = Σ CREDIT amounts − Σ DEBIT amounts
```
Computed on-the-fly from all stored events — no cached balance field that could drift.

### SOLID Principles
| Principle | Application |
|-----------|-------------|
| **S**ingle Responsibility | `EventMapper`, `EventServiceImpl`, `BalanceServiceImpl`, `GlobalExceptionHandler` each have one job |
| **O**pen/Closed | Service interfaces (`EventService`, `BalanceService`) allow alternative implementations without touching controllers |
| **L**iskov Substitution | Controllers depend on interfaces, not concrete classes |
| **I**nterface Segregation | `EventService` and `BalanceService` are separate, focused interfaces |
| **D**ependency Inversion | All dependencies are injected via constructor (no `new` in business logic) |

---

## Project Structure

```
src/
├── main/java/com/broteen/ledger/
│   ├── config/          # OpenApiConfig
│   ├── controller/      # EventController, AccountController
│   ├── domain/
│   │   ├── entity/      # Event (JPA entity)
│   │   └── enums/       # EventType (CREDIT | DEBIT)
│   ├── dto/
│   │   ├── request/     # EventRequest (with validation)
│   │   └── response/    # EventResponse, BalanceResponse, ErrorResponse
│   ├── exception/       # Domain exceptions + GlobalExceptionHandler
│   ├── mapper/          # EventMapper (entity ↔ DTO)
│   ├── repository/      # EventRepository (Spring Data JPA)
│   └── service/         # EventService, BalanceService interfaces + impls
└── test/java/com/broteen/ledger/
    ├── controller/      # Integration tests (MockMvc + H2)
    └── service/         # Unit tests (Mockito)
```
