package com.evidentia.audit.adapters.persistence

import com.evidentia.audit.application.AuditLogRepository
import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.TenantId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AuditEventJpaRepository : JpaRepository<AuditEventEntity, UUID> {
    fun findTop100ByTenantIdOrderByTimestampDesc(tenantId: String): List<AuditEventEntity>
    fun findByCorrelationIdOrderByTimestampAsc(correlationId: UUID): List<AuditEventEntity>
    fun findByResourceTypeAndResourceIdOrderByTimestampAsc(resourceType: String, resourceId: String): List<AuditEventEntity>
}

@Component
class AuditLogRepositoryAdapter(
    private val jpaRepository: AuditEventJpaRepository
) : AuditLogRepository {
    override fun save(event: AuditEvent) {
        jpaRepository.save(AuditEventEntity.fromDomain(event))
    }
    
    override fun findByTenantId(tenantId: TenantId, limit: Int): List<AuditEvent> {
        return jpaRepository.findTop100ByTenantIdOrderByTimestampDesc(tenantId.value)
            .take(limit)
            .map { it.toDomain() }
    }
    
    override fun findByCorrelationId(correlationId: UUID): List<AuditEvent> {
        return jpaRepository.findByCorrelationIdOrderByTimestampAsc(correlationId)
            .map { it.toDomain() }
    }
    
    override fun findByResource(resourceType: String, resourceId: String): List<AuditEvent> {
        return jpaRepository.findByResourceTypeAndResourceIdOrderByTimestampAsc(resourceType, resourceId)
            .map { it.toDomain() }
    }
}
