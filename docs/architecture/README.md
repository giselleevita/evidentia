# Architecture Documentation

## Overview

Evidentia is an enterprise-grade compliance infrastructure platform built as a microservices monorepo.

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
- **Audit Log Service**: Centralized immutable audit logging
- **Integration Service**: External system integrations (Microsoft 365, GitHub, Jira)

### Technology Stack

- **Backend**: Kotlin + Spring Boot
- **Frontend**: React + TypeScript + Vite
- **Database**: PostgreSQL (separate databases per service)
- **Infrastructure**: Azure (AKS, Azure SQL, Key Vault)
- **Auth**: Azure Entra ID (OIDC)

## Multi-Tenancy

All services implement multi-tenancy with tenant isolation at the data layer. Tenant ID is extracted from JWT tokens and enforced in all queries.

## Security

- OAuth2/OIDC with Azure Entra ID
- RBAC via Azure AD App Roles
- All endpoints authenticated by default
- Secrets stored in Azure Key Vault
