# Evidentia — Reviewer Guide

**Private repository — available on request.** This guide helps recruiters and senior engineers evaluate the project in about 15 minutes without needing production credentials.

## What problem it demonstrates

Evidentia models how an organization turns IT and security activity into **reviewable compliance evidence**: lifecycle states (DRAFT → IN_REVIEW → APPROVED → LOCKED), tenant-scoped audit events, incident workflows, and external integrations.

## Architecture (60 seconds)

- **Frontend:** React + TypeScript + Vite compliance portal (`frontend/compliance-portal/`)
- **Backend:** Kotlin / Spring Boot microservices (`backend/*-service/`)
- **Data:** PostgreSQL per service, Flyway migrations
- **Auth:** Azure Entra ID–compatible OIDC, RBAC via app roles, tenant context from JWT claims
- **Controls:** Method-level `@PreAuthorize`, HTTPS-only webhooks, audit emission on writes

See the architecture diagram in [README.md](../README.md).

## Fastest local path

```bash
./start.sh          # infra + services + frontend
# open http://localhost:5173
./stop.sh           # teardown
```

For UI-only review: `./start-frontend-only.sh`

Full setup: [docs/setup/local_dev.md](setup/local_dev.md)

## 15-minute review checklist

| Step | Where to look | What to verify |
|------|---------------|----------------|
| 1 | `backend/evidence-service/` domain + API | Evidence lifecycle and tenant scoping |
| 2 | `backend/audit-log-service/` | Centralized, tenant-scoped audit events |
| 3 | `frontend/compliance-portal/src/` | Portal flows for evidence and incidents |
| 4 | `shared/dto/` or OpenAPI specs | Contract discipline across services |
| 5 | `.github/workflows/ci.yml` | Backend + frontend build gates |

**Tests to skim:**
- Backend: `./gradlew test` (evidence + audit integration patterns)
- Frontend: `cd frontend/compliance-portal && npm run lint && npm test -- --run`

## What this is / is not

- **Is:** A multi-service reference implementation with realistic compliance boundaries
- **Is not:** Production-hardened or certified for regulated deployment without further validation

## Request access

Contact via GitHub profile or portfolio site. Reviewers typically receive read access plus this guide and the main README.
