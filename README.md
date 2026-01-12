# evidentia

Evidentia is an enterprise-grade compliance infrastructure platform that turns existing IT and security activity into continuous, audit-ready evidence.

## Architecture

- **Backend**: Microservices architecture with Kotlin + Spring Boot
  - Evidence Service: Evidence lifecycle management (DRAFT → IN_REVIEW → APPROVED → LOCKED)
  - Audit Log Service: Centralized immutable audit logging
  - Incident Service: Security incident tracking and resolution
  - Integration Service: External system integrations (Microsoft 365, GitHub, Jira)
- **Frontend**: React + TypeScript + Vite (compliance portal)
- **Database**: PostgreSQL with Flyway migrations (separate DBs per service)
- **Auth**: Azure Entra ID (OIDC) with RBAC
- **Infrastructure**: Docker Compose (local), Azure AKS (production), Terraform (IaC)

## Repository Structure

This is a **monorepo** containing all services, frontend, infrastructure, and documentation:

```
evidentia/
├── backend/                    # Backend microservices
│   ├── common/                 # Shared backend code (domain models, security, TenantContext)
│   ├── evidence-service/       # Evidence management service
│   ├── audit-log-service/      # Audit logging service
│   ├── incident-service/       # Incident governance service
│   └── integration-service/   # External integrations service
├── frontend/
│   └── compliance-portal/      # Main React application
├── shared/                     # Cross-stack shared resources
│   └── dto/                    # API contracts, OpenAPI specs
├── infra/                      # Infrastructure as Code
│   ├── docker/                 # Dockerfiles and docker-compose
│   ├── k8s/                    # Kubernetes manifests
│   ├── terraform/              # Terraform for Azure resources
│   └── pipelines/              # CI/CD pipeline definitions
└── docs/                       # Documentation
    ├── architecture/           # Architecture decision records
    └── setup/                  # Setup and deployment guides
```

## Quick Start

### ⚡ Easiest Way to Start

**Start everything with one command:**
```bash
./start.sh
```

Then open: **http://localhost:5173**

**Frontend only (fastest, for UI testing):**
```bash
./start-frontend-only.sh
```

**Stop everything:**
```bash
./stop.sh
```


### Prerequisites
- JDK 17+
- Node.js 18+
- Docker & Docker Compose
- Azure CLI (for Azure AD configuration)

### Local Development (Manual)

1. **Start infrastructure services:**
```bash
cd infra/docker
docker-compose up -d
```

2. **Configure Azure AD** (see [Local Dev Setup](docs/setup/local_dev.md))

3. **Run backend services:**
```bash
# Evidence Service (port 8080)
./gradlew :backend:evidence-service:bootRun

# Audit Log Service (port 8081) - in another terminal
./gradlew :backend:audit-log-service:bootRun

# Incident Service (port 8083) - in another terminal
./gradlew :backend:incident-service:bootRun
```

4. **Run frontend:**
```bash
cd frontend/compliance-portal
npm install
npm run dev
```

See [Local Development Guide](docs/setup/local_dev.md) for detailed setup instructions.

## Development

### Running Tests
- **Backend**: `./gradlew test` (runs tests for all services)
- **Frontend**: `cd frontend/compliance-portal && npm test`

### Building
- **Backend**: `./gradlew build`
- **Frontend**: `cd frontend/compliance-portal && npm run build`

### Database Migrations
Migrations run automatically via Flyway on service startup. Each service has its own database and migrations in `backend/{service}/src/main/resources/db/migration/`.

## Key Features

### Multi-Tenancy
All services implement tenant isolation. Every entity includes `tenantId`, and all queries are automatically filtered by tenant. Tenant ID is extracted from JWT tokens (`tid` or `tenant_id` claim).

### Audit Logging
Every write operation emits an immutable audit event to the audit-log-service with:
- Actor (user)
- Tenant ID
- Action
- Resource type and ID
- Correlation ID
- Timestamp
- Metadata

### Security
- OAuth2/OIDC with Azure Entra ID
- RBAC via Azure AD App Roles (Admin, Auditor, User)
- Method-level security with `@PreAuthorize` annotations
- Automatic tenant context extraction from JWT tokens
- All endpoints authenticated by default (except `/actuator/health`, `/swagger-ui/**`)
- Secrets stored in Azure Key Vault (production)

See [RBAC Documentation](docs/architecture/RBAC.md) for role definitions and permissions.

## CI/CD

CI pipelines are defined in `infra/pipelines/`. The pipeline:
- Runs tests for changed services (path-based triggers)
- Builds Docker images
- Performs security scans
- Deploys to staging/production

See [Azure Deployment Guide](docs/setup/azure_deployment.md) for production deployment.

## Documentation

- [Architecture Documentation](docs/architecture/README.md)
- [RBAC and Role Definitions](docs/architecture/RBAC.md)
- [State Machines](docs/architecture/state-machines.md)
- [Local Development Setup](docs/setup/local_dev.md)
- [Azure Deployment Guide](docs/setup/azure_deployment.md)

## Contributing

1. Create a feature branch from `main`
2. Make changes following the existing architecture patterns
3. Add tests for new functionality
4. Update documentation as needed
5. Submit a pull request

## License

[Add license information]
