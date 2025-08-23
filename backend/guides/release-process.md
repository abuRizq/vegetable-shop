# Release Process

This document describes how we cut, verify, and ship releases for the **VeggieShop Backend** (API image + Helm chart).

---

## Versioning
- **Semantic Versioning**: `MAJOR.MINOR.PATCH` (e.g., `1.4.2`).
- Breaking changes bump **MAJOR**; additive changes bump **MINOR**; fixes bump **PATCH**.
- Changelog is generated from Conventional Commits / PR titles (optional).

---

## Branching Strategy
- `main`: always releasable.
- Feature branches: `feat/*`, fixes: `fix/*` (or any naming your team prefers).
- Prefer small PRs with green CI before merging.

---

## CI Overview
Workflows in `.github/workflows/`:
- `build.yml`: compile, test, package, (optionally) publish snapshot images.
- `pr-checks.yml`: fast checks for pull requests.
- `release.yml`: cut a release on tag, build/push Docker image, package Helm chart.
- `security.yml`: dependency scans (SCA/SAST), configurable.

> Ensure registry credentials (e.g., GHCR, Docker Hub) are set as **secrets** in the repo.

---

## Cut a Release

1. **Update versions** (if needed) in Maven POMs and Helm `Chart.yaml`.
2. **Create a tag** on `main`:
   ```bash
   git pull origin main
   ./mvnw -q help:evaluate -Dexpression=project.version -DforceStdout
   git tag -a vX.Y.Z -m "Release vX.Y.Z"
   git push origin vX.Y.Z
   ```
3. **CI runs `release.yml`**:
   - Build & test
   - Package app Jar
   - Build Docker image `REGISTRY/veggieshop:X.Y.Z`
   - Push image
   - Package Helm chart `config/helm/veggie-shop` with version X.Y.Z
   - (Optional) Upload SBOM via `scripts/sbom-generate.sh`

4. **Artifacts**:
   - Docker image available in registry
   - Helm chart artifact (packaged `.tgz`) in release assets or internal chart repo

---

## Deploy (Kubernetes)

### Pre-Deploy: Migrations
Run DB migrations first (see ADR‑0002):
- Approach A: app runs Flyway at startup (non-prod).
- Approach B (recommended for prod): **migrator job** runs before rollout.

### Helm Install/Upgrade
```bash
helm upgrade -i veggie-shop config/helm/veggie-shop \
  -n <namespace> \
  -f config/helm/veggie-shop/values-prod.yaml \
  --set image.repository=<REGISTRY>/veggieshop \
  --set image.tag=X.Y.Z
```

- Health probes must pass; deployment strategy is rolling update by default.
- If needed: canary with a second release name or TrafficSplit at ingress.

---

## Secrets Management
- Store runtime secrets in external secret store (e.g., External Secrets + SOPS-managed encrypted files).
- Never commit plain-text secrets. `.sops.yaml` defines key sources.
- For local dev, use `.env` with non-sensitive defaults.

---

## Rollback
- Roll back to previous chart version:
  ```bash
  helm rollback veggie-shop <REVISION>
  ```
- DB rollbacks are **not guaranteed**. Prefer roll-forward with corrective migration (see ADR‑0002).

---

## Post-Release Checklist
- Smoke tests: login, browse products, cart, checkout (authz+payment capture in sandbox).
- Monitor error rate, latency, and outbox relay lag.
- Verify consumer idempotency by checking the inbox/outbox states.

---

## Hotfixes
- Branch from `main`, apply fix, bump PATCH, tag `vX.Y.(Z+1)`, merge back.
- Keep releases small; avoid bundling unrelated changes.

---

## Decommissioning / Deprecation
- Mark deprecated endpoints in OpenAPI and docs; warn via response headers when feasible.
- Remove after one MINOR cycle or as agreed with API consumers.

---

## Appendices

### Make Targets (useful)
- `make docker-build IMAGE_TAG=X.Y.Z REGISTRY=ghcr.io/your-org`
- `make docker-push IMAGE_TAG=X.Y.Z REGISTRY=ghcr.io/your-org`
- `make helm-template VALUES=config/helm/veggie-shop/values-prod.yaml`
- `make helm-install VALUES=config/helm/veggie-shop/values-prod.yaml`

### Observability
- Ensure metrics/traces/logs are wired (OTEL exporter, Prometheus scrape).
- Alerting for: 5xx spikes, Kafka publish failures, migration errors.
