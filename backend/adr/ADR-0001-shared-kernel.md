---
id: ADR-0001
title: Shared Kernel for Cross-Context Domain Primitives
status: Accepted
date: 2025-08-20
deciders:
  - VeggieShop Maintainers
  - Platform & Architecture Leads
consulted:
  - Context Maintainers (auth, catalog, inventory, order, pricing, customer, checkout, vendor, media, review, notification, audit)
tags: [ddd, shared-kernel, domain, modularity, stability]
---

## 1. Context

VeggieShop is a modular, DDD-oriented monorepo composed of multiple bounded contexts. Several **domain primitives** (e.g., `Money`, `Weight`, `Dimensions`, `Address`, `GeoLocation`, `ContactInfo`), **enums** (`Currency`, `UnitOfMeasure`, `TemperatureZone`), and **validation utilities** (`Preconditions`, `Validators`) are **ubiquitous** and must behave consistently across contexts.

Historically, such types may be duplicated or diverge subtly (rounding, equality, nullability), leading to bugs and friction during integration and reporting. We require a **single source of truth** that is **stable**, **framework-agnostic**, and **easy to test**.

## 2. Decision

Create and maintain a **Shared Kernel** in module **`modules/domain-kernel`** that provides **immutable, framework-free** domain primitives and utilities used across contexts.

**Key properties**:

- **Immutability & Value Semantics**  
  Implement primitives as **Java 21 records** or final classes with explicit invariants.
- **Framework-Agnostic**  
  No direct dependencies on Spring, JPA/Hibernate, Jackson, or messaging frameworks.
- **Minimal, Stable API**  
  Small surface area with strong invariants; backwards compatible changes favored.
- **Consistency**  
  Single place for currency, measurement units, comparisons, and common validations.
- **Testable & Deterministic**  
  No time, IO, or environment coupling. Provide deterministic operations only.

## 3. Scope

Shared kernel includes (non-exhaustive):

- **Value Objects**: `Money`, `Weight`, `Dimensions`, `Address`, `GeoLocation`, `ContactInfo`
- **Enums**: `Currency`, `UnitOfMeasure`, `TemperatureZone`
- **Validation**: `Preconditions`, `Validators` (+ annotation-free checks)
- **Common Errors** (if any): lightweight exceptions for invariant violations

Excluded from the kernel:

- Persistence annotations (`@Entity`, `@Embeddable`) and ORM mappings
- Serialization frameworks (Jackson/Gson annotations)
- HTTP concerns, controllers, DTOs, mappers
- Business rules that are **context-specific** (keep them within the bounded context)

## 4. Rationale

- **Uniform behavior** across contexts minimizes duplication and drift.
- **Isolation from frameworks** avoids tight coupling and makes primitives reusable in multiple runtime scenarios.
- **Immutability** reduces bugs and aids thread safety.
- **Smaller surface area** simplifies review and makes stability feasible.
- **Easier testing** enables fast feedback and property-based tests.

## 5. Design & Usage Guidelines

### 5.1 Value Objects

- Prefer **records**:
  ```java
  public record Money(BigDecimal amount, Currency currency) {
      public Money {
          Validators.notNull(amount, "amount");
          Validators.notNull(currency, "currency");
      }
      public Money add(Money other) { ensureSameCurrency(other); return new Money(amount.add(other.amount), currency); }
      public Money multiply(BigDecimal factor, RoundingMode mode) { return new Money(amount.multiply(factor).setScale(2, mode), currency); }
      private void ensureSameCurrency(Money other) { Preconditions.check(currency.equals(other.currency), "Currency mismatch: %s vs %s", currency, other.currency); }
  }
  ```
- **No framework annotations** in kernel types. Handle persistence/JSON mapping in adapters (see §5.4).

### 5.2 Validation & Invariants

- Keep **constructor validation** strict; fail early with clear messages.
- Provide `Validators`/`Preconditions` methods only for domain invariants (no logging/metrics).
- Avoid implicit rounding or unit conversion that surprises callers; require explicit rounding modes/units.

### 5.3 Equality, Hashing, Ordering

- Equality must reflect domain identity of the value object (e.g., `Money` equals by amount & currency).
- Provide comparators when meaningful (`MoneyComparator.sameCurrency()`), avoid partial ordering surprises.

### 5.4 Mapping (Persistence & JSON)

- **JPA/Hibernate**: create **AttributeConverters** or **Embeddable mappers** per context/infrastructure:
  ```java
  @Converter(autoApply = false)
  public class MoneyAttributeConverter implements AttributeConverter<Money, String> {
      @Override public String convertToDatabaseColumn(Money money) { return money == null ? null : money.currency().getCode() + ":" + money.amount().toPlainString(); }
      @Override public Money convertToEntityAttribute(String db) {
          if (db == null) return null;
          var parts = db.split(":");
          return new Money(new BigDecimal(parts[1]), Currency.of(parts[0]));
      }
  }
  ```
