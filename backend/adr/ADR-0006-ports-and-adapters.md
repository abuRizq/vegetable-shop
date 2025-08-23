---
id: ADR-0006
title: Ports and Adapters (Hexagonal) Architecture per Bounded Context
status: Accepted
date: 2025-08-20
deciders:
  - VeggieShop Maintainers
  - Platform & Architecture Leads
consulted:
  - Context Maintainers (auth, catalog, inventory, order, pricing, customer, checkout, vendor, media, review, notification, audit)
tags: [ddd, ports-adapters, hexagonal, clean-architecture, layering]
---

## 1. Context

Each bounded context must remain independently evolvable. Direct dependencies on frameworks or external systems inside the domain/application layers increase coupling and reduce testability. We need a consistent pattern to **decouple** the core from infrastructure details while keeping code **navigable**.

## 2. Decision

Adopt **Ports and Adapters** (Hexagonal) architecture **per context**:

- **Ports** (interfaces) live under `application/port/**` (e.g., `OrderRepositoryPort`, `PaymentGatewayPort`).  
- **Application services** depend on ports and domain model; they orchestrate use cases.  
- **Adapters** live under `infrastructure/**` and implement ports (e.g., JPA repositories, Kafka producers, external HTTP clients).  
- **API layer** (`api/http/**`) exposes controllers and DTO mapping without leaking framework or transport details into the domain.

## 3. Scope

- All contexts in `modules/contexts/**`.
- Cross-cutting concerns in `modules/platform/**` support, not replace, hexagonal boundaries.

## 4. Rationale

- **Testability:** Core logic can be tested without infra (use in-memory fakes for ports).  
- **Replaceability:** Swap adapters (e.g., Postgres→another store) with minimal impact.  
- **Clarity:** Explicit boundaries surface integration points and ownership.

## 5. Structure

```
context-xyz/
├─ domain/                 # Entities, VOs, domain services
├─ application/
│  ├─ port/                # Ports (interfaces): repositories, gateways
│  ├─ service/             # Service interfaces (use cases)
│  └─ impl/                # Service implementations
├─ infrastructure/
│  ├─ persistence/         # JPA repos, converters, specs, adapters
│  ├─ messaging/           # Kafka consumers/producers
│  ├─ security/            # Context-specific security adapters
│  └─ adapter/             # Other external integrations
└─ api/http/
   ├─ controller/          # REST controllers
   ├─ dto/                 # Request/Response DTOs
   └─ mapper/              # MapStruct/manual mappers
```

## 6. Guidelines

### 6.1 Dependencies

- **Application** depends on **domain** and **ports** only.  
- **Infrastructure** depends on **application** (to implement ports) and frameworks.  
- **API** depends on **application** (never on infrastructure).  
- **Domain** depends on nothing but JDK and shared kernel types.

### 6.2 Mapping

- Use dedicated **mappers** (`mapper/*`) for DTO↔Domain; avoid leaking DTOs/entities into domain.  
- Persist domain via **adapters** that translate to JPA entities or projections.  
- For events, publish via **outbox** adapter; do not publish from domain services.

### 6.3 Transactions

- Boundary at application services: annotate transactions there.  
- Avoid transactional logic in controllers.  
- Domain remains unaware of transactions.

### 6.4 Validation

- Edge validation at API (request DTO) + **domain invariants** in constructors/factories.  
- Use `Problem Details` (ADR-0003) for HTTP error mapping.

### 6.5 Configuration

- Auto-config lives in `modules/platform-autoconfigure` to wire common beans (error handling, security, persistence, observability) without coupling domain code to Spring.

### 6.6 Naming & Packaging

- Ports end with `Port` or `Gateway`.  
- Adapters end with `Adapter` (e.g., `UserRepositoryAdapter`, `KafkaOutboxPublisher`).  
- Keep packages shallow and consistent across contexts.

### 6.7 Testing

- Unit tests for application services using in-memory fakes of ports.  
- Integration tests for adapters (JPA repositories, HTTP clients) with Testcontainers.  
- System tests for end-to-end flows using real adapters under containers.

## 7. Examples

### 7.1 Repository Port & Adapter (Inventory)

```java
// application/port/InventoryItemRepositoryPort.java
public interface InventoryItemRepositoryPort {
  Optional<InventoryItem> findBySku(String sku);
  InventoryItem save(InventoryItem item);
}
```

```java
// infrastructure/adapter/InventoryItemRepositoryAdapter.java
@Component
@RequiredArgsConstructor
class InventoryItemRepositoryAdapter implements InventoryItemRepositoryPort {
  private final InventoryItemRepository jpaRepository; // Spring Data JPA
  private final InventoryItemMapper mapper;
  public Optional<InventoryItem> findBySku(String sku) {
    return jpaRepository.findBySku(sku).map(mapper::toDomain);
  }
  public InventoryItem save(InventoryItem item) {
    var entity = mapper.toEntity(item);
    return mapper.toDomain(jpaRepository.save(entity));
  }
}
```

### 7.2 External Gateway Port (Payment)

```java
// application/port/PaymentGatewayPort.java
public interface PaymentGatewayPort {
  PaymentTransaction authorize(PaymentRequest request);
  PaymentTransaction capture(String authorizationId, Money amount);
  void refund(String transactionId, Money amount);
}
```

Adapters implement this via REST/SDK in `infrastructure/adapter/payment/*`.

## 8. Enforcement

- **ArchUnit** rules (ADR-0005) assert no forbidden dependencies (API→Infra, Domain→Framework, etc.).  
- CI fails on violations; use temporary waivers with expiry.

## 9. Alternatives Considered

1. **Anemic services + rich controllers** — rejected; couples transport with business logic.  
2. **Direct use of Spring Data repositories in controllers** — rejected; bypasses application layer contracts.  
3. **Single-layer service** — rejected; harms testability and modularity.

## 10. Consequences

**Positive**
- Clear seams for testing and replacement; easier onboarding.  
- Reduced coupling to frameworks.  
**Negative**
- Additional boilerplate (ports/mappers), mitigated by templates in `tools/codegen/*`.

## 11. Implementation Plan

1. Ensure each context has `application/port` and `infrastructure/adapter` packages.  
2. Audit controllers to use **application services** only.  
3. Extract any infra logic from domain to adapters.  
4. Add templates under `tools/codegen/` (already present) for DTOs/mappers.  
5. Strengthen ArchUnit rules to catch leaks.

## 12. Rollback

- If a context proves over-modularized, we may simplify adapter structure while keeping ports intact. Document via a decision log.

## 13. Open Questions

- Should we standardize mapper generation with **MapStruct** everywhere, or allow manual mappers in smaller contexts?  
- Do we want a shared **adapter testing kit** in `modules/testing`?

## 14. References

- `modules/platform*`, `modules/contexts/**`  
- ADRs: `ADR-0003-problem-details-only.md`, `ADR-0005-archunit-boundaries.md`
