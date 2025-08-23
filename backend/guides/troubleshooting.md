# Troubleshooting

This guide lists common issues and quick fixes for the **VeggieShop Backend**.

---

## Build & Maven

### `mvnw: Permission denied` (Unix)
```bash
chmod +x mvnw
```

### Missing toolchain / wrong Java version
Ensure Java 21 is selected (jEnv/SDKMAN) or set `JAVA_HOME` accordingly.

### Dependency resolution failures
- Check network / corporate proxy.
- Run with more logs: `./mvnw -X -e clean verify`.

---

## Docker & Compose

### Containers don't start / port conflicts
- Stop old runs: `make down`.
- Free ports: 8080 (API), 5432 (Postgres), 9092 (Kafka), 9000 (MinIO/S3, if used).
- Check logs: `make logs`.

### Postgres keeps restarting
- Data dir corruption: remove local volume (dev only).  
  **Warning:** data loss in dev.
  ```bash
  docker compose -f config/local/docker-compose.yml down -v
  make up
  ```

### Kafka "connection refused"
- Ensure `KAFKA_ADVERTISED_LISTENERS` matches host/ports in compose.
- Verify env `KAFKA_BOOTSTRAP_SERVERS` in `.env`.

---

## Application Startup

### Flyway migration failure
- Check current head in `modules/migrations/.../db/migration`.
- Inspect `flyway_schema_history`:
  ```bash
  docker compose -f config/local/docker-compose.yml exec db psql -U $DB_USERNAME -d $DB_NAME -c "select * from flyway_schema_history order by installed_rank desc limit 5"
  ```
- Never edit applied migrations; create a new `V*` migration.

### `BeanCreationException` / missing config
- Verify `.env` values are loaded (Make prints variables with `make print-vars`).
- Confirm `application-local.yml` keys match env variable names.

### CORS / 401 / 403 issues
- Check JWT issuer/audience config.
- For local, allow `http://localhost:3000` if using a frontend.

---

## Tests

### Testcontainers can't pull images
- Ensure Docker is running and you have internet access.
- Pre-pull images:
  ```bash
  docker pull postgres:14
  docker pull confluentinc/cp-kafka:7.5.1
  ```

### Flaky integration tests (Kafka/DB timing)
- Increase timeouts; ensure topics are created before consumers start.
- Use `await().untilAsserted(...)` in tests for eventually-consistent flows.

---

## Messaging

### Events not published
- Check **outbox** table for `status=PENDING/FAILED`.
- Relay logs: look for retries/backoff information.
- Verify Kafka broker address and credentials.

### Duplicate messages
- Ensure consumers are **idempotent** (use inbox/dedup_key).

---

## SOPS & Secrets

### `sops: cannot decrypt` 
- Verify `.sops.yaml` key sources (age/GCP KMS/AWS KMS).
- Ensure you have the private key; run `sops -d secrets/dev-app.yaml` as a test.

### Accidentally committed a secret
- Rotate immediately; replace with SOPS-encrypted version; purge from history if necessary.

---

## Performance & Memory

### High memory usage locally
- Lower JVM Xms/Xmx in `application-local.yml`.
- Reduce Docker resource limits or stop extra containers.

### Slow queries
- Use `EXPLAIN ANALYZE`; add/adjust indexes via a new Flyway migration.
- Cache hot reads via Spring Cache when appropriate.

---

## Observability

### Missing trace_id in logs
- Ensure `TraceIdFilter` is registered and logback pattern contains `%X{trace_id}`.

### No metrics in Prometheus
- Verify actuator/metrics endpoints are exposed and the scrape config is correct.

---

## Support Checklist
Before asking for help, include:
- Commit/branch, command you ran, and full logs.
- Output of `make print-vars` (redact secrets).
- `docker compose ps` status.
