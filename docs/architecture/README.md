# Architecture Documentation

## Overview

Evidentia is a compliance workflow reference implementation built as a microservices monorepo.

## Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records documenting key architectural decisions.

### ADR Template

When creating a new ADR, use this template:

```markdown
# ADR-XXX: [Title]

## Status
[Proposed | Accepted | Deprecated | Superseded]

## Context
[Describe the issue motivating this decision]

## Decision
[Describe the change that we're proposing or have agreed to implement]

## Consequences
[Describe the resulting context, after applying the decision]
```

## System Architecture

### Microservices

- **Evidence Service**: Manages evidence lifecycle (draft → submitted → approved/rejected)
- **Audit Log Service**: Centralized tenant-scoped audit event storage
- **Integration Service**: External system integrations (Microsoft 365, GitHub, Jira)

### Technology Stack

- **Backend**: Kotlin + Spring Boot
- **Frontend**: React + TypeScript + Vite
- **Database**: PostgreSQL (separate databases per service)
- **Infrastructure**: Docker Compose with partial Azure/Kubernetes reference templates
- **Auth**: Azure Entra ID (OIDC)

## Multi-Tenancy

Tenant ID is extracted from validated JWT tokens and applied to application-layer
queries. The repository does not currently implement database row-level security.

## Security

- OAuth2/OIDC with Azure Entra ID
- RBAC via Azure AD App Roles
- All endpoints authenticated by default
- OpenAPI endpoints disabled by default
- See [Security Boundaries](security-boundaries.md) for limitations

## Draft Extensions

- `PLUGIN_SYSTEM_DESIGN.md` — plugin architecture boundaries and runtime contract
- `WEBHOOK_API_DRAFT.md` — webhook subscription API draft and delivery model
