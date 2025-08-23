---
id: ADR-0003
title: Problem Details (RFC 7807) as the Only Error Envelope
status: Accepted
date: 2025-08-20
deciders:
  - VeggieShop Maintainers
  - Platform & Architecture Leads
consulted:
  - Context Maintainers (auth, catalog, inventory, order, pricing, customer, checkout, vendor, media, review, notification, audit)
tags: [api, error-handling, rfc-7807, http, consistency, observability]
---

## 1. Context

Historically, APIs drift toward bespoke error formats per team or per endpoint, which:
- increases client complexity (conditionals per service),
- hinders observability (no consistent error code / trace ID),
- and complicates documentation and contract tests.

VeggieShop exposes multiple bounded contexts behind a single Spring Boot app. We need a **uniform, well-specified error model** across all HTTP endpoints. The industry-standard **Problem Details for HTTP APIs (RFC 7807)** is a suitable baseline and interoperable across languages and tooling.

## 2. Decision

Adopt **RFC 7807 Problem Details** as the **only** error envelope for all HTTP APIs:

- Content type: `application/problem+json` (primary), `application/problem+xml` not supported.
- All non-2xx responses must be a valid **Problem Details** document, including validation failures.
- We provide a single **platform** implementation in `modules/platform/web/error/*` wired through global advice/filters.
- Extensions are allowed and standardized (see §5.4).

## 3. Scope

**In-scope**
- All controllers in `modules/contexts/**/api/http/controller/*`.
- Errors from filters and exception handlers (security, rate limiting, etc.).
- Validation errors (Bean Validation) and domain/application exceptions.

**Out-of-scope**
- Success envelopes (no wrapping of normal responses).
- Non-HTTP channels (Kafka events) – separate delivery & error semantics.
- Internal logs/metrics format (mapped but not exposed over HTTP).

## 4. Rationale

- **Interoperability:** RFC 7807 is widely supported and idiomatic for RESTful APIs.
- **Consistency:** clients can parse one shape across contexts.
- **Observability & Support:** uniform fields enable correlation (`trace_id`) and analytics by `error_code`.
- **Documentation:** easy to describe in OpenAPI and test with contract tests.

## 5. Design

### 5.1 RFC 7807 Core Fields
We implement and return:
- `type` (string, URI) — a stable identifier of the error category.
- `title` (string) — human-readable summary, identical for same `type`.
- `status` (number) — HTTP status code.
- `detail` (string) — human-readable detail for this occurrence.
- `instance` (string, URI) — URI reference identifying the specific occurrence (optional).

### 5.2 HTTP Status Mapping (examples)
- 400 — malformed request / failed precondition
- 401 — authentication required / failed
- 403 — authorization failed
- 404 — resource not found
- 409 — conflict (e.g., versioning, uniqueness)
- 410 — gone (tombstoned)
- 415 — unsupported media type
- 422 — **validation error** (see §5.3)
- 429 — too many requests
- 500 — unexpected server error
- 503 — dependency/service unavailable

### 5.3 Validation Errors (422)
Use status **422 Unprocessable Entity** with extensions:
```json
{
  "type": "https://docs.veggieshop.example/errors/validation",
  "title": "Validation failed",
  "status": 422,
  "detail": "Request contains invalid fields",
  "trace_id": "2f0a1c...",
  "error_code": "VALIDATION_FAILED",
  "field_errors": [
    { "field": "email", "message": "must be a well-formed email address" },
    { "field": "password", "message": "length must be between 12 and 72" }
  ]
}
```

### 5.4 Standard Extensions
We extend RFC 7807 with **stable, documented** members:
- `error_code` — machine-stable string (SCREAMING_SNAKE_CASE) for analytics/alerts.
- `trace_id` — correlation ID injected by `TraceIdFilter` (also in logs/MDC).
- `field_errors` — array for 422 validation details.
- `timestamp` — ISO-8601 UTC time of emission.
- (Optional) `parameters` — sanitized input snippets (never secrets or PII).

> **PII/Sensitive data MUST NOT appear** in `detail`, `parameters`, or `field_errors`.

### 5.5 Type URIs & Catalog
- Use stable URIs like `https://docs.veggieshop.example/errors/<kebab-code>`.
- Maintain a catalog in docs: `docs/errors/README.md` (or in the platform module README).
- Each `type` maps 1:1 to an `error_code` and canonical `title`.

### 5.6 Spring Boot Integration
We centralize wiring in **platform**:
- `modules/platform/web/error/ProblemDetails.java`, `ProblemDetails422.java`
- `ErrorResponseFactory`, `ExceptionMappingService`, `DefaultExceptionMappingService`
- `GlobalResponseAdvice` to ensure correct content type & headers
- Controllers **throw exceptions** or use `ExceptionMappingService` — do **not** craft ad-hoc responses.

