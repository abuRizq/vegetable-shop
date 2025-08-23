---
id: ADR-0004
title: Transactional Outbox Pattern for Reliable Event Publishing
status: Accepted
date: 2025-08-20
deciders:
  - VeggieShop Maintainers
  - Platform & Architecture Leads
consulted:
  - Context Maintainers (auth, catalog, inventory, order, pricing, customer, checkout, vendor, media, review, notification, audit)
tags: [messaging, outbox, kafka, reliability, consistency, architecture]
---

## 1. Context

VeggieShop spans multiple bounded contexts with asynchronous interactions via Kafka. Publishing events _directly_ from application code risks **dual-write anomalies** (DB commit succeeds but message publish fails—or vice versa). We need **exactly-once effects** relative to the database state observed by the system.

## 2. Decision

Adopt the **Transactional Outbox** pattern with a **relay** process that publishes to Kafka. Domain/application logic persists events atomically with the DB transaction; a background **OutboxRelay** reads committed outbox rows and publishes them to Kafka with **at-least-once** delivery and idempotent consumers.

- Authoring module: `modules/messaging/outbox/*`
- Kafka publishing: `modules/messaging/kafka/*`
- Support tooling: `TransactionalOutboxAspect`, `OutboxService`, `OutboxRelay`, `KafkaOutboxPublisher`

## 3. Scope

In-scope:
- All cross-context domain events that represent state changes (e.g., `OrderCreated`, `OrderPaid`, `StockReserved`, `NotificationRequested`).

Out-of-scope:
- Request/response patterns; synchronous HTTP calls.
- Non-domain telemetry events (logs/metrics/traces).

## 4. Rationale

- **Atomicity:** Events stored in the same DB transaction as state change ⇒ no dual-writes.
- **Reliability:** Relay uses retries/backoff and persistent offsets.
- **Observability:** Auditable outbox table with statuses and error payloads.
- **Compatibility:** Works with Postgres and Spring transaction model.

## 5. Design

### 5.1 Outbox Table

Minimal schema (simplified for illustration):

```sql
CREATE TABLE outbox (
  id               UUID PRIMARY KEY,
  aggregate_type   TEXT NOT NULL,
  aggregate_id     TEXT NOT NULL,
  event_type       TEXT NOT NULL,
  payload          JSONB NOT NULL,
  headers          JSONB NULL,
  occurred_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  published_at     TIMESTAMPTZ NULL,
  status           TEXT NOT NULL DEFAULT 'PENDING',  -- PENDING, SENT, FAILED
  attempt          INT NOT NULL DEFAULT 0,
  partition_key    TEXT NULL,                         -- for ordering/affinity
  dedup_key        TEXT NULL                          -- idempotency key for consumers
);
CREATE INDEX idx_outbox_pending ON outbox (status, occurred_at);
```

### 5.2 Write Path (Transactional)

- Application services call `OutboxService` (or annotate methods with `@TransactionalOutbox`) to persist events alongside domain changes in a single transaction.
- Events are serialized to JSON using stable schemas under `modules/contracts/events/schema/**`.

### 5.3 Relay Path

- `OutboxRelay` continuously polls **PENDING** rows in small batches (configurable), publishes to Kafka via `KafkaOutboxPublisher`, marks as **SENT** on success; on failure increments `attempt` and applies **exponential backoff + jitter**.
- Relay runs as:
  - a thread within the app (dev/staging), or
  - a dedicated container in prod (recommended for isolation and restart policy).

### 5.4 Ordering & Partitioning

- Use `partition_key` (e.g., `orderId`, `customerId`) to route related events to the same Kafka partition, providing **per-aggregate ordering**.
- Event _causal_ ordering within an aggregate is guaranteed by DB transaction commit order + single-partition publish.

### 5.5 Idempotency

- **At-least-once** publish may result in duplicates. Consumers must be **idempotent** using:
  - `dedup_key` (natural idempotency key, e.g., event UUID), and
  - an **inbox** table (optional) to record processed event IDs per consumer.

### 5.6 Backpressure & Throttling

- Tune batch size, poll interval, and max retries.
- Circuit-break publish on persistent broker errors; surface metrics/alerts to SRE.

### 5.7 Error Handling

- Failed publish ⇒ `status=FAILED`, relay retries with exponential backoff up to `MAX_ATTEMPTS` (or forever with dead-letter after N attempts).
- Record last error cause in headers or a separate error table for forensics.
- Provide a **requeue** admin operation to move `FAILED` → `PENDING`.

### 5.8 Serialization

- JSON serialization matches schemas in `modules/contracts/events/schema/**`.
- Version events using **backward-compatible** changes; add fields with defaults, avoid breaking removals.

### 5.9 Housekeeping

- Periodic job to archive or delete old **SENT** rows (configurable retention).
- Protect table with VACUUM and suitable indexes to keep relay fast.

## 6. Spring Integration

- Transactional annotation/aspect wraps app service methods to capture emitted domain events.
- Publisher uses **idempotent** Kafka producer settings (`enable.idempotence=true`) and proper acks (`acks=all`). This does not remove the need for consumer idempotency.

## 7. Observability

Emit metrics:
- Relay lag (oldest PENDING age)
- Publish success/failure counters by `event_type`
- Retry histogram (attempt counts)
- Outbox table size and retention health
- End-to-end latency (`occurred_at` → published timestamp)

Correlate with application `trace_id` via headers.

## 8. Security & Compliance

- Avoid placing PII in `payload`; prefer domain identifiers. If necessary, encrypt fields at producer or avoid including them entirely.
- Ensure payload size constraints to avoid broker issues; chunk or reference external storage if needed.

## 9. Testing Strategy

- Unit tests for serialization and outbox write helpers.
- Integration tests using Testcontainers for Postgres + Kafka; assert **exactly-once effect** relative to DB.
- Contract tests to validate event schema compatibility.

## 10. Alternatives Considered

1. **Direct publish within transaction** — not supported by Kafka; still subject to dual-write risk.  
2. **Outbox via DB triggers** — more opaque; harder to test/version; _possible_ but less explicit.  
3. **Change Data Capture (CDC) with Debezium** — viable but adds infra complexity; outbox is simpler and sufficient.

## 11. Consequences

**Positive**
- Strong reliability with simple operational model.
- Clear audit trail.  
**Negative**
- Extra component (relay) to run and monitor.
- Outbox table growth without housekeeping.

## 12. Implementation Plan

1. Use existing `modules/messaging/outbox/*` and `modules/messaging/kafka/*` as baseline.  
2. Add retention job and admin requeue endpoint/CLI.  
3. Wire configuration via env (`OUTBOX_*`, `KAFKA_*`).  
4. Add system tests that simulate publish failures and verify retries + idempotency.

## 13. Rollback

- Relay can be disabled via config; app continues without publishing (events accumulate).  
- Re-enable relay and drain backlog. In extreme cases, export rows to files and replay with a safe tool.

## 14. Open Questions

- Do we need a **dead-letter topic** per event type after N failures?  
- Should we adopt a **global** inbox table library for consumers?

## 15. References

- `modules/messaging/outbox/*`, `modules/messaging/kafka/*`
- Event schemas: `modules/contracts/events/schema/**`
- Related ADRs: `ADR-0002-flyway-single-module.md`, `ADR-0006-ports-and-adapters.md`
