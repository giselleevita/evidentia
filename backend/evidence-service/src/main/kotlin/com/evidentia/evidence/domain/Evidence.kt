package com.evidentia.evidence.domain

import com.evidentia.common.domain.TenantId
import java.time.Instant

data class Evidence(
    val id: EvidenceId,
    val tenantId: TenantId,
    val title: String,
    val description: String,
    val type: String,
    val sourceSystem: String,
    val owner: String,
    val approver: String? = null,
    val status: EvidenceStatus,
    val version: Int = 1,
    val createdAt: Instant,
    val updatedAt: Instant,
    val approvedAt: Instant? = null,
    val references: Map<String, String> = emptyMap(),
    val attachmentIds: List<String> = emptyList()
) {
    fun canTransitionTo(newStatus: EvidenceStatus): Boolean {
        return when (status) {
            EvidenceStatus.DRAFT -> newStatus == EvidenceStatus.IN_REVIEW
            EvidenceStatus.IN_REVIEW -> newStatus in listOf(EvidenceStatus.APPROVED, EvidenceStatus.REJECTED)
            EvidenceStatus.APPROVED -> newStatus == EvidenceStatus.LOCKED
            EvidenceStatus.REJECTED -> newStatus == EvidenceStatus.DRAFT // Can resubmit after rejection
            EvidenceStatus.LOCKED -> false // Locked evidence cannot be changed
        }
    }
    
    fun canBeEdited(): Boolean {
        return status in listOf(EvidenceStatus.DRAFT, EvidenceStatus.REJECTED)
    }
    
    fun canBeLocked(): Boolean {
        return status == EvidenceStatus.APPROVED
    }
    
    fun createNewVersion(): Evidence {
        return this.copy(
            version = version + 1,
            status = EvidenceStatus.DRAFT,
            approver = null,
            approvedAt = null,
            updatedAt = Instant.now()
        )
    }
}
