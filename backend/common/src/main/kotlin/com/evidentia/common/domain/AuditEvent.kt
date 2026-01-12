package com.evidentia.common.domain

import java.time.Instant
import java.util.UUID

data class AuditEvent(
    val id: UUID = UUID.randomUUID(),
    val tenantId: TenantId,
    val actor: String,
    val action: String,
    val resourceType: String,
    val resourceId: String,
    val correlationId: UUID,
    val timestamp: Instant = Instant.now(),
    val metadata: Map<String, String> = emptyMap()
)
