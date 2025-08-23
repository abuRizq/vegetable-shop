# Decision Logs (Lightweight Design Records)

This folder hosts **decision logs** for the VeggieShop backend. Decision logs capture **day‑to‑day** and **tactical** technical decisions that don’t rise to the level of a full ADR (Architecture Decision Record). They provide traceability for why we did something, when, and who agreed—without blocking delivery.

> For **strategic** or **architecturally significant** decisions, create or update an **ADR** under `adr/`. See the ADR index in the repository root and the ADRs already present (e.g., outbox pattern, ports/adapters, error model).

---

## When to write a Decision Log vs. an ADR

| Use | Decision Log | ADR |
|---|---|---|
| Scope | Local change, single module/context, or ops config | Cross-cutting architecture, long-term direction |
| Examples | Choose index for a slow query, adjust retry/backoff, enable a feature flag, pick HTTP pagination style for one endpoint | Adopt outbox pattern, choose persistence strategy, standardize error envelope |
| Turnaround | Hours–days | Days–weeks |
| Required sections | Context, Decision, Alternatives, Consequences | Same plus deeper trade-offs, alignment, migration plan |
| Reviewers | Context maintainers / module owners | Tech leads / architects / multiple maintainers |

If you’re unsure, start with a **decision log** and promote it to an ADR if the impact broadens.

---

## Naming & Location

- Place files in **`docs/decision-logs/`**, optionally grouped by year:  
  `docs/decision-logs/2025/`
- File name format:  
  `DL-YYYYMMDD-<short-slug>.md`  
  Example: `DL-20250820-catalog-paging-cursors.md`

> Keep the slug short, lowercase, hyphen-separated. Use the *date the decision was taken*, not the PR date.

---

## Status Lifecycle

Use one of the following statuses:

- `Proposed` – documented but not yet agreed
- `Accepted` – agreed and implemented or planned to implement
- `Rejected` – discussed but declined (kept for history)
- `Deprecated` – superseded by another decision, to be phased out
- `Superseded by <ID>` – fully replaced by another log/ADR

> When a decision is superseded, **update both** records to cross-link the relationship.

---

## Front Matter (YAML)

Every decision log starts with YAML front matter:

```yaml
---
id: DL-20250820-catalog-paging-cursors
title: Use cursor-based pagination for product listings in Catalog
date: 2025-08-20
status: Accepted
authors:
  - full_name: Jane Doe
    handle: @jane
reviewers:
  - @catalog-maintainers
context_tags: [catalog, api, performance]
categories: [api, data, performance]
links:
  issues: [ "#1234" ]
  prs: [ "#1235" ]
  docs: [ "modules/contracts/http/openapi/catalog.yaml" ]
supersedes: null
superseded_by: null
---
```

**Fields**

- `id` – file base name (unique)
- `title` – succinct decision title
- `date` – ISO date of decision
- `status` – see lifecycle
- `authors`, `reviewers` – participants (GitHub handles or emails)
- `context_tags` – which bounded contexts? (e.g., `auth`, `catalog`, `order`)
- `categories` – e.g., `api`, `data`, `security`, `ops`, `testing`, `build`
- `links` – issues/PRs/docs relevant to the decision
- `supersedes`, `superseded_by` – id of related decision/ADR if applicable

---

## Body Template

Use the following sections in the body. Keep it concise and pragmatic.

```markdown
## Context
What problem are we solving? Include constraints and any measurements (latency, error rate, throughput).

## Decision
What did we decide? Be precise about the recommendation and the scope (module/context).

## Alternatives Considered
- Alternative A — trade-offs
- Alternative B — trade-offs
- (Optional) Non-goals

## Consequences
- Positive impacts
- Risks and mitigations
- Monitoring/metrics to watch

## Rollback / Exit Strategy
How to revert if the decision fails in prod? (feature flag, config toggle, DB rollback plan)

## Implementation Notes
Pointers to code areas, configuration keys, feature flags, and tests to add/update.

## Follow-ups / Tasks
- [ ] Item 1
- [ ] Item 2
```

> Keep logs brief (~1–2 pages). Use an ADR for deep analysis and long-lived strategy.

---

## Process

1. **Draft** a decision log in a branch under `docs/decision-logs/` (use the naming scheme).
2. Open a **PR** titled: `[DL] <title>` with relevant labels (e.g., `decision`, `context:catalog`).
3. Request review from **module owners/maintainers** of the affected area.
4. On approval, **merge** and set `status: Accepted` (or `Rejected`). Link the PR/issue in the front matter.
5. If the decision is later superseded, update `status` and `superseded_by` / `supersedes` fields in both records.

---

## Governance & Roles

- **Context Maintainers**: Review decisions in their bounded context.
- **Tech Leads/Architects**: Review cross-context or high-impact decisions; may request ADR.
- **Infra/Ops**: Review decisions affecting deployment/runtime (Helm, SOPS, Kafka, DB).

> Use CODEOWNERS to auto-request the right reviewers.

---

## Style & Quality Checklist

- [ ] Title is clear and scoped
- [ ] Status is set correctly (Proposed/Accepted/Rejected/Deprecated)
- [ ] Context is concise with data where possible
- [ ] Alternatives and trade-offs are listed
- [ ] Consequences include risks and metrics
- [ ] Rollback plan exists
- [ ] Links to PRs/issues/docs are present
- [ ] Context tags and categories are set

---

## Index Generation (Optional)

To list all decision logs in a simple index, run:

```bash
# From repository root
echo "# Decision Log Index" > docs/decision-logs/INDEX.md
echo "" >> docs/decision-logs/INDEX.md
grep -R --include="DL-*.md" -h '^title:' docs/decision-logs \
  | sed -E 's/title:\s*/- /' >> docs/decision-logs/INDEX.md
```

For a richer index, consider a small script to parse the YAML front matter and generate a table with `date`, `status`, and `links`.

---

## Relationship to Other Docs

- **ADRs (`adr/`)** document strategies and significant architecture decisions that are harder to change.
- **Guides (`guides/`)** explain *how* to do things (local dev, release process, troubleshooting).
- **Architecture (`architecture/*.puml`)** offers up-to-date diagrams (C1/C2/C3/ERD/Sequences).
- **README** provides onboarding and quick links.

---

## Examples

- **DL-20250820-catalog-paging-cursors.md** – choose cursor vs. offset pagination for product listings
- **DL-20250820-auth-hash-params.md** – tweak password hashing cost parameters
- **DL-20250820-inventory-batch-size.md** – adjust batch size for stock reservation worker

> Use examples as a starting point; keep decisions small and iterative.
