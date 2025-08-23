---
id: ADR-0002
title: Centralize Flyway Migrations in a Single Module
status: Accepted
date: 2025-08-20
deciders:
  - VeggieShop Maintainers
  - Platform & Architecture Leads
consulted:
  - Context Maintainers (auth, catalog, inventory, order, pricing, customer, checkout, vendor, media, review, notification, audit)
tags: [database, flyway, migrations, monorepo, devops, postgres]
---

## 1. Context

VeggieShop is a multi-module, DDD-oriented monorepo with many bounded contexts sharing a single PostgreSQL cluster (and typically a single schema per environment). Historically, projects let each module ship its own migration scripts. That approach increases drift, ordering conflicts, duplicated DDL, and production risk.

We already maintain SQL migrations under `modules/migrations/src/main/resources/db/migration`, with versions like `V1__baseline_auth_core.sql` through `V7__outbox_inbox_jobs.sql`. We need to formalize the **single-module** strategy and establish conventions that scale as contexts evolve.

## 2. Decision

**All database migrations are centralized in one module: `modules/migrations`.**

- The **only** Flyway location on the classpath is `classpath:db/migration` from the **migrations** module.
- Application startup runs Flyway based on environment configuration **or** a dedicated **migrator** container/job runs before app rollout.
- Context modules **must not** ship their own `db/migration` folders or Flyway SQL files.

## 3. Scope

**In-scope:**
- DDL/DML needed to evolve production schemas and reference data.
- Cross-context schema coordination (FKs, indexes, outbox/inbox tables, background jobs).

**Out-of-scope:**
- Non-production seed/demo data (`tools/db/seeds/*.sql`), executed manually in dev.
- Temporary test fixtures (handled via Testcontainers or per-test setup).

## 4. Rationale

- **Ordering & determinism:** A single chain of versioned scripts provides a clear linear history.
- **Safety:** One owning place to review/approve schema changes; easier to roll forward with confidence.
- **Observability:** One Flyway schema history table to audit what ran and when.
- **Operations:** Consistent execution model across environments; less chance of partial upgrades.
- **DDD alignment:** Schema is a shared operational concern; contexts integrate via events/APIs, while the physical storage is coordinated centrally.

## 5. Conventions & Design

### 5.1 Locations & Configuration
- **Module:** `modules/migrations`
- **Flyway locations:** `SPRING_FLYWAY_LOCATIONS=classpath:db/migration`
- **Schema:** `DB_SCHEMA` (default `public`) via `SPRING_JPA_PROPERTIES_HIBERNATE_DEFAULT_SCHEMA`
- **Baseline:** `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` for brownfield support
- **Enabled:** `SPRING_FLYWAY_ENABLED=true` (may be disabled in prod when using migrator job)

### 5.2 Versioned vs Repeatable
- Prefer **versioned** migrations: `V<N>__<snake_or_kebab_desc>.sql` (e.g., `V8__order_tables.sql`).
- Use **repeatable** `R__*.sql` only for *derived artifacts* (views, functions, materialized views) and *idempotent* reference data. Avoid business data updates in repeatables.

### 5.3 Authoring Rules
- **Immutability:** Never edit an applied migration; create a new version to change behavior.
- **Idempotence:** Where possible, use `IF NOT EXISTS` / `CREATE OR REPLACE` for non-destructive ops.
- **Transactional:** Prefer transactional DDL. If a step is non-transactional, document clearly in comments and split carefully.
- **Naming:** Prefix description with context if it helps traceability, e.g., `V9__catalog_add_product_image.sql`.
- **Comments:** Include context, ticket/PR link, and author in SQL comments at top of file.
- **Placeholders:** If required, define Flyway placeholders via `SPRING_FLYWAY_PLACEHOLDERS_*` and reference with `${placeholder}`.

### 5.4 Zero-Downtime Strategy
Follow **expand → migrate → contract**:
1. **Expand:** Add new columns/tables with defaults; make writes compatible.
2. **Migrate:** Backfill data in small batches (SQL or background job).
3. **Contract:** Remove old columns/constraints after all consumers are updated.

### 5.5 Long-Running & Risky Changes
- Prefer **out-of-band** operations (DBAs/backfill jobs) for heavy data moves; migrations should orchestrate schema, not long batch ETL.
- Gate with **feature flags** to avoid breaking readers/writers during rollout.

