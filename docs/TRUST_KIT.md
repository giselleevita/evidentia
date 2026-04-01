# Evidentia — Trust Kit

> Procurement-ready security and architecture documentation for enterprise compliance buyers.

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────┐
│                    Azure Entra ID (OIDC)                      │
│                    Multi-Tenant RBAC                           │
└──────────┬───────────────────────────────────┬───────────────┘
           │                                   │
┌──────────▼──────────┐  ┌───────────────────▼──────────────┐
│  Evidence Service    │  │  Audit Log Service                │
│  (Kotlin/Spring)     │  │  (Kotlin/Spring)                  │
│  Port 8080           │  │  Port 8081                        │
│                      │  │  Immutable append-only             │
│  Lifecycle:          │  │  Hash-chained records              │
│  DRAFT → IN_REVIEW   │  │                                   │
│  → APPROVED → LOCKED │  │                                   │
└──────────┬──────────┘  └───────────────────┬───────────────┘
           │                                 │
┌──────────▼──────────┐  ┌─────────────────▼────────────────┐
│  Incident Service    │  │  Integration Service              │
│  (Kotlin/Spring)     │  │  (Kotlin/Spring)                  │
│  Port 8083           │  │  M365 / GitHub / Jira connectors  │
└──────────┬──────────┘  └─────────────────┬────────────────┘
           │                                │
           └──────────┬─────────────────────┘
                      │
          ┌───────────▼───────────┐
          │   PostgreSQL           │
          │   (Separate DB/service)│
          │   Flyway Migrations    │
          └───────────────────────┘
```

## Data Residency

- All data stored within customer-designated Azure region
- Default: Azure West Europe (Netherlands)
- No cross-region replication unless customer-configured
- Database encryption at rest: Azure Storage Service Encryption (AES-256)
- In transit: TLS 1.2+ enforced on all inter-service and client communication

## Evidence Immutability Guarantee

1. **Application layer**: Evidence transitions DRAFT → IN_REVIEW → APPROVED → LOCKED; LOCKED state is terminal
2. **Audit log layer**: Append-only PostgreSQL table with hash-chained records; each entry includes SHA-256 hash of previous entry
3. **Database layer**: Application role has INSERT-only permission on audit_log table; UPDATE/DELETE revoked
4. **Infrastructure layer**: Database backups are immutable (Azure immutable blob storage)

## Azure Entra Integration

- Authentication: OpenID Connect with Azure Entra ID
- Authorization: Role-based access control (ADMIN, AUDITOR, VIEWER, INTEGRATION)
- Tenant isolation: Every query scoped by tenant_id extracted from JWT claims
- Session management: Stateless JWT validation with configurable token lifetime

## Security Controls

| Control             | Implementation                                                            |
| ------------------- | ------------------------------------------------------------------------- |
| Authentication      | Azure Entra OIDC (no local passwords)                                     |
| Authorization       | RBAC with 4 roles, enforced at service and database layers                |
| Tenant Isolation    | tenant_id filter on every query; separate DB schemas per tenant available |
| Input Validation    | Pydantic-equivalent Kotlin data classes with Bean Validation              |
| Rate Limiting       | Per-tenant, per-endpoint rate limiting                                    |
| Security Headers    | HSTS, CSP, X-Frame-Options, X-Content-Type-Options                        |
| Dependency Scanning | GitHub Dependabot + Snyk                                                  |
| Container Security  | Non-root execution, read-only filesystem, capability drop                 |

## Compliance Positioning

| Standard     | How Evidentia Helps                                                        |
| ------------ | -------------------------------------------------------------------------- |
| ISO 27001    | Continuous evidence capture for Annex A controls; audit-ready export       |
| SOC 2        | Maps to Trust Service Criteria; evidence lifecycle tracks CC/PI/A criteria |
| NIS2         | Incident tracking and audit trails support Art. 21 requirements            |
| GDPR Art. 30 | Record of processing activities supported via evidence metadata            |
