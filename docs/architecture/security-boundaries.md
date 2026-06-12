# Security Boundaries

Evidentia is a reference implementation, not a production security appliance.
This document records the controls implemented in the repository and the
remaining boundaries that require deployment-specific work.

## Implemented

- Backend endpoints require a validated OAuth2 bearer token except health and
  info endpoints.
- Azure Entra ID `roles` claims are mapped to Spring `ROLE_*` authorities for
  method-level authorization.
- The tenant identifier is derived from the validated JWT. A conflicting
  `X-Tenant-Id` header is rejected.
- Audit trail, correlation, and resource queries are scoped to the authenticated
  tenant.
- Audit ingestion requires an `Admin` or internal `Service` role, and the
  persisted tenant is derived from the validated token.
- Webhook destinations must use HTTPS and resolve only to public addresses.
- OpenAPI and Swagger endpoints are disabled unless `OPENAPI_ENABLED=true`.
- Frontend authentication fails closed unless Azure Entra ID configuration is
  present or explicit UI-only demo mode is enabled.

## Known Limitations

- Service-to-service authentication and authorization are not implemented.
  Audit delivery currently uses synchronous HTTP and requires deployment-level
  identity before it is reliable across services.
- Audit rows are append-oriented by API design but are not cryptographically
  tamper-evident or protected from privileged database modification.
- Webhook signing secrets are stored in the integration database and require
  encryption-at-rest and key-management controls in a production deployment.
- Tenant isolation is implemented in application queries, not with PostgreSQL
  row-level security.
- The Azure Terraform and Kubernetes files are partial reference templates, not
  a validated production deployment.