### 5.6 Ownership & Reviews
- CODEOWNERS for `modules/migrations/**` includes platform DB owners + affected context maintainers.
- PRs must link to issues/ADRs/decision logs and include estimated impact (locks, downtime risk).

## 6. Execution Models

Two supported execution modes:

1) **At App Startup (dev/staging/local):**  
   App starts Flyway before context initialization. Simple and fast.
  - Set `SPRING_FLYWAY_ENABLED=true`
  - Ensure DB is reachable; keep migrations quick and safe.

2) **Dedicated Migrator (prod-first):**  
   A separate container/job (`docker/infra/migrator/Dockerfile`) runs migrations before the new app version is rolled out.
  - App startup migration can be disabled in prod (`SPRING_FLYWAY_ENABLED=false`) to avoid double runs.
  - CI/CD pipeline stage: *migrate → deploy*.

## 7. CI/CD Integration

- `build.yml`/`pr-checks.yml` run `./mvnw clean verify` ensuring SQL compiles in tests (via Testcontainers if present).
- Optional: add a **dry-run** validation step for SQL linting (e.g., `sqlfluff`/`psql` schema checks) in containers.
- Publish Flyway info (`flyway info`) as a build artifact for auditing (optional).

## 8. Enforcement

- Scan repository in CI to **fail** if any module other than `modules/migrations` contains `src/main/resources/db/migration`.
- ArchUnit or custom checks can enforce that only the executable app and migrator depend on the migrations module for classpath locations.
- PR template requires checking: _"Has a new migration been added for schema changes?"_

## 9. Alternatives Considered

1. **Per-context migrations (multi-location):**  
   *Rejected.* Causes ordering conflicts, hidden dependencies, and coupling at deploy time.
2. **Multiple schemas per context:**  
   *Deferred.* Feasible, but increases operational overhead (cross-schema joins, permissions). Consider only if isolation requirements justify complexity.
3. **Liquibase instead of Flyway:**  
   *Rejected for now.* Flyway fits our simple, SQL-first workflow and is already integrated. Switching offers limited benefit relative to cost.
4. **ORM auto-DDL (`hibernate.hbm2ddl.auto`)**:  
   *Rejected.* Non-deterministic, unsafe for production, and conflicts with DDL review culture.

## 10. Consequences

**Positive**
- Single source of truth; predictable ordering and audit trail.
- Easier reviews and safer rollouts.
- Better collaboration across contexts.

**Negative / Risks**
- Central queue can become a bottleneck for teams; mitigate with small, incremental migrations and clear ownership.
- Cross-context coupling still exists at the physical DB layer; requires coordination and communication.

## 11. Implementation Plan

- Keep **Flyway** SQL here: `modules/migrations/src/main/resources/db/migration`
- Maintain current sequence: `V1__baseline_auth_core.sql` … `V7__outbox_inbox_jobs.sql`
- Introduce **naming** convention: `V<next>__<context>_<short_desc>.sql`
- Update **Makefile** target `migrate` and migrator image usage in `docker/infra/migrator/Dockerfile` docs.
- Add CI safeguard to ban stray `db/migration` folders outside the migrations module.

## 12. Rollback Strategy

- **Rollbacks** (downgrades) are not guaranteed; prefer **roll-forward** with corrective migrations.
- If an emergency rollback is required:
  - Disable app writes (maintenance mode) if necessary.
  - Apply a **compensating migration** to revert schema/data to a safe state.
  - Document via a decision log and schedule a proper fix.

## 13. Monitoring & Ops

- Track Flyway history table (`flyway_schema_history`) for each environment.
- Observe DB metrics during migrations: locks, long transactions, replication lag.
- Alert on failed migrations; block deployment when migrations fail.

## 14. Open Questions

- Should we introduce **repeatable** migrations for views/functions now or keep all versioned?
- Do we need a **canary migration** stage on a read replica before promoting to primary?
- Should heavy backfills be moved to a separate **job framework** (e.g., Spring Batch) with telemetry?

## 15. References

- Flyway Docs: https://flywaydb.org/documentation
- Project paths: `modules/migrations/src/main/resources/db/migration`
- Make targets: `make migrate`, `make seed`
- Related ADRs: `ADR-0001-shared-kernel.md`, `ADR-0004-outbox-pattern.md`, `ADR-0006-ports-and-adapters.md`
