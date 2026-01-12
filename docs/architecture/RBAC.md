# Role-Based Access Control (RBAC)

## Overview

The platform implements role-based access control using Azure Entra ID (Azure AD) app roles. Each user is assigned roles that determine their permissions within the platform.

## Roles

### Admin
**Full administrative access**
- Create, update, delete evidence and incidents
- Escalate incidents
- Resolve incidents
- Lock evidence
- View all audit logs
- Manage integrations

### Auditor
**Compliance and audit responsibilities**
- View all evidence and incidents (read-only access to most resources)
- Approve evidence (change status from IN_REVIEW to APPROVED)
- Review resolved incidents (add review notes and close)
- Query audit logs
- Cannot create or modify most resources

### User
**Standard user access**
- Create evidence and incidents
- Submit evidence for review
- View own evidence and incidents
- Cannot approve evidence or resolve incidents

## Implementation

### Azure AD Configuration

1. **App Registration**: Register the application in Azure AD as multi-tenant
2. **App Roles**: Define roles in the app manifest:
   ```json
   "appRoles": [
     {
       "allowedMemberTypes": ["User"],
       "displayName": "Admin",
       "id": "...",
       "isEnabled": true,
       "value": "Admin"
     },
     {
       "allowedMemberTypes": ["User"],
       "displayName": "Auditor",
       "id": "...",
       "isEnabled": true,
       "value": "Auditor"
     },
     {
       "allowedMemberTypes": ["User"],
       "displayName": "User",
       "id": "...",
       "isEnabled": true,
       "value": "User"
     }
   ]
   ```
3. **Role Assignment**: Assign roles to users or groups in each tenant

### Backend Enforcement

Roles are enforced using Spring Security's `@PreAuthorize` annotations:

```kotlin
@PostMapping("/{id}/approve")
@PreAuthorize("hasRole('Auditor')")
fun approveEvidence(...) { ... }

@PostMapping("/{id}/escalate")
@PreAuthorize("hasRole('Admin')")
fun escalateIncident(...) { ... }
```

### Multi-Tenant Isolation

Roles are scoped per tenant:
- A user with Admin role in Tenant A has no access to Tenant B's data
- Tenant ID is extracted from JWT token (`tid` claim)
- All queries automatically filter by tenant ID

## Security Best Practices

1. **Principle of Least Privilege**: Users should have the minimum role necessary
2. **Regular Audits**: Review role assignments periodically
3. **Separation of Duties**: Auditors should not be able to create/modify evidence they approve
4. **Audit Logging**: All role-based actions are logged for compliance
