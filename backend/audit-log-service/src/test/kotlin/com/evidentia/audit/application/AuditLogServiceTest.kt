package com.evidentia.audit.application

import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.TenantId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class AuditLogServiceTest {
    private val repository = RecordingAuditRepository()
    private val service = AuditLogService(repository)

    @Test
    fun `correlation and resource queries preserve tenant boundary`() {
        val tenantId = TenantId("tenant-a")
        val correlationId = UUID.randomUUID()

        service.getEventsByCorrelationId(tenantId, correlationId)
        service.getEventsByResource(tenantId, "evidence", "item-1")

        assertEquals(tenantId to correlationId, repository.correlationQuery)
        assertEquals(Triple(tenantId, "evidence", "item-1"), repository.resourceQuery)
    }

    private class RecordingAuditRepository : AuditLogRepository {
        var correlationQuery: Pair<TenantId, UUID>? = null
        var resourceQuery: Triple<TenantId, String, String>? = null

        override fun save(event: AuditEvent) = Unit
        override fun findByTenantId(tenantId: TenantId, limit: Int) = emptyList<AuditEvent>()
        override fun findByCorrelationId(tenantId: TenantId, correlationId: UUID): List<AuditEvent> {
            correlationQuery = tenantId to correlationId
            return emptyList()
        }

        override fun findByResource(tenantId: TenantId, resourceType: String, resourceId: String): List<AuditEvent> {
            resourceQuery = Triple(tenantId, resourceType, resourceId)
            return emptyList()
        }
    }
}
