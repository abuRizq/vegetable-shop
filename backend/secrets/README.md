# Secrets — SOPS-managed (age) 🔐

**Do not commit plaintext secrets.** This directory contains **Kubernetes Secret manifests encrypted with SOPS**.
Decryption happens locally (developer machine or CI) using your **age** private key. The repo keeps only the
**encrypted YAML** so it’s safe to share.

> The repo root includes `.sops.yaml` that defines encryption rules/recipients for files under `backend/secrets/*.yaml`.
> Ensure your identity is in the recipients list (age public key) to be able to decrypt.

---

## Quick start

### 1) Install SOPS + age
- macOS: `brew install sops age`
- Linux: use your distro packages or the GitHub releases.

### 2) Create an age key (once per user)
```bash
mkdir -p ~/.config/sops/age
age-keygen -o ~/.config/sops/age/keys.txt
# Show your public key to a maintainer to add to .sops.yaml
grep '^# public key:' ~/.config/sops/age/keys.txt
```

### 3) Add your public key to `.sops.yaml`
A maintainer must add your `age1...` to the recipients. Example:
```yaml
# .sops.yaml (example)
creation_rules:
  - path_regex: backend/secrets/.*\.yaml
    encrypted_regex: '^(data|stringData)$'
    age: ['age1examplepublickeyxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx']
```

### 4) Decrypt and apply to a cluster
**Never write decrypted files to disk in repos; use stdin piping instead.**
```bash
# Dev namespace example:
sops -d backend/secrets/dev-app.yaml | kubectl -n dev apply -f -
# Staging namespace example:
sops -d backend/secrets/staging-app.yaml | kubectl -n staging apply -f -
```

### 5) Editing an existing secret (in-place encryption)
```bash
sops backend/secrets/dev-app.yaml          # opens your $EDITOR; writes back encrypted
# or:
sops -d backend/secrets/dev-app.yaml > /tmp/dev-app.yaml.plain  # if you must (avoid committing this)
# ...
sops -e -i backend/secrets/dev-app.yaml
```

---

## What’s inside

These files create a **Kubernetes Secret** named `veggieshop-app-secrets` containing app configuration secrets expected by the service:
- `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`
- (optional) `jdbcUrl` — used in prod/staging to supply the full JDBC URL via a single key

They are referenced in Helm values (see `values.yaml` and `values-*.yaml`) via `envFrom.secrets` or `env.valueFrom`.

---

## CI/CD usage (no plaintext on disk)

Recommended deployment pattern:
```bash
# Render and apply without creating a decrypted file on disk:
sops -d backend/secrets/staging-app.yaml | kubectl apply -f -
```

If you package manifests (e.g., with Kustomize/Helm), keep secrets **external** and plug them during the CD stage; never check in decrypted variants.

---

## Alternative: External Secrets Operator (ESO)

For production, prefer **ESO** with a cloud secrets store (AWS Secrets Manager, GCP Secret Manager, Vault):
- Configure `templates/external-secret.yaml` + `values-prod.yaml`
- The ExternalSecret controller will materialize a Kubernetes Secret at runtime

---

## Incident hygiene

- **Rotate secrets** regularly and on departure of team members.
- **Re-key** SOPS recipients in `.sops.yaml` if an age key is compromised.
- Consider **short TTL tokens** or cloud IAM where possible instead of long-lived static secrets.

_Last updated: 2025-08-20T14:40:25Z_