- **JSON**: perform mapping in DTO mappers (MapStruct/manual). Do **not** add Jackson annotations in kernel types.
- Ensure **stable wire formats** at the edges (DTOs/events), not in the kernel.

### 5.5 Time & Units

- Keep **time-free** primitives (no implicit system clock). If needed, pass `Clock` from application layer.
- Units must be explicit (e.g., `UnitOfMeasure`) and validated.

### 5.6 Error Handling

- Throw unchecked domain exceptions on invariant violations (e.g., `DomainViolationException` in kernel or use `IllegalArgumentException` with clear messages). Map to HTTP Problem Details in platform/web layer.

## 6. Dependencies & Module Rules

- `domain-kernel` depends only on the **JDK** (and possibly tiny, vetted libs; default: none).
- **Must not depend** on `platform`, `messaging`, or any `context-*` module.
- It is safe for **platform** and **contexts** to depend on `domain-kernel`.
- Enforce via **ArchUnit** rules in `modules/testing` and CI.

## 7. Versioning & Stability

- Monorepo builds a single version, but treat the kernel as a **stability anchor**.
- Changes policy:
  - **Additive**: allowed with tests and docs.
  - **Behavior changes**: require a **Decision Log (DL-)** or new ADR and a migration plan.
  - **Deprecation**: mark API elements with Javadoc `@deprecated` + replacement; remove after N releases.
- Document semantic changes in `CHANGELOG` and module-level `README`.

## 8. Testing

- Kernel has isolated **unit tests** (no Spring/Testcontainers). Prefer:
  - Example-based tests for invariants and arithmetic.
  - Property-based tests for `Money`, `Weight`, etc. where helpful.
- Coverage should remain **high**; break builds below agreed thresholds (if Jacoco configured).

## 9. Alternatives Considered

1. **Per-context primitives** – rejected due to duplication and divergence risk.
2. **Include framework annotations** – rejected to preserve purity and portability.
3. **Place kernel in `platform`** – rejected; `platform` is cross-cutting infra, not pure domain.

## 10. Consequences

**Positive**
- Consistent domain semantics and fewer integration defects.
- Lower cognitive load for new contributors.
- Easier reuse in tests and prototypes.

**Negative / Risks**
- Centralization can cause contention for changes; mitigated with **review path** and **deprecation policy**.
- Mapping overhead (JPA/JSON) moves to adapters; mitigated with reusable converters/mappers.

## 11. Implementation Plan

- The module **already exists** at `modules/domain-kernel` with initial types:
  - `domain/model`: `Address`, `Money`, `Weight`, `Dimensions`, `ContactInfo`, `GeoLocation`
  - `domain/enums`: `Currency`, `UnitOfMeasure`, `TemperatureZone`
  - `validation`: `Preconditions`, `Validators`
- Actions:
  1. **Audit** existing contexts for duplicate types; replace with kernel types.
  2. Provide **AttributeConverters** in infrastructure where needed.
  3. Add **ArchUnit rules** to prevent framework annotations in kernel and to restrict dependencies.
  4. Document mappers in `guides/conventions.md` and examples in `testing` helpers.
  5. Add **decision logs** for major semantic changes (e.g., `Money` rounding policy).

## 12. Enforcement (ArchUnit Example)

```java
@AnalyzeClasses(packages = "com.veggieshop")
public class SharedKernelRules {
  @ArchTest
  static final ArchRule kernel_should_be_framework_free =
      noClasses().that().resideInAPackage("..shared.domain..")
        .should().dependOnClassesThat().resideInAnyPackage(
          "org.springframework..", "jakarta.persistence..", "com.fasterxml.jackson..");

  @ArchTest
  static final ArchRule kernel_should_not_depend_on_contexts =
      noClasses().that().resideInAPackage("..shared.domain..")
        .should().dependOnClassesThat().resideInAnyPackage("..auth..", "..catalog..", "..order..");
}
```
> Adapt package names to your actual kernel package (`com.veggieshop.shared.*`). Integrate with `modules/testing` ArchRules.

## 13. Migration & Rollback

- Migration is safe and incremental; replace types in each context/module with kernel equivalents.
- If a kernel change proves harmful, **rollback** by reverting to a previous commit/version and open a DL to revise the design.

## 14. Open Questions

- Do we standardize **rounding modes** for `Money` operations globally, or keep them caller-defined?
- Should we add **unit conversion helpers** (e.g., g↔kg) or keep conversions in application services?

## 15. References

- DDD: Evans, *Domain-Driven Design*
- Clean Architecture: Martin
- ADRs in this repo: `adr/ADR-0002-flyway-single-module.md`, `adr/ADR-0004-outbox-pattern.md`, `adr/ADR-0006-ports-and-adapters.md`
- Guides: `guides/conventions.md`, `guides/local-dev.md`
