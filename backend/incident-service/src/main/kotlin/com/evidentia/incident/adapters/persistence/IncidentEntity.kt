package com.evidentia.incident.adapters.persistence

import com.evidentia.common.domain.TenantId
import com.evidentia.incident.domain.Incident
import com.evidentia.incident.domain.IncidentId
import com.evidentia.incident.domain.IncidentSeverity
import com.evidentia.incident.domain.IncidentStatus
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "incident", indexes = [Index(name = "idx_incident_tenant_id", columnList = "tenant_id")])
data class IncidentEntity(
    @Id
    val id: UUID,
    
    @Column(name = "tenant_id", nullable = false)
    val tenantId: String,
    
    @Column(nullable = false)
    val title: String,
    
    @Column(columnDefinition = "TEXT")
    val description: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val severity: IncidentSeverity,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: IncidentStatus,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant,
    
    @Column(name = "created_by", nullable = false)
    val createdBy: String,
    
    @Column(name = "resolved_at")
    val resolvedAt: Instant? = null,
    
    @Column(name = "resolved_by")
    val resolvedBy: String? = null,
    
    @Column(name = "reviewed_at")
    val reviewedAt: Instant? = null,
    
    @Column(name = "reviewed_by")
    val reviewedBy: String? = null,
    
    @Column(name = "review_notes", columnDefinition = "TEXT")
    val reviewNotes: String? = null,
    
    @Column(name = "escalation_note", columnDefinition = "TEXT")
    val escalationNote: String? = null
) {
    fun toDomain(): Incident {
        return Incident(
            id = IncidentId(id),
            tenantId = TenantId(tenantId),
            title = title,
            description = description,
            severity = severity,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
            createdBy = createdBy,
            resolvedAt = resolvedAt,
            resolvedBy = resolvedBy,
            reviewedAt = reviewedAt,
            reviewedBy = reviewedBy,
            reviewNotes = reviewNotes,
            escalationNote = escalationNote
        )
    }
    
    companion object {
        fun fromDomain(incident: Incident): IncidentEntity {
            return IncidentEntity(
                id = incident.id.value,
                tenantId = incident.tenantId.value,
                title = incident.title,
                description = incident.description,
                severity = incident.severity,
                status = incident.status,
                createdAt = incident.createdAt,
                updatedAt = incident.updatedAt,
                createdBy = incident.createdBy,
                resolvedAt = incident.resolvedAt,
                resolvedBy = incident.resolvedBy,
                reviewedAt = incident.reviewedAt,
                reviewedBy = incident.reviewedBy,
                reviewNotes = incident.reviewNotes,
                escalationNote = incident.escalationNote
            )
        }
    }
}
