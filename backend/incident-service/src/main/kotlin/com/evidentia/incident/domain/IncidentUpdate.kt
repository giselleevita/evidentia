package com.evidentia.incident.domain

import java.time.Instant
import java.util.UUID

data class IncidentUpdate(
    val id: UUID = UUID.randomUUID(),
    val incidentId: IncidentId,
    val timestamp: Instant = Instant.now(),
    val user: String,
    val action: String,
    val notes: String? = null
)
