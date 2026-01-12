package com.evidentia.incident.adapters.persistence

import com.evidentia.common.domain.TenantId
import com.evidentia.incident.application.IncidentRepository
import com.evidentia.incident.domain.Incident
import com.evidentia.incident.domain.IncidentId
import com.evidentia.incident.domain.IncidentSeverity
import com.evidentia.incident.domain.IncidentStatus
import org.springframework.stereotype.Component

@Component
class IncidentRepositoryAdapter(
    private val jpaRepository: IncidentJpaRepository
) : IncidentRepository {
    override fun save(incident: Incident): Incident {
        val entity = IncidentEntity.fromDomain(incident)
        val saved = jpaRepository.save(entity)
        return saved.toDomain()
    }
    
    override fun findById(id: IncidentId, tenantId: TenantId): Incident? {
        return jpaRepository.findByIdAndTenantId(id.value, tenantId.value)
            ?.toDomain()
    }
    
    override fun findAll(tenantId: TenantId): List<Incident> {
        return jpaRepository.findByTenantId(tenantId.value)
            .map { it.toDomain() }
    }
    
    override fun findByStatus(tenantId: TenantId, status: IncidentStatus): List<Incident> {
        return jpaRepository.findByTenantIdAndStatus(tenantId.value, status)
            .map { it.toDomain() }
    }
    
    override fun findBySeverity(tenantId: TenantId, severity: IncidentSeverity): List<Incident> {
        return jpaRepository.findByTenantIdAndSeverity(tenantId.value, severity)
            .map { it.toDomain() }
    }
}
