# Local Development Guide

This guide helps you run the **VeggieShop Backend** locally with minimal friction.

---

## Prerequisites
- **Java 21** (set via `.java-version` if using SDKMAN or jEnv).
- **Docker** & **Docker Compose**.
- **Git**, **Make**, and an editor with Lombok support if you use Lombok.
- Optional: **PlantUML** (or Dockerized PlantUML) to render diagrams.

> All build commands should use the Maven wrapper: `./mvnw`

---

## 1) Bootstrap
```bash
# Clone and enter the repo (assumed path layout)
cd vegetable-shop/backend

# Create ..env from example
make .env

# Start local infra (Postgres, Kafka, Nginx, etc.)
make up

# Build the project
make build

# Run the API with the local profile
make run PROFILE=local
```

Visit **http://localhost:8080** (proxied via Nginx if enabled).

---

## 2) Configuration
- Local configs live under `apps/veggieshop-service/src/main/resources/application-local.yml` and `.env`.
- Common env vars: `DB_*`, `KAFKA_*`, `SECURITY_*`, `STORAGE_*` (see `.env.example`).

To switch profiles:
```bash
make run PROFILE=prod   # not recommended locally unless testing prod config
```

---

## 3) Database & Migrations
- Migrations are in **`modules/migrations`**.
- To run migrations quickly:
```bash
make migrate PROFILE=local
```
- Seed data:
```bash
make seed
```

> Use **Flyway** only via the dedicated module; do not add per-module migrations.

---

## 4) Tests
```bash
make test      # unit tests
make it        # integration tests (if configured)
make system    # system tests with Testcontainers
make verify    # full pipeline (build + all checks)
```

- Testcontainers will pull Docker images on first run (Postgres, Kafka). Ensure Docker is running.

---

## 5) Contracts & OpenAPI
- Specs in `modules/contracts/http/openapi/*`.
- Keep DTOs & controllers aligned; use shared components; use `ApiResponses422` helpers.
- For event schemas, see `modules/contracts/events/schema/*` and the `contracts` make target.

---

## 6) Diagrams
Render PlantUML diagrams:
```bash
make uml
# Generated under architecture/out
```

---

## 7) Troubleshooting Quick Wins
- Ports busy? stop containers: `make down` or free port 8080/5432/9092.
- Migration failing? check `modules/migrations` and `flyway_schema_history` in DB.
- Kafka connection refused? ensure broker is up in Compose and `KAFKA_BOOTSTRAP_SERVERS` is set.
- See full guide: `guides/troubleshooting.md`.

---

## 8) Useful Make Targets
- `make up / down / logs`
- `make run PROFILE=local`
- `make docker-build IMAGE_TAG=local`
- `make helm-template VALUES=config/helm/veggie-shop/values-local.yaml`

---

## 9) Security & Secrets
- Never commit secrets. Use **SOPS** + `.sops.yaml` for encrypted files under `secrets/`.
- Local dev may use `.env` with **non-sensitive** defaults.

---

## 10) IDE Tips
- Enable annotation processing (MapStruct/Lombok if present).
- Use **Run/Debug** Spring Boot run configuration pointing at `VegetableShopApplication` with `--spring.profiles.active=local`.
