---
id: ADR-0005
title: Enforce Architecture Boundaries via ArchUnit
status: Accepted
date: 2025-08-20
deciders:
  - VeggieShop Maintainers
  - Platform & Architecture Leads
consulted:
  - Context Maintainers (auth, catalog, inventory, order, pricing, customer, checkout, vendor, media, review, notification, audit)
tags: [archunit, architecture, testing, boundaries, layering, ddd]
---

## 1. Context

VeggieShop is a modular monorepo with **bounded contexts** and **clean architecture** layering. Over time, ad-hoc imports and shortcuts can erode boundaries (e.g., controllers calling repositories, domain using framework types, cross-context leaks). We need automated **architecture tests** to prevent regressions.

## 2. Decision

Use **ArchUnit** tests located in `modules/testing/src/test/java/com/veggieshop/testing/ArchRules.java` (and per-context variants) to enforce key dependency rules:

- **No cross-context internals** imports (use APIs/events).
- **Layering:** Controllers → Application → Domain; Infrastructure implements ports only.
- **Kernel purity:** `domain-kernel` must remain framework-free.
- **Platform isolation:** Contexts may depend on `platform`, but **platform** must not depend on contexts.

## 3. Scope

Applies to all Java code under `modules/**` except generated sources and explicit test fixtures.

## 4. Rationale

- Prevents architectural drift with low maintenance.  
- Communicates boundaries to contributors via failing tests.  
- Makes refactoring safer and CI-enforced.

## 5. Rules (Illustrative)

### 5.1 Layering (Per Context)

```java
layeredArchitecture()
  .consideringAllDependencies()
  .layer("api").definedBy("..api.http..")
  .layer("application").definedBy("..application..")
  .layer("domain").definedBy("..domain..")
  .layer("infrastructure").definedBy("..infrastructure..")
  .whereLayer("api").mayOnlyBeAccessedByLayers("")
  .whereLayer("application").mayOnlyBeAccessedByLayers("api")
  .whereLayer("domain").mayOnlyBeAccessedByLayers("application")
  .whereLayer("infrastructure").mayOnlyBeAccessedByLayers("application");
```

### 5.2 Controllers Do Not Use Repositories Directly

```java
noClasses().that().resideInAPackage("..api.http..")
  .should().dependOnClassesThat().resideInAPackage("..infrastructure.persistence..");
```

### 5.3 Domain is Framework-Free

```java
noClasses().that().resideInAPackage("..domain..")
  .should().dependOnClassesThat().resideInAnyPackage(
    "org.springframework..", "jakarta.persistence..", "com.fasterxml.jackson..");
```

### 5.4 Shared Kernel Isolation

```java
noClasses().that().resideInAPackage("..shared..")
  .should().dependOnClassesThat().resideInAnyPackage("..api..","..infrastructure..","..auth..","..catalog..");
```

### 5.5 Platform Does Not Depend on Contexts

```java
noClasses().that().resideInAPackage("..platform..")
  .should().dependOnClassesThat().resideInAPackage("..contexts..");
```

### 5.6 Problem Details Enforcement

```java
noClasses().that().resideInAPackage("..api.http..")
  .should().callMethodWhere(targetOwner("org.springframework.http.ResponseEntity"),
                             named("status"))
  .because("Controllers must delegate to ExceptionMappingService / Problem Details factory");
```

> Package names must match the actual structure (e.g., `com.veggieshop.<context>.api.http`). Use shared utilities in `modules/testing` to compose rules per context.

## 6. CI Integration

- Run ArchUnit tests as part of `./mvnw clean verify`.  
- Fail the pipeline on rule violations.  
- Offer a **waiver mechanism** (temporary ignores) with explicit expiry dates and links to issues/PRs.

## 7. Test Data & Generated Code

- Exclude generated mappers/clients from strict rules if necessary, but keep them in a separate package (`..generated..`).  
- Allow tests to use framework classes in test scope; rules target `main` sources primarily.

## 8. Documentation

- Document rules in `guides/conventions.md` and place class-level Javadoc on `ArchRules.java`.  
- Provide quick links from README for new contributors.

## 9. Alternatives Considered

1. **Code reviews only:** insufficient; human reviews miss subtle imports.  
2. **Build-time module systems (JPMS):** heavy for the current repo; ArchUnit is faster to iterate.  
3. **Gradle/Maven enforcer only:** good complement, but lacks code-level graph analysis.

## 10. Consequences

**Positive**
- Predictable module boundaries, fewer regressions.  
- Clear feedback loop for contributors.  
**Negative**
- Occasional friction during refactoring; mitigated by updating rules + docs.

## 11. Implementation Plan

1. Consolidate shared rules in `modules/testing`.  
2. Add per-context `@ArchTest` suites that specialize package paths.  
3. Wire CI to fail on violations and publish reports.  
4. Add docs and examples to `guides/conventions.md`.

## 12. Rollback

- If a rule proves too strict, **relax** or **scope** it rather than removing wholesale; document rationale with a decision log.

## 13. Open Questions

- Do we gate releases on **zero** violations or allow a temporary budget?  
- Should we generate ArchUnit packages from a single source of truth (YAML) to reduce duplication?

## 14. References

- `modules/testing/src/test/java/com/veggieshop/testing/ArchRules.java`  
- ADRs: `ADR-0001-shared-kernel.md`, `ADR-0003-problem-details-only.md`, `ADR-0006-ports-and-adapters.md`
