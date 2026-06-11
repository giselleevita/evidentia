package com.evidentia.evidence.application

import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.Result
import com.evidentia.common.domain.TenantId
import com.evidentia.evidence.domain.Evidence
import com.evidentia.evidence.domain.EvidenceId
import com.evidentia.evidence.domain.EvidenceStatus
import java.time.Instant

interface EvidenceRepository {
    fun save(evidence: Evidence): Evidence
    fun findById(id: EvidenceId, tenantId: TenantId): Evidence?
    fun findAll(tenantId: TenantId): List<Evidence>
    fun findByOwner(tenantId: TenantId, owner: String): List<Evidence>
    fun delete(id: EvidenceId, tenantId: TenantId): Boolean
}

interface AuditEventClient {
    fun save(event: AuditEvent)
}

data class CreateEvidenceRequest(
    val title: String,
    val description: String,
    val type: String,
    val sourceSystem: String,
    val owner: String,
    val references: Map<String, String> = emptyMap()
)

data class UpdateEvidenceRequest(
    val title: String? = null,
    val description: String? = null,
    val type: String? = null,
    val sourceSystem: String? = null,
    val references: Map<String, String>? = null
)

data class SubmitEvidenceRequest(
    val note: String? = null
)

data class ApproveEvidenceRequest(
    val note: String? = null
)

data class RejectEvidenceRequest(
    val reason: String
)

sealed class EvidenceError {
    data object NotFound : EvidenceError()
    data class InvalidTransition(val from: EvidenceStatus, val to: EvidenceStatus) : EvidenceError()
    data class ValidationError(val message: String) : EvidenceError()
}

