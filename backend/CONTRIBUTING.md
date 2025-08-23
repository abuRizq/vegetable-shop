# Contributing to VeggieShop Backend

Thank you for your interest in contributing! This repository is a **multi-module Spring Boot monorepo** that powers the VeggieShop backend. Contributions are welcome from everyone. This guide explains how to set up your environment, propose changes, and help maintain project quality.

> By participating, you agree to abide by our **[Code of Conduct](./CODE_OF_CONDUCT.md)**.

---

## 1. Repository Layout (Quick Tour)

```
backend/
├─ apps/veggieshop-service/         # Executable Spring Boot app (API)
├─ modules/                          # Multi-module domain + platform
│  ├─ domain-kernel/                 # Shared domain primitives (value objects, validation)
│  ├─ platform-autoconfigure/        # Auto-config for platform features
│  ├─ platform-starter/              # Starter POM for platform consumers
│  ├─ platform/                      # Web, security, persistence, observability utilities
│  ├─ messaging/                     # Outbox pattern + Kafka integration
│  ├─ migrations/                    # Flyway SQL migrations
│  ├─ contracts/                     # Event schemas + OpenAPI specs (codegen)
│  ├─ testing/                       # Test utilities + base test classes
│  ├─ system-tests/                  # Testcontainers-based system tests
│  └─ contexts/                      # Bounded contexts (auth, catalog, inventory, ...)
├─ config/helm/veggie-shop/          # Helm chart for K8s deployment
├─ config/local/docker-compose.yml   # Local infra (DB/Kafka/Nginx/etc.)
├─ docker/                           # Dockerfiles for app/infra
├─ scripts/                          # Dev/CI helper scripts
├─ secrets/                          # SOPS-encrypted K8s secrets (placeholders)
└─ guides/, docs/, adr/              # Documentation + ADRs + decision logs
```

---

## 2. Prerequisites

- **Java 21** (see `.java-version`)
- **Maven Wrapper** (`./mvnw`) – no need to install Maven globally
- **Docker** + **Docker Compose** (for local infra + Testcontainers compatibility)
- (Optional) **Node/Yarn** if you’re working with any API demo clients
- (Optional) **SOPS** for secret management, **Helm** for chart validation

---

## 3. Getting Started (Local Dev)

1) Clone and bootstrap:
```bash
git clone <your-fork-url>
cd vegetable-shop/backend
cp ..env.example ..env
```

2) Start local infra (Postgres, Kafka, etc.):
```bash
docker compose -f config/local/docker-compose.yml up -d
```

3) Build & unit tests:
```bash
./mvnw -s ci/maven-settings.xml clean verify
# or use helper scripts
./scripts/lint.sh && ./scripts/test.sh
```

4) Run the application (local profile):
```bash
./mvnw -pl apps/veggieshop-service -am spring-boot:run -Dspring-boot.run.profiles=local
```

5) System tests (Testcontainers):
```bash
./mvnw -pl modules/system-tests test
# or
./scripts/it.sh
```

> If you modify **contracts** (OpenAPI or event schemas), run codegen:
```bash
bash modules/contracts/events/codegen.sh
```

---

## 4. Issues, Proposals & Help Wanted

- **Search first** to avoid duplicates.
- Use labels: `bug`, `feature`, `enhancement`, `good first issue`, `help wanted`.
- For design changes, reference **ADRs** in `adr/` or propose a new ADR.
- For questions, open a **Discussion** if enabled, otherwise an issue.

**Bug report template (minimum):**
- Steps to reproduce
- Expected vs. actual behavior
- Logs / stacktraces
- Environment (OS, JDK, DB version)
- A minimal repro if possible

---

## 5. Branching & Workflow

- We follow a lightweight GitHub Flow:
  - **main**: protected, always deployable.
  - feature branches: `feature/<short-desc>`
  - fix branches: `fix/<short-desc>`
  - chore/refactor branches: `chore/<short-desc>` or `refactor/<short-desc>`

- Keep PRs **small, focused, and incremental**.
- Rebase frequently; avoid long-lived branches drifting from `main`.

---

## 6. Commit Messages (Conventional Commits)

Use **Conventional Commits** to improve readability and changelog generation:

```
<type>(optional scope): <short summary>

<body - optional>

<footer - optional (BREAKING CHANGE, closes #123, etc.)>
```

