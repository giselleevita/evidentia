package com.evidentia.incident.adapters.persistence

import com.evidentia.incident.domain.IncidentSeverity
import com.evidentia.incident.domain.IncidentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface IncidentJpaRepository : JpaRepository<IncidentEntity, UUID> {
    fun findByTenantId(tenantId: String): List<IncidentEntity>
    fun findByTenantIdAndStatus(tenantId: String, status: IncidentStatus): List<IncidentEntity>
    fun findByTenantIdAndSeverity(tenantId: String, severity: IncidentSeverity): List<IncidentEntity>
    fun findByIdAndTenantId(id: UUID, tenantId: String): IncidentEntity?
}