class EvidenceService(
    private val evidenceRepository: EvidenceRepository,
    private val auditEventClient: AuditEventClient
) {
    fun createEvidence(
        tenantId: TenantId,
        request: CreateEvidenceRequest,
        actor: String,
        correlationId: java.util.UUID
    ): Result<Evidence, EvidenceError> {
        val evidence = Evidence(
            id = EvidenceId(),
            tenantId = tenantId,
            title = request.title,
            description = request.description,
            type = request.type,
            sourceSystem = request.sourceSystem,
            owner = request.owner,
            status = EvidenceStatus.DRAFT,
            version = 1,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            references = request.references
        )
        
        val saved = evidenceRepository.save(evidence)
        
        auditEventClient.save(
            AuditEvent(
                tenantId = tenantId,
                actor = actor,
                action = "evidence.created",
                resourceType = "Evidence",
                resourceId = saved.id.value.toString(),
                correlationId = correlationId,
                metadata = mapOf(
                    "title" to request.title,
                    "type" to request.type,
                    "sourceSystem" to request.sourceSystem,
                    "version" to "1"
                )
            )
        )
        
        return Result.success(saved)
    }
    
    fun updateEvidence(
        id: EvidenceId,
        tenantId: TenantId,
        request: UpdateEvidenceRequest,
        actor: String,
        correlationId: java.util.UUID
    ): Result<Evidence, EvidenceError> {
        val evidence = evidenceRepository.findById(id, tenantId)
            ?: return Result.failure(EvidenceError.NotFound)
        
        if (!evidence.canBeEdited()) {
            return Result.failure(
                EvidenceError.ValidationError("Evidence cannot be edited in ${evidence.status} state")
            )
        }
        
        val updated = evidence.copy(
            title = request.title ?: evidence.title,
            description = request.description ?: evidence.description,
            type = request.type ?: evidence.type,
            sourceSystem = request.sourceSystem ?: evidence.sourceSystem,
            references = request.references ?: evidence.references,
            updatedAt = Instant.now()
        )
        
        val saved = evidenceRepository.save(updated)
        
        auditEventClient.save(
            AuditEvent(
                tenantId = tenantId,
                actor = actor,
                action = "evidence.updated",
                resourceType = "Evidence",
                resourceId = saved.id.value.toString(),
                correlationId = correlationId,
                metadata = mapOf("version" to saved.version.toString())
            )
        )
        
        return Result.success(saved)
    }
    
    fun submitForReview(
        id: EvidenceId,
        tenantId: TenantId,
        request: SubmitEvidenceRequest,
        actor: String,
        correlationId: java.util.UUID
    ): Result<Evidence, EvidenceError> {
        val evidence = evidenceRepository.findById(id, tenantId)
            ?: return Result.failure(EvidenceError.NotFound)
        
        if (!evidence.canTransitionTo(EvidenceStatus.IN_REVIEW)) {
            return Result.failure(
                EvidenceError.InvalidTransition(evidence.status, EvidenceStatus.IN_REVIEW)
            )
        }
        
        val updated = evidence.copy(
            status = EvidenceStatus.IN_REVIEW,
            updatedAt = Instant.now()
        )
        
        val saved = evidenceRepository.save(updated)
        
        auditEventClient.save(
            AuditEvent(
                tenantId = tenantId,
                actor = actor,
                action = "evidence.submitted",
                resourceType = "Evidence",
                resourceId = saved.id.value.toString(),
                correlationId = correlationId,
                metadata = mapOf("note" to (request.note ?: ""))
            )
        )
        
        return Result.success(saved)
    }
    
    fun approveEvidence(
        id: EvidenceId,
        tenantId: TenantId,
        request: ApproveEvidenceRequest,
        approver: String,
        correlationId: java.util.UUID
    ): Result<Evidence, EvidenceError> {
        val evidence = evidenceRepository.findById(id, tenantId)
            ?: return Result.failure(EvidenceError.NotFound)
        
        if (!evidence.canTransitionTo(EvidenceStatus.APPROVED)) {
            return Result.failure(
                EvidenceError.InvalidTransition(evidence.status, EvidenceStatus.APPROVED)
            )
        }
        
        val now = Instant.now()
        val updated = evidence.copy(
            status = EvidenceStatus.APPROVED,
            approver = approver,
            approvedAt = now,
            updatedAt = now
        )
        
        val saved = evidenceRepository.save(updated)
        
        auditEventClient.save(
            AuditEvent(
                tenantId = tenantId,
                actor = approver,
                action = "evidence.approved",
                resourceType = "Evidence",
                resourceId = saved.id.value.toString(),
                correlationId = correlationId,
                metadata = mapOf(
                    "note" to (request.note ?: ""),
                    "version" to saved.version.toString()
                )
            )
        )
        
        return Result.success(saved)
    }
    
    fun rejectEvidence(
        id: EvidenceId,
        tenantId: TenantId,
        request: RejectEvidenceRequest,
        actor: String,
        correlationId: java.util.UUID
    ): Result<Evidence, EvidenceError> {
        val evidence = evidenceRepository.findById(id, tenantId)
            ?: return Result.failure(EvidenceError.NotFound)
        
        if (!evidence.canTransitionTo(EvidenceStatus.REJECTED)) {
            return Result.failure(
                EvidenceError.InvalidTransition(evidence.status, EvidenceStatus.REJECTED)
            )
        }
        
        val updated = evidence.copy(
            status = EvidenceStatus.REJECTED,
            updatedAt = Instant.now()
        )
        
        val saved = evidenceRepository.save(updated)
        
        auditEventClient.save(
            AuditEvent(
                tenantId = tenantId,
                actor = actor,
                action = "evidence.rejected",
                resourceType = "Evidence",
                resourceId = saved.id.value.toString(),
                correlationId = correlationId,
                metadata = mapOf("reason" to request.reason)
            )
        )
        
        return Result.success(saved)
    }
    
    fun lockEvidence(
        id: EvidenceId,
        tenantId: TenantId,
        actor: String,
        correlationId: java.util.UUID
    ): Result<Evidence, EvidenceError> {
        val evidence = evidenceRepository.findById(id, tenantId)
            ?: return Result.failure(EvidenceError.NotFound)
        
        if (!evidence.canBeLocked()) {
            return Result.failure(
                EvidenceError.ValidationError("Only approved evidence can be locked")
            )
        }
        
        val updated = evidence.copy(
            status = EvidenceStatus.LOCKED,
            updatedAt = Instant.now()
        )
        
        val saved = evidenceRepository.save(updated)
        
        auditEventClient.save(
            AuditEvent(
                tenantId = tenantId,
                actor = actor,
                action = "evidence.locked",
                resourceType = "Evidence",
                resourceId = saved.id.value.toString(),
                correlationId = correlationId,
                metadata = mapOf("version" to saved.version.toString())
            )
        )
        
        return Result.success(saved)
    }
    
    fun getEvidence(id: EvidenceId, tenantId: TenantId): Result<Evidence, EvidenceError> {
        return evidenceRepository.findById(id, tenantId)
            ?.let { Result.success(it) }
            ?: Result.failure(EvidenceError.NotFound)
    }
    
    fun listEvidence(tenantId: TenantId, status: EvidenceStatus? = null): List<Evidence> {
        val all = evidenceRepository.findAll(tenantId)
        return if (status != null) {
            all.filter { it.status == status }
        } else {
            all
        }
    }
    
    fun findByOwner(tenantId: TenantId, owner: String): List<Evidence> {
        return evidenceRepository.findByOwner(tenantId, owner)
    }
}
