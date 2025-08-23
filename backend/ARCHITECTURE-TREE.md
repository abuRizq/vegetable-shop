# VeggieShop Backend — Architecture Tree

> This document is a **living map** of the VeggieShop backend architecture. It explains the module graph, layering rules, data & event flows, deployment topology, and practical guidelines for extending the system.

- **Tech Stack:** Java 21, Spring Boot 3.5.x, Maven, PostgreSQL, Kafka, Docker, Helm, SOPS, Testcontainers
- **Style:** DDD + Ports & Adapters, modular monorepo
- **Status:** Production-ready foundations with room for contexts to evolve

---

## 1) High-Level View (C4)

- **C1 – System Context:** `architecture/C1-system-context.puml`
- **C2 – Container Diagram:** `architecture/C2-container.puml`
- **C3 – Component Diagram:** `architecture/C3-component.puml`
- **Key Sequences:** `architecture/catalog-sequence-checkout.puml`
- **ERD:** `architecture/database-erd.puml`

> PlantUML sources above are authoritative for visual structure. Update them with design changes.

---

## 2) Repository Architecture Tree

```
backend/
├─ apps/
│  └─ veggieshop-service/          # Executable Spring Boot application (API)
│
├─ modules/
│  ├─ domain-kernel/               # Shared domain primitives (Value Objects, validation)
│  ├─ platform-autoconfigure/      # Auto-config for platform features
│  ├─ platform-starter/            # BOM/Starter for platform consumers
│  ├─ platform/                    # Cross-cutting: web errors, security, persistence, observability
│  ├─ messaging/                   # Outbox domain + Kafka publisher/relay
│  ├─ migrations/                  # Flyway SQL migrations
│  ├─ contracts/                   # HTTP (OpenAPI) + Event schemas (JSON) + codegen
│  ├─ testing/                     # Test support: base classes, REST/wiremock helpers, ArchUnit rules
│  ├─ system-tests/                # Testcontainers E2E tests (API/DB/Kafka)
│  └─ contexts/                    # Bounded contexts (clean architecture per context)
│     ├─ context-auth/             # AuthN/AuthZ: users, tokens, MFA, sessions, OAuth
│     ├─ context-catalog/          # Products, categories, tags, nutrition, images
│     ├─ context-inventory/        # Warehouses, stock batches, movements, reservations
│     ├─ context-pricing/          # Price lists, promotions, coupons, tax classes
│     ├─ context-customer/         # Customer profiles, addresses, loyalty, wishlist
│     ├─ context-order/            # Cart, order, payment, delivery
│     ├─ context-checkout/         # Checkout sessions
│     ├─ context-vendor/           # Suppliers, farms, compliance docs
│     ├─ context-media/            # Media assets
│     ├─ context-review/           # Reviews, flags
│     ├─ context-notification/     # Notifications, channel prefs
│     └─ context-audit/            # Audit logs
│
├─ config/
│  ├─ local/docker-compose.yml     # Local infra: DB, Kafka, Nginx, etc.
│  └─ helm/veggie-shop/            # Helm chart (values-*.yaml, templates/*)
│
├─ docker/                         # Dockerfiles (app, nginx, migrator)
├─ scripts/                        # Helper scripts: build/test/lint/it/dev_up/down
├─ secrets/                        # SOPS-encrypted placeholders
├─ guides/, docs/, adr/            # Documentation and ADRs
└─ architecture/                   # PlantUML diagrams (C1/C2/C3/ERD/Sequences)
```

---

## 3) Dependency Graph (Module-Level)

```
domain-kernel ─┐
platform       ├──> used by all contexts
messaging      ┘
migrations  ───────► (DB schema; consumed by app on startup or migrator)
contracts  ───────► (OpenAPI & Events for consumers + codegen)

contexts/*  ─────► depend on: domain-kernel, platform, messaging (for events)
apps/veggieshop-service ─► assembles all contexts + platform + messaging
```

**Rules:**
- Contexts **must not** depend directly on other contexts’ internals. Cross-context interaction is via:
  - **HTTP APIs** (controllers declared per context; OpenAPI under `modules/contracts/http/openapi/*.yaml`).
  - **Domain Events** published through the **outbox** (see `modules/messaging/outbox/*`) and consumed asynchronously.
