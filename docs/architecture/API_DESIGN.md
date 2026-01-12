# API Design Standards

## Response Format

All API endpoints return a standardized `ApiResponse<T>` wrapper:

### Success Response
```json
{
  "success": true,
  "data": {
    // Response data here
  },
  "error": null,
  "timestamp": "2024-01-08T12:00:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed",
    "details": {
      "title": "Title is required",
      "description": "Description must not be empty"
    }
  },
  "timestamp": "2024-01-08T12:00:00Z"
}
```

## Error Codes

Standard error codes used across the platform:

- `VALIDATION_ERROR` - Request validation failed (400)
- `NOT_FOUND` - Resource not found (404)
- `INVALID_ARGUMENT` - Invalid argument provided (400)
- `ILLEGAL_STATE` - Operation not allowed in current state (409)
- `INVALID_TRANSITION` - Invalid state transition (400)
- `UNAUTHORIZED` - Authentication required (401)
- `FORBIDDEN` - Insufficient permissions (403)
- `INTERNAL_ERROR` - Unexpected server error (500)

## HTTP Status Codes

- `200 OK` - Successful GET, PUT, PATCH
- `201 Created` - Successful POST (resource created)
- `204 No Content` - Successful DELETE
- `400 Bad Request` - Validation errors, invalid input
- `401 Unauthorized` - Missing or invalid authentication
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - State conflict, invalid transition
- `500 Internal Server Error` - Unexpected server error

## Request Headers

All authenticated requests must include:
- `Authorization: Bearer <jwt-token>`

Optional headers:
- `X-Correlation-Id: <uuid>` - For distributed tracing (auto-generated if not provided)

## Pagination

For list endpoints, use query parameters:
- `page` - Page number (0-indexed, default: 0)
- `size` - Page size (default: 20, max: 100)
- `sort` - Sort field and direction (e.g., `createdAt,desc`)

Response includes pagination metadata:
```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

## Filtering

Use query parameters for filtering:
- `status` - Filter by status
- `severity` - Filter by severity (for incidents)
- `owner` - Filter by owner
- `fromDate` / `toDate` - Date range filters

## Versioning

API versioning via URL path:
- `/api/v1/evidence` - Version 1
- `/api/v2/evidence` - Version 2 (future)

Maintain backward compatibility within major versions.
