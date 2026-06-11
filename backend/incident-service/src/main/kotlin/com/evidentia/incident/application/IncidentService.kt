
package com.evidentia.incident.application

import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.Result
import com.evidentia.common.domain.TenantId
import com.evidentia.incident.domain.Incident
import com.evidentia.incident.domain.IncidentId
import com.evidentia.incident.domain.IncidentSeverity
import com.evidentia.incident.domain.IncidentStatus
import java.time.Instant

interface IncidentRepository {
    fun save(incident: Incident): Incident
    fun findById(id: IncidentId, tenantId: TenantId): Incident?
    fun findAll(tenantId: TenantId): List<Incident>
    fun findByStatus(tenantId: TenantId, status: IncidentStatus): List<Incident>
    fun findBySeverity(tenantId: TenantId, severity: IncidentSeverity): List<Incident>
}

interface AuditEventClient {
    fun save(event: AuditEvent)
}

data class CreateIncidentRequest(
    val title: String,
    val description: String,
    val severity: IncidentSeverity
)

data class EscalateIncidentRequest(
    val escalationNote: String
)

data class ResolveIncidentRequest(
    val resolutionNote: String
)

data class ReviewIncidentRequest(
    val reviewNote: String
)

sealed class IncidentError {
    data object NotFound : IncidentError()
    data class InvalidState(val message: String) : IncidentError()
    data class ValidationError(val message: String) : IncidentError()
}

class IncidentService(
    private val incidentRepository: IncidentRepository,
    private val auditEventClient: AuditEventClient
) {
    fun createIncident(
        tenantId: TenantId,
        request: CreateIncidentRequest,
        createdBy: String,
        correlationId: java.util.UUID
    ): Result<Incident, IncidentError> {
        val incident = Incident(
            id = IncidentId(),
            tenantId = tenantId,
            title = request.title,
            description = request.description,
            severity = request.severity,
            status = IncidentStatus.OPEN,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            createdBy = createdBy
        )
        
        val saved = incidentRepository.save(incident)
        
        auditEventClient.save(
            AuditEvent(
                tenantId = tenantId,
                actor = createdBy,
                action = "incident.created",
                resourceType = "Incident",
                resourceId = saved.id.value.toString(),
                correlationId = correlationId,
                metadata = mapOf(
                    "title" to request.title,
                    "severity" to request.severity.name
                )
            )
        )
        
        return Result.success(saved)
    }
    
    fun escalateIncident(
        id: IncidentId,
        tenantId: TenantId,
        request: EscalateIncidentRequest,
        actor: String,
        correlationId: java.util.UUID
    ): Result<Incident, IncidentError> {
        val incident = incidentRepository.findById(id, tenantId)
            ?: return Result.failure(IncidentError.NotFound)
        
        if (!incident.canEscalate()) {
            return Result.failure(
                IncidentError.InvalidState("Incident cannot be escalated from ${incident.status}")
            )
        }
        
        val updated = incident.copy(
            status = IncidentStatus.ESCALATED,
            escalationNote = request.escalationNote,
            updatedAt = Instant.now()
        )
        
        val saved = incidentRepository.save(updated)
        
        auditEventClient.save(
            AuditEvent(
                tenantId = tenantId,
                actor = actor,
                action = "incident.escalated",
                resourceType = "Incident",
                resourceId = saved.id.value.toString(),
                correlationId = correlationId,
                metadata = mapOf("escalationNote" to request.escalationNote)
            )
        )
        
        return Result.success(saved)
    }
    
    fun resolveIncident(
        id: IncidentId,
        tenantId: TenantId,
        request: ResolveIncidentRequest,
        resolvedBy: String,
        correlationId: java.util.UUID
    ): Result<Incident, IncidentError> {
        val incident = incidentRepository.findById(id, tenantId)
            ?: return Result.failure(IncidentError.NotFound)
        
        if (!incident.canResolve()) {
            return Result.failure(
                IncidentError.InvalidState("Incident cannot be resolved from ${incident.status}")
            )
        }
        
        val now = Instant.now()
        val updated = incident.copy(
            status = IncidentStatus.RESOLVED,
            resolvedAt = now,
            resolvedBy = resolvedBy,
            updatedAt = now
        )
        
        val saved = incidentRepository.save(updated)
        
        auditEventClient.save(
            AuditEvent(
                tenantId = tenantId,
                actor = resolvedBy,
                action = "incident.resolved",
                resourceType = "Incident",
                resourceId = saved.id.value.toString(),
                correlationId = correlationId,
                metadata = mapOf("resolutionNote" to request.resolutionNote)
            )
        )
        
        return Result.success(saved)
    }
    
    fun reviewIncident(
        id: IncidentId,
        tenantId: TenantId,
        request: ReviewIncidentRequest,
        reviewer: String,
        correlationId: java.util.UUID
    ): Result<Incident, IncidentError> {
        val incident = incidentRepository.findById(id, tenantId)
            ?: return Result.failure(IncidentError.NotFound)
        
        if (!incident.canReview()) {
            return Result.failure(
                IncidentError.InvalidState("Only resolved incidents can be reviewed")
            )
        }
        
        val now = Instant.now()
        val updated = incident.copy(
            reviewedAt = now,
            reviewedBy = reviewer,
            reviewNotes = request.reviewNote,
            updatedAt = now
        )
        
        val saved = incidentRepository.save(updated)
        
        auditEventClient.save(
            AuditEvent(
                tenantId = tenantId,
                actor = reviewer,
                action = "incident.reviewed",
                resourceType = "Incident",
                resourceId = saved.id.value.toString(),
                correlationId = correlationId,
                metadata = mapOf("reviewNote" to request.reviewNote)
            )
        )
        
        return Result.success(saved)
    }
    
    fun getIncident(id: IncidentId, tenantId: TenantId): Result<Incident, IncidentError> {
        return incidentRepository.findById(id, tenantId)
            ?.let { Result.success(it) }
            ?: Result.failure(IncidentError.NotFound)
    }
    
    fun listIncidents(tenantId: TenantId, status: IncidentStatus? = null): List<Incident> {
        return if (status != null) {
            incidentRepository.findByStatus(tenantId, status)
        } else {
            incidentRepository.findAll(tenantId)
        }
    }
    
    fun listIncidentsBySeverity(tenantId: TenantId, severity: IncidentSeverity): List<Incident> {
        return incidentRepository.findBySeverity(tenantId, severity)
    }
}
