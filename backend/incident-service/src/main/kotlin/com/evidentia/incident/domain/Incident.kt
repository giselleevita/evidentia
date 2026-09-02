package com.evidentia.incident.domain

import com.evidentia.common.domain.TenantId
import java.time.Instant

data class Incident(
    val id: IncidentId,
    val tenantId: TenantId,
    val title: String,
    val description: String,
    val severity: IncidentSeverity,
    val status: IncidentStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val createdBy: String,
    val resolvedAt: Instant? = null,
    val resolvedBy: String? = null,
    val reviewedAt: Instant? = null,
    val reviewedBy: String? = null,
    val reviewNotes: String? = null,
    val escalationNote: String? = null
) {
    fun canEscalate(): Boolean {
        return status == IncidentStatus.OPEN
    }
    
    fun canResolve(): Boolean {
        return status in listOf(IncidentStatus.OPEN, IncidentStatus.ESCALATED)
    }
    
    fun canReview(): Boolean {
        return status == IncidentStatus.RESOLVED
    }
    
    fun canClose(): Boolean {
        return status == IncidentStatus.RESOLVED && reviewedAt != null
    }
}
