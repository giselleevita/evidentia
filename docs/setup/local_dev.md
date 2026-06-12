# Local Development Setup

## Prerequisites

- JDK 17+
- Node.js 20+
- Docker & Docker Compose
- Azure CLI (for Azure AD configuration)

## Quick Start

### 1. Start Infrastructure Services

```bash
cd infra/docker
docker compose up -d
```

This starts:
- PostgreSQL for evidence service (port 15432)
- PostgreSQL for audit log service (port 5433)
- PostgreSQL for incident service (port 5434)
- PostgreSQL for rating service (port 5435)
- PostgreSQL for integration service (port 5436)
- Redis (port 6379)

### 2. Configure Azure AD

1. Register applications in Azure AD:
   - Frontend app (public client)
   - Backend API (resource server)

2. Set environment variables:
```bash
export AZURE_AD_ISSUER_URI="https://login.microsoftonline.com/{tenant-id}/v2.0"
export AZURE_AD_JWK_SET_URI="https://login.microsoftonline.com/{tenant-id}/discovery/v2.0/keys"
```

### 3. Run Backend Services

```bash
# Evidence Service
DATABASE_URL=jdbc:postgresql://localhost:15432/evidentia_evidence ./gradlew :backend:evidence-service:bootRun

# Audit Log Service (in another terminal)
DATABASE_URL=jdbc:postgresql://localhost:5433/evidentia_audit ./gradlew :backend:audit-log-service:bootRun

# Integration Service (in another terminal)
INTEGRATION_DB_URL=jdbc:postgresql://localhost:5436/evidentia_integration \
INTEGRATION_DB_USER=evidentia \
INTEGRATION_DB_PASS=evidentia \
./gradlew :backend:integration-service:bootRun
```

### 4. Run Frontend

```bash
cd frontend/compliance-portal
npm install
npm run lint
npm run dev
```

## Database Migrations

Migrations run automatically via Flyway on service startup.

To manually run migrations:
```bash
./gradlew :backend:evidence-service:flywayMigrate
./gradlew :backend:audit-log-service:flywayMigrate
```

## Testing

### Backend
```bash
./gradlew test
```

### Frontend
```bash
cd frontend/compliance-portal
npm run lint
npm test -- --run
npm run build
```

## Troubleshooting

### Port Conflicts
- Evidence Service: 8080
- Audit Log Service: 8081
- Rating Service: 8082
- Incident Service: 8083
- Integration Service: 8084
- Frontend: 5173

### Database Connection Issues
Ensure Docker containers are running:
```bash
docker ps
```

### Azure AD Token Issues
Verify your Azure AD app registration and redirect URIs match your local setup.
