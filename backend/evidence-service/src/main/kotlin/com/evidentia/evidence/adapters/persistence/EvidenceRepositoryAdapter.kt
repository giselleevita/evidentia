package com.evidentia.evidence.adapters.persistence

import com.evidentia.common.domain.TenantId
import com.evidentia.evidence.application.EvidenceRepository
import com.evidentia.evidence.domain.Evidence
import com.evidentia.evidence.domain.EvidenceId
import org.springframework.stereotype.Component

@Component
class EvidenceRepositoryAdapter(
    private val jpaRepository: EvidenceJpaRepository
) : EvidenceRepository {
    override fun save(evidence: Evidence): Evidence {
        val entity = EvidenceEntity.fromDomain(evidence)
        val saved = jpaRepository.save(entity)
        return saved.toDomain()
    }
    
    override fun findById(id: EvidenceId, tenantId: TenantId): Evidence? {
        return jpaRepository.findByIdAndTenantId(id.value, tenantId.value)
            ?.toDomain()
    }
    
    override fun findAll(tenantId: TenantId): List<Evidence> {
        return jpaRepository.findByTenantId(tenantId.value)
            .map { it.toDomain() }
    }
    
    override fun findByOwner(tenantId: TenantId, owner: String): List<Evidence> {
        return jpaRepository.findByTenantIdAndOwner(tenantId.value, owner)
            .map { it.toDomain() }
    }
    
    override fun delete(id: EvidenceId, tenantId: TenantId): Boolean {
        val entity = jpaRepository.findByIdAndTenantId(id.value, tenantId.value)
            ?: return false
        jpaRepository.delete(entity)
        return true
    }
}