- `platform` provides reusable cross-cutting concerns (web error model, security, observability, persistence config).
- `domain-kernel` contains value objects and validation primitives used across contexts.

---

## 4) Clean Architecture per Context

Each context follows layers:

```
context-xyz/
├─ domain/            # Entities, Value Objects, Domain services, Aggregates
├─ application/       # Ports (interfaces), Use cases (services)
│  └─ port/           # RepositoryPort, GatewayPort, etc.
│  └─ service/        # Application services (interfaces)
│  └─ impl/           # Application service implementations
├─ infrastructure/    # Adapters (JPA repositories, messaging, external APIs)
│  └─ persistence/    # JPA repositories, adapters
│  └─ messaging/      # Kafka consumers/producers (when applicable)
│  └─ security/       # Context-specific security adapters (if any)
├─ api/http/          # REST controllers, DTOs, mappers
│  └─ controller/     # Controllers (@RestController)
│  └─ dto/            # Request/Response DTOs
│  └─ mapper/         # MapStruct or manual mappers
```

**Boundaries (enforced by conventions & ArchUnit):**
- Controllers → Application services → Domain
- Infrastructure implements Ports; Domain never depends on Infrastructure
- DTOs never leak into Domain; use mappers

See ADRs for design intent:
- **ADR-0001:** Shared Kernel
- **ADR-0002:** Flyway in single module
- **ADR-0003:** Problem Details only (error model)
- **ADR-0004:** Outbox pattern
- **ADR-0005:** ArchUnit boundaries
- **ADR-0006:** Ports and Adapters

---

## 5) Data Model & Persistence

- **Database:** PostgreSQL
- **Migrations:** Flyway under `modules/migrations/src/main/resources/db/migration`
- **Entity base classes & repos:** `modules/platform/persistence/*`
- **ERD:** `architecture/database-erd.puml`

**Conventions:**
- No destructive changes to applied migrations—create new `Vx__desc.sql`
- Use projections/specifications for read-optimized queries
- Default schema controlled by env: `DB_SCHEMA` / `SPRING_JPA_PROPERTIES_HIBERNATE_DEFAULT_SCHEMA`

---

## 6) Messaging & Events

- **Pattern:** Transactional Outbox → Relay → Kafka
- **Outbox domain:** `modules/messaging/outbox/*`
- **Kafka publisher:** `modules/messaging/kafka/*`
- **Event Schemas (JSON):** `modules/contracts/events/schema/**`

**Core event families (examples):**
- `order.OrderCreated`, `order.OrderPaid`
- `inventory.StockReserved`
- `notification.NotificationRequested`

> Produce via application services → persist to outbox → relay publishes to Kafka. Consumers react asynchronously (possibly in other bounded contexts or external services).

---

## 7) Web & Error Handling

- Global problem-details response model: `modules/platform/web/error/*`
- Response advice & filters for trace IDs, correlation: `modules/platform/web/*`
- CORS and security config in platform + app `application-*.yml` & env

---

## 8) Security

- **Auth:** JWT-based; context-auth provides endpoints and services
- **MFA, OAuth:** optional via adapters under `context-auth/infrastructure/security/adapters/*`
- **Password policy:** `modules/platform/security/PasswordPolicy.java`
- **Rate limiting:** `RequestRateLimiter` in `modules/platform/security/*`
- **Secrets:** never in code; use `.env` for local and **SOPS**/External Secrets in Kubernetes

---

## 9) Observability

- **Actuator:** health, metrics, info, Prometheus scrape (`/actuator/prometheus`)
- **Tracing:** OpenTelemetry (`OTEL_*` env), `TraceIdFilter`
- **Logging:** Logback with MDC correlation (`logback-spring.xml`)

---

## 10) Configuration & Profiles

- App configuration in `apps/veggieshop-service/src/main/resources/{application,application-local,application-prod}.yml`
- Local env via `.env` (see `backend.env.example`)
- Common keys:
  - DB: `SPRING_DATASOURCE_*`, `DB_*`
  - Kafka: `SPRING_KAFKA_*`, `KAFKA_*`
  - Security/JWT: `AUTH_*`
  - Mail/Media/OTEL/RateLimit: see example env

---

## 11) Containerization & Deployment

