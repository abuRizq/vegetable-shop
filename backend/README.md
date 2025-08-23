# VeggieShop Backend

![Build](https://img.shields.io/github/actions/workflow/status/your-org/vegetable-shop/build.yml?branch=main) ![Tests](https://img.shields.io/github/actions/workflow/status/your-org/vegetable-shop/pr-checks.yml?label=tests) ![Coverage](https://img.shields.io/codecov/c/github/your-org/vegetable-shop?token=YOUR_TOKEN) ![License](https://img.shields.io/badge/license-Apache--2.0-blue) ![Java](https://img.shields.io/badge/Java-21-informational) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen) ![Docker](https://img.shields.io/badge/Docker-ready-blue) ![Helm](https://img.shields.io/badge/Helm-chart-blue) ![SOPS](https://img.shields.io/badge/SOPS-encrypted%20secrets-8A2BE2)

**VeggieShop** is a modular, production-grade **Java 21 + Spring Boot** backend for an e-commerce domain, structured as a multi-module monorepo. It implements clean architecture with domain-driven design (DDD), ports & adapters, messaging via outbox + Kafka, and first-class support for containerization and Kubernetes.

> **Status:** Active development • **License:** Apache 2.0

## Quick Links
- **API Endpoints Map:** See [`API_ENDPOINTS.md`](./API_ENDPOINTS.md)
- **cURL Cookbook:** See [`CURL_COOKBOOK.md`](./CURL_COOKBOOK.md)
- **Helm Chart:** `config/helm/veggie-shop`
- **Migrations:** `modules/migrations/src/main/resources/db/migration`
- **Contracts (OpenAPI):** `modules/contracts/http/openapi/*.yaml`
- **Events (Schemas):** `modules/contracts/events/schema/**`

> Replace `your-org/vegetable-shop` and Codecov token in badge URLs with your actual org/repo.

---

## Architecture

- **DDD / Bounded Contexts:** `contexts/*` (auth, catalog, inventory, order, pricing, customer, checkout, vendor, media, review, notification, audit).
- **Shared Kernel:** `modules/domain-kernel` (value objects, validation).
- **Platform Layer:** `modules/platform*` (web errors, filters, security, persistence, config, observability).
- **Messaging:** `modules/messaging` (outbox pattern + Kafka adapter).
- **Migrations:** `modules/migrations` (Flyway SQL).
- **Executable App:** `apps/veggieshop-service` (Spring Boot application).

High-level flow:

```
Client → HTTP (Controller) → Application Service → Domain Model
      ↘  Events → Outbox → Kafka → Other services/consumers
```

---

## Quick Start (Local)

1. **Env & Infra**
   ```bash
   cd vegetable-shop/backend
   cp ..env.example ..env
   docker compose -f config/local/docker-compose.yml up -d
   ```

2. **Build & Test**
   ```bash
   ./mvnw -s ci/maven-settings.xml clean verify
   ./scripts/lint.sh && ./scripts/test.sh   # optional helpers
   ```

3. **Run**
   ```bash
   ./mvnw -pl apps/veggieshop-service -am spring-boot:run -Dspring-boot.run.profiles=local
   ```

Health: `http://localhost:8081/actuator/health` • App: `http://localhost:8080`

---

## Configuration & Secrets

- Local env via `.env` (see `backend.env.example`).
- **Never commit real secrets.** Use **SOPS** with `.sops.yaml` and External Secrets in K8s.
- Helm values (non-secrets): `values-*.yaml`; secrets via External Secrets / SOPS-encrypted files.

---

## Database & Migrations

Flyway migrations: `modules/migrations/src/main/resources/db/migration/` (`V1__*.sql`, `V2__*.sql`, ...).  
Use migrator container or auto-run on app startup (if enabled). Do not edit applied migrations—create a new one.

---

## Contracts (OpenAPI & Events)

- **HTTP APIs:** `modules/contracts/http/openapi/*.yaml`
- **Domain Events:** `modules/contracts/events/schema/**`
- **Codegen:** `bash modules/contracts/events/codegen.sh`

Swagger UI (if enabled): `/swagger-ui.html`

---

## Testing

- Unit: `./mvnw test` • Integration: `./scripts/it.sh` • System: `./mvnw -pl modules/system-tests test`
- Keep tests deterministic; use Testcontainers for integration/system tests.

---

## Observability & Security

- Actuator endpoints (`/actuator/*`), Prometheus (`/actuator/prometheus`), OTEL (`OTEL_*` env).
- JWT auth, password policies, and rate limiting. Report security issues privately: `security@veggieshop.example`.

---

## Contributing

See **[CONTRIBUTING.md](./CONTRIBUTING.md)** and **[CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md)**.  
By contributing, you agree to the **Apache-2.0** license terms (**[LICENSE](./LICENSE)**).

---

## FAQ & Troubleshooting

FAQ and tips live in this README and under `guides/`. For common issues see `guides/troubleshooting.md`.
