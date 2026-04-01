# Plugin System Design (Draft v1)

Status: Draft
Owner: Integration Service
Date: 2026-04-01

## Goal

Define a plugin model that lets Evidentia ingest and emit compliance events
without coupling core services to vendor-specific logic.

## Plugin Boundaries

- Core services own domain state and validation.
- Plugins own external protocol adapters and transformation rules.
- Plugin failures must not break core evidence write paths.

## Plugin Types

- `ingest`: imports external findings/evidence into Evidentia domain format.
- `egress`: publishes internal events to external systems.
- `enrichment`: augments records with metadata (risk tags, ownership, mapping).

## Runtime Contract

Each plugin implements:

- `plugin_id` (stable identifier)
- `version` (semantic version)
- `supported_events` (set of event types)
- `health()` (connectivity + credential validation)
- `execute(payload)` (deterministic transform/dispatch)

## Safety Requirements

- Tenant context must be injected by platform, never trusted from plugin payload.
- Plugin secrets are loaded from environment/secret store only.
- Every plugin invocation emits an audit event with correlation ID.
- Retry policy is bounded and dead-letters on persistent failure.

## Initial Roadmap

1. Define Kotlin interface package under integration-service.
2. Add plugin registry with allow-list by tenant.
3. Add plugin execution audit events.
4. Add webhook plugin as reference egress implementation.