- **Dockerfiles:**
  - App: `docker/app/Dockerfile`
  - Nginx: `docker/infra/nginx/Dockerfile`
  - Migrator: `docker/infra/migrator/Dockerfile`
- **Compose (local):** `config/local/docker-compose.yml`
- **Kubernetes (Helm):** `config/helm/veggie-shop`
  - Values: `values-local.yaml`, `values-prod.yaml`
  - Templates: deployment, service, ingress, HPA, PDB, external-secret
- **Secrets:** managed with **SOPS** + External Secrets (Cluster)

---

## 12) Testing Strategy

- **Unit tests:** per module (`modules/*/src/test/java`)
- **Integration tests:** repository/adapters with Testcontainers
- **Contract tests:** OpenAPI + events schema validation
- **System tests:** `modules/system-tests` for E2E flows (API + DB + messaging)
- **Architecture tests:** `modules/testing/src/test/java/.../ArchRules.java`

**CI (GitHub Actions):**
- `build.yml`: compile/test/(SBOM)
- `pr-checks.yml`: lint, tests, scans
- `release.yml`: versioning/publish (if enabled)
- `security.yml`: dependency/code scanning

---

## 13) Dependency & Layering Rules (Must Follow)

- No context → context direct dependencies (use APIs/events)
- Domain model **never** depends on framework annotations beyond minimal JPA annotations where necessary
- Controllers **do not** contain business logic—delegate to application services
- Infra adapters implement ports; domain/application must not import infra packages
- Shared utilities belong to `platform` or `domain-kernel` (avoid copy-paste)

**Recommended Build Guards:**
- Maven Enforcer: require Java 21, upper-bound-deps, dependency convergence
- Static analysis (Spotless/Checkstyle/PMD) + Jacoco coverage threshold

---

## 14) How to Add a New Bounded Context

1. Create `modules/contexts/context-<name>/` with standard layers (domain, application, infrastructure, api/http).
2. Define ports in `application/port/*` and implement adapters in `infrastructure/*`.
3. Add controllers & DTOs in `api/http/*`; add mappers.
4. Add Flyway migrations if persistence is needed.
5. Define events (if any) under `modules/contracts/events/schema/<context>/` and update codegen if used.
6. Update OpenAPI specs in `modules/contracts/http/openapi/<context>.yaml`.
7. Register the module in the root `pom.xml` and wire up Spring configuration.
8. Add tests: unit + integration + contract + (optional) system tests.
9. Update PlantUML diagrams if architecture changes.
10. Add/Update ADR if it introduces a significant decision or tradeoff.

---

## 15) ADR Index (Selected)

- `adr/ADR-0001-shared-kernel.md` — Shared Kernel strategy
- `adr/ADR-0002-flyway-single-module.md` — Single-module migration management
- `adr/ADR-0003-problem-details-only.md` — Unified error model
- `adr/ADR-0004-outbox-pattern.md` — Transactional outbox
- `adr/ADR-0005-archunit-boundaries.md` — Architecture boundaries enforcement
- `adr/ADR-0006-ports-and-adapters.md` — Ports/Adapters mapping

> New ADRs: use the template in `adr/` and link them here.

---

## 16) Known Tradeoffs

- Strong modularity imposes stricter layering; it pays off in maintainability but increases initial overhead
- Eventual consistency between contexts around outbox/Kafka is intentional—design consumers to handle out-of-order events
- Single migration module simplifies ops but requires coordination when multiple contexts evolve schemas

---

## 17) Glossary

- **Bounded Context:** A domain boundary with its own model and language
- **Port:** An interface representing an external capability (e.g., repository, gateway)
- **Adapter:** An implementation of a port targeting a specific technology (e.g., JPA, Kafka)
- **Outbox:** Pattern to publish events reliably from a transactional DB
- **Platform Layer:** Cross-cutting reusable code (web, security, persistence, observability)
- **Shared Kernel:** Set of domain primitives shared across contexts

---

## 18) References & Pointers

- **API Endpoint Map:** `API_ENDPOINTS.md` (if present) or `modules/contracts/http/openapi/*.yaml`
- **cURL Cookbook:** `CURL_COOKBOOK.md` (if present)
- **README:** project overview, quick start, and links
- **CONTRIBUTING:** workflow, testing, and PR process
- **CODE_OF_CONDUCT:** community expectations
- **LICENSE:** Apache-2.0
