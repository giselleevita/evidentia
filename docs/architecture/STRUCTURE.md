# Project Structure Guide

This document describes the improved enterprise-grade structure of the Evidentia platform.

## Backend Structure

### Common Module (`backend/common/`)

The common module provides shared infrastructure and utilities used across all services:

```
common/
├── domain/              # Shared domain models
│   ├── TenantId.kt
│   └── AuditEvent.kt
├── context/            # Context management
│   └── TenantContext.kt
├── security/            # Security configuration
│   └── SecurityConfig.kt
├── web/                 # Web layer utilities
│   ├── ApiResponse.kt      # Standardized API responses
│   ├── GlobalExceptionHandler.kt  # Centralized exception handling
│   ├── TenantFilter.kt      # Tenant extraction filter
│   ├── RequestLoggingInterceptor.kt  # Request logging
│   ├── WebConfig.kt         # Web configuration
│   └── JwtUtils.kt          # JWT utility functions
└── validation/         # Custom validators
    └── ValidTenantId.kt
```

### Service Module Structure

Each service follows a consistent hexagonal architecture:

```
{service}/
├── domain/              # Domain models (pure business logic)
│   ├── {Entity}Id.kt
│   ├── {Entity}Status.kt (enums)
│   └── {Entity}.kt
├── application/         # Use cases and business logic
│   ├── {Entity}Service.kt
│   └── {Entity}Repository.kt (interface)
├── adapters/
│   ├── persistence/     # Database adapters
│   │   ├── {Entity}Entity.kt
│   │   ├── {Entity}JpaRepository.kt
│   │   └── {Entity}RepositoryAdapter.kt
│   ├── web/            # REST API adapters
│   │   ├── dto/        # Data Transfer Objects
│   │   │   └── {Entity}Dto.kt
│   │   ├── mapper/     # Domain-to-DTO mappers
│   │   │   └── {Entity}Mapper.kt
│   │   └── {Entity}Controller.kt
│   └── audit/          # Audit logging adapters
│       └── AuditEventClientAdapter.kt
└── resources/
    ├── application.yml
    └── db/migration/
```

## Key Improvements

### 1. Separated Concerns

- **DTOs** are in dedicated `dto/` packages, not mixed with controllers
- **Mappers** handle domain-to-DTO conversion in separate classes
- **Validation** is applied at the DTO level with Jakarta Validation

### 2. Standardized API Responses

All endpoints return `ApiResponse<T>` wrapper:
```kotlin
{
  "success": true,
  "data": { ... },
  "error": null,
  "timestamp": "2024-01-08T12:00:00Z"
}
```

### 3. Centralized Exception Handling

- `GlobalExceptionHandler` catches all exceptions
- Consistent error response format
- Proper HTTP status codes
- Detailed error messages for validation failures

### 4. Request Logging & Tracing

- `RequestLoggingInterceptor` logs all requests
- Correlation IDs for distributed tracing
- MDC context for log correlation

### 5. Tenant Context Management

- `TenantFilter` extracts tenant ID from JWT
- `TenantContext` provides thread-local tenant access
- Automatic tenant filtering in all queries

### 6. JWT Utilities

- `JwtUtils` provides reusable JWT extraction functions
- Consistent actor and tenant ID extraction
- Used across all controllers

## Best Practices

### DTOs
- Use validation annotations (`@NotBlank`, `@Size`, etc.)
- Keep DTOs separate from domain models
- Use mappers for conversion, not inline logic

### Controllers
- Keep controllers thin - delegate to services
- Use `@Valid` for request validation
- Return `ApiResponse<T>` for consistency
- Extract JWT info using `JwtUtils`

### Services
- Pure business logic, no framework dependencies
- Use `Result<T, E>` for error handling
- Call audit service for all state changes

### Repositories
- Interface in application layer
- Implementation in adapters/persistence
- Always filter by tenantId

## Migration Guide

When refactoring existing controllers:

1. Move DTOs to `adapters/web/dto/` package
2. Create mapper in `adapters/web/mapper/` package
3. Update controller to use `ApiResponse<T>`
4. Add `@Valid` annotations to request DTOs
5. Use `JwtUtils` for JWT extraction
6. Remove inline error handling (use GlobalExceptionHandler)
