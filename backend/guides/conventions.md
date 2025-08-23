# Engineering Conventions

This document defines project-wide conventions for the **VeggieShop Backend**. Conventions keep code predictable, accelerate reviews, and help us scale with confidence.

> TL;DR: Domain stays pure, adapters do the talking, and every error is **Problem Details**.

---

## Languages, Tooling & Versions
- **Java:** 21 (LTS). Use `record` for pure value objects.
- **Build:** Maven (wrapper `./mvnw`).
- **Framework:** Spring Boot 3.x.
- **DB:** PostgreSQL 14+; SQL-first migrations by **Flyway**.
- **Messaging:** Kafka.
- **Docs & Diagrams:** OpenAPI / PlantUML.
- **Secrets:** SOPS (`.sops.yaml`).

---

## Repository Structure (recap)
```
modules/
  domain-kernel/           # shared, framework-free value objects & validators
  platform*/               # cross-cutting: web errors, security, persistence, observability
  messaging/               # outbox, relay, kafka adapters
  migrations/              # Flyway SQL (single authoritative location)
  contexts/
    context-<name>/
      domain/              # entities, VOs, domain services (no frameworks)
      application/         # use cases, ports, services
      infrastructure/      # adapters (JPA, Kafka, HTTP, storage)
      api/http/            # controllers, DTOs, mappers
apps/veggieshop-service/   # boot app
```

---

## Packages & Naming
- Packages start with `com.veggieshop.<context>.<layer>`. Keep **layers shallow**.
- **Ports** end with `Port` or `Gateway` (e.g., `PaymentGatewayPort`).
- **Adapters** end with `Adapter` (e.g., `UserRepositoryAdapter`).
- **DTOs** end with `DTO`, requests with `Request` (e.g., `LoginRequest`).
- **Repositories** (Spring Data) end with `Repository` (e.g., `OrderRepository`).
- **Mappers** end with `Mapper` and live under `api/http/mapper` or `infrastructure/persistence/...`.
- **Events** use past tense nouns (e.g., `OrderCreated`).

---

## Architecture Boundaries (Ports & Adapters)
- Controllers **only** call **application services**.
- Application services **depend on ports**, never on frameworks.
- Adapters (persistence, messaging, HTTP clients) **implement ports**.
- Domain model is **framework-free**. No `@Entity`, no Jackson in `domain` or `domain-kernel`.
- Enforced by **ArchUnit** (see `modules/testing`).

---

## Error Handling (ADR‑0003)
- All errors are **RFC 7807 Problem Details** (`application/problem+json`).
- Use `ErrorResponseFactory` & `ExceptionMappingService` to map exceptions.
- Validation errors → **422** with `field_errors` extension.
- Include `trace_id` and `error_code` extensions; never leak secrets or PII.

---

## Transactions
- **Boundary:** application services (`@Transactional`).
- No transactions in controllers.
- Domain is unaware of transactions/time; pass `Clock` if needed.

---

## Validation
- **Edge validation** in controllers (Bean Validation annotations on DTOs).
- **Invariants** inside value objects and aggregates (constructors/factories throw).
- Avoid silent coercions; require explicit `RoundingMode`, units, etc.

---

## Persistence
- JPA entities live **only** in infrastructure. Map domain ↔ entity via mappers or converters.
- No lazy surprises across boundaries—DTOs are shaped for API needs, not entities.
- Queries:
  - Prefer Spring Data + Specifications/Projections for complex reads.
  - Write **explicit indexes** in Flyway migrations.
- IDs are **UUIDv4** (unless otherwise justified).

---

## Migrations (ADR‑0002)
- **Single** module `modules/migrations` holds all Flyway SQL.
- Naming: `V<seq>__<context>_<short_desc>.sql` (e.g., `V8__order_add_txn_idx.sql`).
- Never edit applied migrations—create a new one. Favor transactional DDL.

---

## Messaging (ADR‑0004)
- Publish via **Transactional Outbox**; never publish directly from use cases/transactions.
- Provide `partition_key` for ordering (e.g., order/customer id).
- Consumers are **idempotent** (inbox or dedup key).

---

## REST API Design
- Resource names are **plural**, kebab-case paths: `/api/products`, `/api/orders/{id}`.
- **Pagination:** cursor-based with `next`/`prev` tokens. Return summaries by default, with `?expand=` for heavy fields.
- **Status codes:** standard HTTP (422 for validation). Avoid 200 for errors.
- **Consistency:** shared headers, Problem Details, and error codes across contexts.
- **OpenAPI:** components reused; examples provided; **no** ad-hoc schemas in controllers.

---

## Security
- JWT Bearer for auth; scopes/roles checked with method-level annotations.
- Enforce CORS via `platform` config.
- Rate-limit public endpoints (`RequestRateLimiter`).
- Never log secrets; scrub structured logs. Use `trace_id` for correlation.
- Follow OWASP recommendations (input validation, headers, CSRF for cookie flows).

---

## Logging & Observability
- Use MDC `trace_id` everywhere. Add domain identifiers where helpful (safe to log).
- Emit metrics for latency, error counts by `status`/`error_code`, outbox lag, etc.
- Prefer **structured logs** (JSON) in production.

---

## Testing Strategy
- **Unit:** domain + application (ports faked). Fast; high coverage.
- **Integration:** adapters with Testcontainers (Postgres, Kafka).
- **Contract:** OpenAPI & Problem Details shape.
- **System:** end-to-end critical flows (checkout, payment, reservation).

---

## Coding Style
- Follow **Google Java Style** or Checkstyle config in CI (when present).
- Use `record` where possible; immutable value objects.
- Null-safety: prefer `Optional` for optional returns; guard preconditions.

---

## Git & PRs
- **Conventional Commits**: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`
- PR title mirrors commit; include context labels.
- Link issues/ADRs/Decision Logs in PR description.
- CODEOWNERS auto-request appropriate reviewers.

---

## Deprecation
- Mark with Javadoc `@deprecated` and add a replacement.
- Keep compatibility for at least **one** minor release before removal.
