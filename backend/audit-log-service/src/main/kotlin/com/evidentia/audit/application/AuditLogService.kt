package com.evidentia.audit.application

import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.TenantId
import org.springframework.stereotype.Service
import java.util.UUID

interface AuditLogRepository {
    fun save(event: AuditEvent)
    fun findByTenantId(tenantId: TenantId, limit: Int = 100): List<AuditEvent>
    fun findByCorrelationId(tenantId: TenantId, correlationId: UUID): List<AuditEvent>
    fun findByResource(tenantId: TenantId, resourceType: String, resourceId: String): List<AuditEvent>
}

@Service
class AuditLogService(
    private val repository: AuditLogRepository
) {
    fun recordEvent(event: AuditEvent) {
        repository.save(event)
    }
    
    fun getAuditTrail(tenantId: TenantId, limit: Int = 100): List<AuditEvent> {
        return repository.findByTenantId(tenantId, limit)
    }
    
    fun getEventsByCorrelationId(tenantId: TenantId, correlationId: UUID): List<AuditEvent> {
        return repository.findByCorrelationId(tenantId, correlationId)
    }
    
    fun getEventsByResource(tenantId: TenantId, resourceType: String, resourceId: String): List<AuditEvent> {
        return repository.findByResource(tenantId, resourceType, resourceId)
    }
}
