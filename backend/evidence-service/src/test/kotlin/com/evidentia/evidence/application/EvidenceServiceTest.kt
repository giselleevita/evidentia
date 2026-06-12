package com.evidentia.evidence.application

import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.Result
import com.evidentia.common.domain.TenantId
import com.evidentia.evidence.domain.Evidence
import com.evidentia.evidence.domain.EvidenceId
import com.evidentia.evidence.domain.EvidenceStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class EvidenceServiceTest {
    private val repository = InMemoryEvidenceRepository()
    private val auditClient = RecordingAuditClient()
    private val service = EvidenceService(repository, auditClient)
    private val tenantId = TenantId("tenant-a")
    private val correlationId = UUID.randomUUID()

    @Test
    fun `evidence follows the approval and locking lifecycle with an audit trail`() {
        val created = service.createEvidence(
            tenantId,
            CreateEvidenceRequest("Access review", "Quarterly review", "control", "IAM", "owner@example.com"),
            "owner@example.com",
            correlationId
        ).successValue()

        val submitted = service.submitForReview(
            created.id,
            tenantId,
            SubmitEvidenceRequest("Ready"),
            "owner@example.com",
            correlationId
        ).successValue()
        val approved = service.approveEvidence(
            created.id,
            tenantId,
            ApproveEvidenceRequest("Verified"),
            "approver@example.com",
            correlationId
        ).successValue()
        val locked = service.lockEvidence(created.id, tenantId, "approver@example.com", correlationId).successValue()

        assertEquals(EvidenceStatus.IN_REVIEW, submitted.status)
        assertEquals(EvidenceStatus.APPROVED, approved.status)
        assertEquals("approver@example.com", approved.approver)
        assertTrue(approved.approvedAt != null)
        assertEquals(EvidenceStatus.LOCKED, locked.status)
        assertEquals(
            listOf("evidence.created", "evidence.submitted", "evidence.approved", "evidence.locked"),
            auditClient.events.map { it.action }
        )
    }

    @Test
    fun `draft evidence cannot be approved and produces no audit event`() {
        val created = service.createEvidence(
            tenantId,
            CreateEvidenceRequest("Policy", "Policy evidence", "document", "GRC", "owner@example.com"),
            "owner@example.com",
            correlationId
        ).successValue()
        auditClient.events.clear()

        val result = service.approveEvidence(
            created.id,
            tenantId,
            ApproveEvidenceRequest(),
            "approver@example.com",
            correlationId
        )

        val failure = assertInstanceOf(Result.Failure::class.java, result)
        assertEquals(
            EvidenceError.InvalidTransition(EvidenceStatus.DRAFT, EvidenceStatus.APPROVED),
            failure.error
        )
        assertTrue(auditClient.events.isEmpty())
    }

    @Test
    fun `evidence is not visible or mutable through another tenant`() {
        val created = service.createEvidence(
            tenantId,
            CreateEvidenceRequest("Policy", "Policy evidence", "document", "GRC", "owner@example.com"),
            "owner@example.com",
            correlationId
        ).successValue()
        val otherTenant = TenantId("tenant-b")

        val read = service.getEvidence(created.id, otherTenant)
        val update = service.updateEvidence(
            created.id,
            otherTenant,
            UpdateEvidenceRequest(title = "Spoofed"),
            "attacker@example.com",
            correlationId,
        )

        assertEquals(EvidenceError.NotFound, assertInstanceOf(Result.Failure::class.java, read).error)
        assertEquals(EvidenceError.NotFound, assertInstanceOf(Result.Failure::class.java, update).error)
    }

    private fun Result<Evidence, EvidenceError>.successValue(): Evidence =
        assertInstanceOf(Result.Success::class.java, this).value as Evidence

    private class InMemoryEvidenceRepository : EvidenceRepository {
        private val evidence = mutableMapOf<Pair<EvidenceId, TenantId>, Evidence>()

        override fun save(evidence: Evidence): Evidence {
            this.evidence[evidence.id to evidence.tenantId] = evidence
            return evidence
        }

        override fun findById(id: EvidenceId, tenantId: TenantId): Evidence? = evidence[id to tenantId]

        override fun findAll(tenantId: TenantId): List<Evidence> =
            evidence.values.filter { it.tenantId == tenantId }

        override fun findByOwner(tenantId: TenantId, owner: String): List<Evidence> =
            findAll(tenantId).filter { it.owner == owner }

        override fun delete(id: EvidenceId, tenantId: TenantId): Boolean = evidence.remove(id to tenantId) != null
    }

    private class RecordingAuditClient : AuditEventClient {
        val events = mutableListOf<AuditEvent>()

        override fun save(event: AuditEvent) {
            events += event
        }
    }
}
