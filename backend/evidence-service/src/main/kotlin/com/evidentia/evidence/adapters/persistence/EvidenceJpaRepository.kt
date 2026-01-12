package com.evidentia.evidence.adapters.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface EvidenceJpaRepository : JpaRepository<EvidenceEntity, UUID> {
    fun findByTenantId(tenantId: String): List<EvidenceEntity>
    fun findByTenantIdAndStatus(tenantId: String, status: EvidenceStatus): List<EvidenceEntity>
    fun findByTenantIdAndOwner(tenantId: String, owner: String): List<EvidenceEntity>
    fun findByIdAndTenantId(id: UUID, tenantId: String): EvidenceEntity?
}
