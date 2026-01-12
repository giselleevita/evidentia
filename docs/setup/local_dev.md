# Local Development Setup

## Prerequisites

- JDK 17+
- Node.js 18+
- Docker & Docker Compose
- Azure CLI (for Azure AD configuration)

## Quick Start

### 1. Start Infrastructure Services

```bash
cd infra/docker
docker-compose up -d
```

This starts:
- PostgreSQL for evidence service (port 5432)
- PostgreSQL for audit log service (port 5433)
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
./gradlew :backend:evidence-service:bootRun

# Audit Log Service (in another terminal)
./gradlew :backend:audit-log-service:bootRun

# Integration Service (optional)
./gradlew :backend:integration-service:bootRun
```

### 4. Run Frontend

```bash
cd frontend/compliance-portal
npm install
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
npm test
```

## Troubleshooting

### Port Conflicts
- Evidence Service: 8080
- Audit Log Service: 8081
- Integration Service: 8082
- Frontend: 5173

### Database Connection Issues
Ensure Docker containers are running:
```bash
docker ps
```

### Azure AD Token Issues
Verify your Azure AD app registration and redirect URIs match your local setup.
