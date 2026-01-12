package com.evidentia.audit.application

import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.TenantId
import java.util.UUID

interface AuditLogRepository {
    fun save(event: AuditEvent)
    fun findByTenantId(tenantId: TenantId, limit: Int = 100): List<AuditEvent>
    fun findByCorrelationId(correlationId: UUID): List<AuditEvent>
    fun findByResource(resourceType: String, resourceId: String): List<AuditEvent>
}

class AuditLogService(
    private val repository: AuditLogRepository
) {
    fun recordEvent(event: AuditEvent) {
        repository.save(event)
    }
    
    fun getAuditTrail(tenantId: TenantId, limit: Int = 100): List<AuditEvent> {
        return repository.findByTenantId(tenantId, limit)
    }
    
    fun getEventsByCorrelationId(correlationId: UUID): List<AuditEvent> {
        return repository.findByCorrelationId(correlationId)
    }
    
    fun getEventsByResource(resourceType: String, resourceId: String): List<AuditEvent> {
        return repository.findByResource(resourceType, resourceId)
    }
}
