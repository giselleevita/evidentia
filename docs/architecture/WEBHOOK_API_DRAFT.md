# Webhook API Draft (v1)

Status: Draft
Service: integration-service
Base path: `/api/v1/webhooks`

## Purpose

Provide tenant-scoped webhook subscription management for outbound integration events.

## Endpoints

### `GET /api/v1/webhooks`

- Roles: `Admin`, `Auditor`
- Response: `ApiResponse<List<WebhookDto>>`

### `POST /api/v1/webhooks`

- Roles: `Admin`
- Request:

```json
{
  "targetUrl": "https://example.com/evidentia/webhooks",
  "eventTypes": ["evidence.created", "incident.updated"]
}
```

- Response: `201 Created` with created subscription DTO.

### `PATCH /api/v1/webhooks/{id}/pause`

- Roles: `Admin`
- Effect: marks subscription status as `PAUSED`.

### `DELETE /api/v1/webhooks/{id}`

- Roles: `Admin`
- Response: `204 No Content`.

## Delivery Model

- Event enqueue is tenant-scoped and filtered by `eventTypes`.
- Dispatcher retries bounded attempts and tracks delivery status.
- Failed deliveries are retained for operational triage.

## Planned v2 Additions

- Signature headers (`X-Evidentia-Signature`, timestamp)
- Endpoint verification handshake
- Dead-letter replay endpoint
- Per-subscription rate limits
