package com.evidentia.integration.domain

import com.evidentia.common.domain.TenantId
import java.time.Instant

data class Integration(
    val id: IntegrationId,
    val tenantId: TenantId,
    val type: IntegrationType,
    val name: String,
    val description: String? = null,
    val status: IntegrationStatus,
    val configuration: Map<String, String> = emptyMap(),
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastSyncAt: Instant? = null,
    val errorMessage: String? = null
)
