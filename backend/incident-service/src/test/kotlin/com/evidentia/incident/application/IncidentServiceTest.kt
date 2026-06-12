package com.evidentia.incident.application

import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.Result
import com.evidentia.common.domain.TenantId
import com.evidentia.incident.domain.Incident
import com.evidentia.incident.domain.IncidentId
import com.evidentia.incident.domain.IncidentSeverity
import com.evidentia.incident.domain.IncidentStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class IncidentServiceTest {
    private val repository = InMemoryIncidentRepository()
    private val auditClient = RecordingAuditClient()
    private val service = IncidentService(repository, auditClient)
    private val tenantId = TenantId("tenant-a")
    private val correlationId = UUID.randomUUID()

    @Test
    fun `incident follows escalation resolution and review lifecycle`() {
        val created = service.createIncident(
            tenantId,
            CreateIncidentRequest("Unauthorized access", "Investigate access", IncidentSeverity.HIGH),
            "reporter@example.com",
            correlationId
        ).successValue()
        val escalated = service.escalateIncident(
            created.id,
            tenantId,
            EscalateIncidentRequest("Executive notification required"),
            "analyst@example.com",
            correlationId
        ).successValue()
        val resolved = service.resolveIncident(
            created.id,
            tenantId,
            ResolveIncidentRequest("Credentials revoked"),
            "analyst@example.com",
            correlationId
        ).successValue()
        val reviewed = service.reviewIncident(
            created.id,
            tenantId,
            ReviewIncidentRequest("Remediation verified"),
            "reviewer@example.com",
            correlationId
        ).successValue()

        assertEquals(IncidentStatus.ESCALATED, escalated.status)
        assertEquals(IncidentStatus.RESOLVED, resolved.status)
        assertEquals("analyst@example.com", resolved.resolvedBy)
        assertTrue(reviewed.canClose())
        assertEquals(
            listOf("incident.created", "incident.escalated", "incident.resolved", "incident.reviewed"),
            auditClient.events.map { it.action }
        )
    }

    @Test
    fun `resolved incident cannot be escalated`() {
        val created = service.createIncident(
            tenantId,
            CreateIncidentRequest("Service issue", "Investigate", IncidentSeverity.MEDIUM),
            "reporter@example.com",
            correlationId
        ).successValue()
        service.resolveIncident(
            created.id,
            tenantId,
            ResolveIncidentRequest("Resolved"),
            "analyst@example.com",
            correlationId
        )

        val result = service.escalateIncident(
            created.id,
            tenantId,
            EscalateIncidentRequest("Too late"),
            "analyst@example.com",
            correlationId
        )

        val failure = assertInstanceOf(Result.Failure::class.java, result)
        assertInstanceOf(IncidentError.InvalidState::class.java, failure.error)
    }

    @Test
    fun `incident is not visible or mutable through another tenant`() {
        val created = service.createIncident(
            tenantId,
            CreateIncidentRequest("Unauthorized access", "Investigate access", IncidentSeverity.HIGH),
            "reporter@example.com",
            correlationId
        ).successValue()
        val otherTenant = TenantId("tenant-b")

        val read = service.getIncident(created.id, otherTenant)
        val escalation = service.escalateIncident(
            created.id,
            otherTenant,
            EscalateIncidentRequest("Spoofed escalation"),
            "attacker@example.com",
            correlationId,
        )

        assertEquals(IncidentError.NotFound, assertInstanceOf(Result.Failure::class.java, read).error)
        assertEquals(IncidentError.NotFound, assertInstanceOf(Result.Failure::class.java, escalation).error)
    }

    private fun Result<Incident, IncidentError>.successValue(): Incident =
        assertInstanceOf(Result.Success::class.java, this).value as Incident

    private class InMemoryIncidentRepository : IncidentRepository {
        private val incidents = mutableMapOf<Pair<IncidentId, TenantId>, Incident>()

        override fun save(incident: Incident): Incident {
            incidents[incident.id to incident.tenantId] = incident
            return incident
        }

        override fun findById(id: IncidentId, tenantId: TenantId): Incident? = incidents[id to tenantId]
        override fun findAll(tenantId: TenantId): List<Incident> = incidents.values.filter { it.tenantId == tenantId }
        override fun findByStatus(tenantId: TenantId, status: IncidentStatus): List<Incident> =
            findAll(tenantId).filter { it.status == status }

        override fun findBySeverity(tenantId: TenantId, severity: IncidentSeverity): List<Incident> =
            findAll(tenantId).filter { it.severity == severity }
    }

    private class RecordingAuditClient : AuditEventClient {
        val events = mutableListOf<AuditEvent>()

        override fun save(event: AuditEvent) {
            events += event
        }
    }
}
