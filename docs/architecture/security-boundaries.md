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
- Business services acquire OAuth2 client-credentials tokens and attach bearer
  authentication to audit-log-service requests when service authentication is enabled.
- API security tests verify that ordinary users cannot inject audit events,
  conflicting tenant headers are rejected, and service identities can write
  only to the tenant derived from their validated token.
- Webhook destinations must use HTTPS and resolve only to public addresses.
- OpenAPI and Swagger endpoints are disabled unless `OPENAPI_ENABLED=true`.
- Frontend authentication fails closed unless Azure Entra ID configuration is
  present or explicit UI-only demo mode is enabled.

## Known Limitations

- Audit delivery uses authenticated but synchronous, best-effort HTTP. A
  durable queue and delivery monitoring are still required for production
  reliability.
- The current tenancy model treats the validated Entra ID `tid` claim as the
  Evidentia tenant authority. Deployments requiring multiple application
  tenants inside one Entra tenant need a different signed tenant claim and
  authorization model.
- Audit rows are append-oriented by API design but are not cryptographically
  tamper-evident or protected from privileged database modification.
- Webhook signing secrets are stored in the integration database and require
  encryption-at-rest and key-management controls in a production deployment.
- Tenant isolation is implemented in application queries, not with PostgreSQL
  row-level security.
- The Azure Terraform and Kubernetes files are partial reference templates, not
  a validated production deployment.