**Controller advice** maps:
- `MethodArgumentNotValidException` → 422 + `field_errors`
- `ConstraintViolationException` → 422 + `field_errors`
- `AccessDeniedException` → 403
- `AuthenticationException` → 401
- `EntityNotFoundException` / `NoSuchElementException` → 404
- `IllegalStateException`/`IllegalArgumentException` (domain guard) → 400
- Fallback `Exception` → 500

### 5.7 Content Negotiation & Headers
- Always set `Content-Type: application/problem+json; charset=utf-8`.
- Preserve `WWW-Authenticate` for 401 and rate-limit headers for 429 (e.g., `Retry-After`).

### 5.8 OpenAPI & Contracts
- Define shared schemas/components for Problem Details & Validation Problem in `modules/contracts/http/openapi/*` and reference via `$ref` in error responses.
- Provide `ApiResponses422` helpers (already present) to keep specs DRY.

### 5.9 Localization
- `title` is **not** localized; it is a stable label for the error category.
- `detail` **may** be localized by client needs; server emits English by default. If i18n is needed, include an `i18n_key` extension.

### 5.10 Logging & Telemetry
- Log at appropriate level with `trace_id`, `error_code`, `status`.
- Emit metrics counters per `(status, error_code)` and histograms for time-to-failure.
- Never log secrets; scrub structured logs.

## 6. Examples

### 6.1 404 Not Found
```json
{
  "type": "https://docs.veggieshop.example/errors/not-found",
  "title": "Resource not found",
  "status": 404,
  "detail": "Product with id PROD_123 not found",
  "error_code": "NOT_FOUND",
  "trace_id": "3a1b..."
}
```

### 6.2 409 Conflict
```json
{
  "type": "https://docs.veggieshop.example/errors/conflict",
  "title": "Conflict",
  "status": 409,
  "detail": "Email already registered",
  "error_code": "EMAIL_ALREADY_EXISTS",
  "trace_id": "d090..."
}
```

### 6.3 500 Internal Server Error
```json
{
  "type": "https://docs.veggieshop.example/errors/internal",
  "title": "Internal server error",
  "status": 500,
  "detail": "An unexpected error occurred. Please try again later.",
  "error_code": "INTERNAL_SERVER_ERROR",
  "trace_id": "7f3d..."
}
```

## 7. Enforcement

- **Do not** return ad-hoc error shapes from controllers.
- Lint OpenAPI files to ensure error responses reference the shared Problem schemas.
- Add **ArchUnit** rules in `modules/testing` to prevent controllers from constructing raw `ResponseEntity` with custom error bodies.
- Contract tests in `modules/testing` (e.g., `ProblemDetailsContract`) verify the envelope and headers.

## 8. Versioning & Compatibility

- Adding new `error_code`s is **backward compatible**.
- Changing `type` URIs or `title` requires deprecation and transitional support.
- Extensions **must** be additive; clients should ignore unknown members per RFC 7807.

## 9. Alternatives Considered

1. **Custom error envelope**: rejected; reinvents the wheel and complicates interoperability.
2. **Multiple envelopes (per context)**: rejected; causes client branching and weak observability.
3. **HTTP status-only with plain text**: rejected; too limited for programmatic handling.

## 10. Consequences

**Positive**
- Single error contract across all endpoints.
- Easier client SDK generation and uniform docs.
- Better SRE workflows via stable `error_code` and `trace_id`.

**Negative / Risks**
- Migration effort to align all controllers and existing clients.
- Requires discipline to keep the `type`/`title`/`error_code` catalog consistent.

## 11. Implementation Plan

1. Finalize `ErrorResponseFactory` mappings in `modules/platform/web/error/*`.
2. Add controller advice for common framework exceptions.
3. Update OpenAPI components to reference shared Problem schemas; generate examples.
4. Add tests: unit + contract (`ProblemDetailsContract`) + system tests for critical flows.
5. Publish the error catalog doc (`docs/errors/README.md`) and link from `README.md`.
6. Migrate old endpoints (if any) and mark legacy shapes as deprecated until removed.

## 12. Rollback Strategy

- Keep a feature flag to switch to the legacy envelope during migration (temporary, default OFF).
- If critical client breaks occur, temporarily enable legacy envelope **only** for the affected paths while fixing clients. Record a Decision Log for the exception.

## 13. Open Questions

- Do we need an `i18n_key` extension now, or wait for client demand?
- Should `instance` be a clickable log search URL (e.g., to Kibana/Grafana) in production?

## 14. References

- RFC 7807 — *Problem Details for HTTP APIs*
- Platform code: `modules/platform/web/error/*`, `modules/platform/web/GlobalResponseAdvice.java`
- Contracts: `modules/contracts/http/openapi/*` (see `ApiResponses422`)
- Tests: `modules/testing/src/test/java/com/veggieshop/testing/ProblemDetailsContract.java`
