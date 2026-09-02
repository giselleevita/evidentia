# Contributing

Evidentia is maintained as a reference implementation. Keep changes narrow,
tested, and explicit about production limitations.

## Local Verification

```bash
./gradlew build --no-daemon
cd frontend/compliance-portal
npm ci
npm run lint
npm test -- --run
npm run build
```

Also run `docker compose -f infra/docker/docker-compose.yml config --quiet`
when changing local infrastructure.

## Security-Sensitive Changes

Changes to authentication, authorization, tenant-scoped queries, audit events,
webhooks, or deployment configuration require regression tests and an update to
`docs/architecture/security-boundaries.md`.
