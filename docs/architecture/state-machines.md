# State Machines

## Evidence Lifecycle

```
DRAFT → IN_REVIEW → APPROVED → LOCKED
  ↓                    ↓
REJECTED ──────────────┘
  ↓
DRAFT (can resubmit)
```

### State Transitions

- **DRAFT → IN_REVIEW**: User submits evidence for review
- **IN_REVIEW → APPROVED**: Auditor approves evidence
- **IN_REVIEW → REJECTED**: Auditor rejects evidence
- **REJECTED → DRAFT**: User can resubmit after rejection
- **APPROVED → LOCKED**: Admin/Auditor locks evidence (immutable)

### Rules

- Only DRAFT or REJECTED evidence can be edited
- Only APPROVED evidence can be locked
- LOCKED evidence cannot be modified

## Incident Lifecycle

```
OPEN → ESCALATED → RESOLVED → CLOSED
  ↓         ↓
RESOLVED ───┘
```

### State Transitions

- **OPEN → ESCALATED**: Admin escalates incident
- **OPEN → RESOLVED**: Admin resolves incident directly
- **ESCALATED → RESOLVED**: Admin resolves escalated incident
- **RESOLVED → CLOSED**: Auditor reviews and closes incident

### Rules

- Only OPEN or ESCALATED incidents can be resolved
- Only RESOLVED incidents can be reviewed
- CLOSED incidents are immutable

## Implementation

State transitions are enforced in domain models:

```kotlin
fun canTransitionTo(newStatus: EvidenceStatus): Boolean {
    return when (status) {
        EvidenceStatus.DRAFT -> newStatus == EvidenceStatus.IN_REVIEW
        EvidenceStatus.IN_REVIEW -> newStatus in listOf(APPROVED, REJECTED)
        // ...
    }
}
```

Invalid transitions return `InvalidTransition` errors.