Common types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`.  
Examples:
```
feat(catalog): add product tagging API
fix(inventory): adjust stock reservation rounding
chore: bump spring-boot to 3.5.3
```

---

## 7. Coding Standards

- **Java 21** features are welcome (records, pattern matching, etc.) where appropriate.
- Follow existing package/module boundaries (no cross-context leakage).
- Prefer **constructor injection**, avoid field injection.
- Avoid static state; keep classes testable and deterministic.
- Nullability: Prefer `Optional` and validation in service boundaries.
- DTOs and Mappers: follow patterns under `modules/*/api/http/dto` and mapper templates.

**Formatting & Linting**
- If configured, run formatters/linters (Spotless/Checkstyle/PMD). Otherwise, keep consistent style.
- Run `./scripts/lint.sh` before committing.

**Error Handling**
- Use platform error types under `modules/platform/web/error/*` and **Problem Details** model.

**Security**
- Follow `modules/platform/security/*`. No secrets in code; use `.env`/External Secrets.
- Validate input at the edges; use strong password policy and rate limiting.

---

## 8. Testing Policy

- **Unit tests** for new logic **required**.
- **Integration tests** for repository/adapters and REST controllers where relevant.
- **Contract tests** when touching event schemas or OpenAPI endpoints.
- **System tests** for end-to-end happy paths (Testcontainers).
- Keep tests deterministic and fast; avoid external network calls.

Helpful commands:
```bash
./scripts/test.sh
./scripts/it.sh
./mvnw -pl modules/system-tests test
```

Coverage (if Jacoco configured): maintain or improve module coverage.

---

## 9. Database & Migrations (Flyway)

- Put SQL migrations in `modules/migrations/src/main/resources/db/migration` with proper versioning (`Vx__desc.sql`).
- **Never** edit an applied migration; create a new one.
- For seed/demo data, use `tools/db/seeds/*.sql` (optional, not in Flyway history).
- Coordinate changes across contexts to avoid breaking cross-module contracts.

---

## 10. Messaging & Outbox

- Use the **outbox pattern** provided in `modules/messaging/outbox`.
- Publish domain events via the provided services; avoid direct Kafka usage from domain services.
- Update **event schemas** under `modules/contracts/events/schema/*` and regenerate code as needed.

---

## 11. API Guidelines

- REST endpoints live under `.../api/http` per context.
- Follow resource-oriented design, consistent HTTP status codes, and Problem Details on errors.
- Keep DTOs stable; introduce new versions or fields with backward compatibility in mind.
- Document changes in OpenAPI specs under `modules/contracts/http/openapi/*`.

---

## 12. Secrets & Configuration

- Do **not** commit real secrets. Use `.env` (local only) and **SOPS** for K8s secrets.
- See `backend.env.example` and `.sops.yaml` for best practices.
- Helm values: non-secret config in `values-*.yaml`; secrets via External Secrets or sealed secrets.

---

## 13. CI/CD

- GitHub Actions in `.github/workflows/*`:
  - `build.yml` – compile, test, (optionally) sbom
  - `pr-checks.yml` – PR validations (lint, tests, vulnerability scans)
  - `release.yml` – versioning & publishing (if configured)
  - `security.yml` – dependency scanning / code scanning

- Keep builds green; do not merge red pipelines.
- Add/adjust steps via scripts in `scripts/` where possible to keep workflows tidy.

---

## 14. Performance & Observability

- Prefer efficient queries; use indexes and projections where appropriate.
- Use provided **observability** utilities (`TraceIdFilter`, MDC executors, etc.).
- Expose metrics through Actuator/Prometheus endpoints (`/actuator/prometheus`).

---

## 15. Pull Request Checklist

Before requesting review:

- [ ] Linked issue(s) and ADRs if design-impacting.
- [ ] Added/updated unit tests and integration tests.
- [ ] Updated OpenAPI/events and re-ran codegen if contracts changed.
- [ ] Database migrations created (if schema changed).
- [ ] Ran `./mvnw clean verify` locally and fixed lints.
- [ ] Updated docs (`guides/`, `README.md`, `CHANGELOG` if applicable).

**Review expectations:**
- 1–2 maintainer approvals required (protected branch rules may vary).
- Be responsive to review comments and keep commits focused.

---

## 16. Security Policy

If you think you have found a security issue, **do not open a public issue**. Email us at **security@veggieshop.example** with details and a repro. We’ll acknowledge within **3 business days**.

---

## 17. License

By contributing, you agree that your contributions will be licensed under the **Apache License, Version 2.0**. See [`LICENSE`](./LICENSE).

---

## 18. Recognition

We’re grateful for your time and expertise. ⭐ Consider adding yourself to contributors once your first PR is merged!
