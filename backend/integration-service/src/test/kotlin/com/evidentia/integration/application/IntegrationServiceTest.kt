package com.evidentia.integration.application

import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.Result
import com.evidentia.common.domain.TenantId
import com.evidentia.integration.domain.Integration
import com.evidentia.integration.domain.IntegrationId
import com.evidentia.integration.domain.IntegrationStatus
import com.evidentia.integration.domain.IntegrationType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class IntegrationServiceTest {
    private val repository = InMemoryIntegrationRepository()
    private val auditClient = RecordingAuditClient()
    private val service = IntegrationService(repository, auditClient)
    private val tenantId = TenantId("tenant-a")
    private val correlationId = UUID.randomUUID()

    @Test
    fun `integration lifecycle persists state and emits audit events`() {
        val created = service.createIntegration(
            tenantId,
            CreateIntegrationRequest(IntegrationType.GITHUB, "GitHub", configuration = mapOf("organization" to "acme")),
            "admin@example.com",
            correlationId
        ).successValue()
        val activated = service.activateIntegration(created.id, tenantId, "admin@example.com", correlationId).successValue()
        val deactivated = service.deactivateIntegration(
            created.id,
            tenantId,
            "admin@example.com",
            correlationId
        ).successValue()
        val deleted = service.deleteIntegration(created.id, tenantId, "admin@example.com", correlationId)

        assertEquals(IntegrationStatus.ACTIVE, activated.status)
        assertEquals(IntegrationStatus.INACTIVE, deactivated.status)
        assertInstanceOf(Result.Success::class.java, deleted)
        assertTrue(repository.findById(created.id, tenantId) == null)
        assertEquals(
            listOf("integration.created", "integration.activated", "integration.deactivated", "integration.deleted"),
            auditClient.events.map { it.action }
        )
    }

    @Test
    fun `integration is not visible to another tenant`() {
        val created = service.createIntegration(
            tenantId,
            CreateIntegrationRequest(IntegrationType.JIRA, "Jira"),
            "admin@example.com",
            correlationId
        ).successValue()

        val result = service.getIntegration(created.id, TenantId("tenant-b"))

        val failure = assertInstanceOf(Result.Failure::class.java, result)
        assertEquals(IntegrationError.NotFound, failure.error)
    }

    private fun Result<Integration, IntegrationError>.successValue(): Integration =
        assertInstanceOf(Result.Success::class.java, this).value as Integration

    private class InMemoryIntegrationRepository : IntegrationRepository {
        private val integrations = mutableMapOf<Pair<IntegrationId, TenantId>, Integration>()

        override fun save(integration: Integration): Integration {
            integrations[integration.id to integration.tenantId] = integration
            return integration
        }

        override fun findById(id: IntegrationId, tenantId: TenantId): Integration? = integrations[id to tenantId]
        override fun findAll(tenantId: TenantId): List<Integration> =
            integrations.values.filter { it.tenantId == tenantId }

        override fun findByType(tenantId: TenantId, type: IntegrationType): List<Integration> =
            findAll(tenantId).filter { it.type == type }

        override fun delete(id: IntegrationId, tenantId: TenantId): Boolean =
            integrations.remove(id to tenantId) != null
    }

    private class RecordingAuditClient : AuditEventClient {
        val events = mutableListOf<AuditEvent>()

        override fun save(event: AuditEvent) {
            events += event
        }
    }
}
