# Contributing

Evidentia is maintained as a reference implementation. Keep changes narrow,
tested, and explicit about production limitations.

## Cloning on Windows

Some paths in this repository exceed the legacy 260-character Windows limit, for
example `backend/integration-service/src/main/kotlin/com/evidentia/integration/adapters/persistence/WebhookSubscriptionJpaRepository.kt`.

Enable long paths **before** cloning:

```bash
git config --global core.longpaths true
```

Without it, `git clone` reports "Clone succeeded, but checkout failed" and leaves a
working tree that is missing files while the index still references them. A commit
made in that state records the missing files as **deletions** — it is possible to
wipe most of the repository with a single innocuous-looking commit. If you see
`Filename too long` during a clone, stop and fix the setting rather than continuing.

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
