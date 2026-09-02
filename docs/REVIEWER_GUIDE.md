# Evidentia — Reviewer Guide

This guide helps recruiters and senior engineers evaluate the project in about 15 minutes without needing production credentials.

## What problem it demonstrates

Evidentia models how an organization turns IT and security activity into **reviewable compliance evidence**: lifecycle states (DRAFT → IN_REVIEW → APPROVED → LOCKED), tenant-scoped audit events, incident workflows, and external integrations.

## Architecture (60 seconds)

- **Frontend:** React + TypeScript + Vite compliance portal (`frontend/compliance-portal/`)
- **Backend:** Kotlin / Spring Boot microservices (`backend/*-service/`)
- **Data:** PostgreSQL per service, Flyway migrations
- **Auth:** Azure Entra ID–compatible OIDC, RBAC via app roles, tenant context from JWT claims
- **Controls:** Method-level `@PreAuthorize`, HTTPS-only webhooks, audit emission on writes

See the architecture diagram in [README.md](../README.md).

## Review without cloud credentials

Use JDK 17, matching the Gradle toolchain and CI. From the repository root:

```bash
./gradlew :backend:evidence-service:test --no-daemon
```

On Windows PowerShell, use `.\gradlew.bat` instead of `./gradlew`. Gradle downloads
dependencies on the first run; these tests do not need Docker, a running database,
or an Entra ID application registration.

Start with [EvidenceServiceTest](../backend/evidence-service/src/test/kotlin/com/evidentia/evidence/application/EvidenceServiceTest.kt).
It passes a synthetic "Access review" record through the real application
service using an in-memory repository and a recording audit client:

| Input / action | Expected result |
|---|---|
| Create, submit, approve, then lock evidence | `DRAFT → IN_REVIEW → APPROVED → LOCKED` |
| Inspect the recorded audit actions | `evidence.created`, `evidence.submitted`, `evidence.approved`, `evidence.locked` |
| Try to approve a draft directly | `InvalidTransition`; no approval audit event |
| Read or update the record as a different tenant | `NotFound` |

Also inspect [EvidenceControllerSecurityTest](../backend/evidence-service/src/test/kotlin/com/evidentia/evidence/adapters/web/EvidenceControllerSecurityTest.kt)
for HTTP authorization checks. Expected command result: `BUILD SUCCESSFUL` with
passing tests. Open `backend/evidence-service/build/reports/tests/test/index.html`
for the individual results. This verifies local application and controller
behavior, not a deployed identity provider, PostgreSQL isolation, or audit delivery.

For the complete backend and frontend checks:

```bash
./gradlew build --no-daemon
cd frontend/compliance-portal
npm ci
npm run lint
npm test -- --run
npm run build
```

## Authenticated application path

Complete [local setup](setup/local_dev.md) before starting the application. A
frontend public-client registration, API configuration, and separate credentials
for authenticated service-to-service audit delivery are needed for that workflow.

```bash
./start.sh          # infra + services + frontend
# open http://localhost:5173
./stop.sh           # teardown
```

Without the frontend Entra ID variables, the portal displays an authentication
configuration message. Starting Vite alone does not provide a mock evidence flow.

## 15-minute review checklist

| Step | Where to look | What to verify |
|------|---------------|----------------|
| 1 | `backend/evidence-service/` domain + API | Evidence lifecycle and tenant scoping |
| 2 | `backend/audit-log-service/` | Centralized, tenant-scoped audit events |
| 3 | `frontend/compliance-portal/src/` | Portal flows for evidence and incidents |
| 4 | `backend/common/` | Shared domain models, security config, and tenant context used by all services |
| 5 | `.github/workflows/ci.yml` | Backend + frontend build gates |

**Tests to skim:**
- Backend: `./gradlew test` (evidence + audit integration patterns)
- Frontend: `cd frontend/compliance-portal && npm run lint && npm test -- --run`

## What this is / is not

- **Is:** A multi-service reference implementation with realistic compliance boundaries
- **Is not:** Production-hardened or certified for regulated deployment without further validation

## Design choices and failure handling

- Five services with separate databases make domain boundaries visible, but add
  deployment and cross-service consistency costs compared with a modular monolith.
- Audit delivery uses OAuth2 client credentials. Business operations can continue
  when audit delivery fails, with the failure logged; the workflow is not an
  atomic transaction across the business database and the audit service.
- Tenant context and role checks are implemented and locally tested. Those tests
  do not establish production isolation across every infrastructure boundary.

See [security boundaries](architecture/security-boundaries.md) and
[state machines](architecture/state-machines.md) for the concrete controls and limits.
