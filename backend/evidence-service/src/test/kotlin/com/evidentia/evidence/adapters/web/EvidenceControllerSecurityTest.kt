package com.evidentia.evidence.adapters.web

import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.TenantId
import com.evidentia.common.security.SecurityConfig
import com.evidentia.common.web.RateLimitFilter
import com.evidentia.common.web.TenantFilter
import com.evidentia.evidence.application.AuditEventClient
import com.evidentia.evidence.application.EvidenceRepository
import com.evidentia.evidence.application.EvidenceService
import com.evidentia.evidence.domain.Evidence
import com.evidentia.evidence.domain.EvidenceId
import com.evidentia.evidence.domain.EvidenceStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.util.UUID

@WebMvcTest(EvidenceController::class)
@Import(
    SecurityConfig::class,
    TenantFilter::class,
    RateLimitFilter::class,
    EvidenceControllerSecurityTest.TestBeans::class,
)
class EvidenceControllerSecurityTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var repository: RecordingEvidenceRepository

    @Autowired
    private lateinit var auditClient: RecordingAuditClient

    @AfterEach
    fun resetState() {
        repository.clear()
        auditClient.clear()
    }

    @Test
    fun `list evidence requires authentication`() {
        mockMvc.get("/api/v1/evidence")
            .andExpect {
                status { isUnauthorized() }
            }

        assertEquals(emptyList<TenantId>(), repository.findAllTenants)
    }

    @Test
    fun `authenticated evidence requests require a tenant claim`() {
        mockMvc.get("/api/v1/evidence") {
            with(jwt().authorities(SimpleGrantedAuthority("ROLE_User")))
        }.andExpect {
            status { isForbidden() }
        }

        assertEquals(emptyList<TenantId>(), repository.findAllTenants)
    }

    @Test
    fun `service role cannot access user evidence endpoints`() {
        mockMvc.get("/api/v1/evidence") {
            with(jwtFor("Service"))
        }.andExpect {
            status { isForbidden() }
        }

        assertEquals(emptyList<TenantId>(), repository.findAllTenants)
    }

    @Test
    fun `tenant header mismatch is rejected before evidence service access`() {
        mockMvc.get("/api/v1/evidence") {
            with(jwtFor("User", tenantId = "tenant-a"))
            header("X-Tenant-Id", "tenant-b")
        }.andExpect {
            status { isForbidden() }
        }

        assertEquals(emptyList<TenantId>(), repository.findAllTenants)
    }

    @Test
    fun `user role cannot lock evidence`() {
        val evidenceId = UUID.randomUUID()

        mockMvc.post("/api/v1/evidence/$evidenceId/lock") {
            with(jwtFor("User"))
        }.andExpect {
            status { isForbidden() }
        }

        assertEquals(0, repository.findByIdCalls)
    }

    @Test
    fun `admin role can approve evidence for the validated tenant`() {
        val evidenceId = UUID.randomUUID()
        repository.seed(evidenceFor(evidenceId, TenantId("tenant-a"), EvidenceStatus.IN_REVIEW))

        mockMvc.post("/api/v1/evidence/$evidenceId/approve") {
            with(jwtFor("Admin", subject = "admin@example.com"))
            contentType = MediaType.APPLICATION_JSON
            content = """{"note":"approved"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.tenantId") { value("tenant-a") }
            jsonPath("$.data.status") { value("APPROVED") }
            jsonPath("$.data.approver") { value("admin@example.com") }
        }

        val approved = repository.get(EvidenceId(evidenceId), TenantId("tenant-a"))
        assertEquals(EvidenceStatus.APPROVED, approved?.status)
        assertEquals("admin@example.com", approved?.approver)
        assertEquals(listOf("evidence.approved"), auditClient.events.map { it.action })
        assertEquals(listOf(TenantId("tenant-a")), repository.findByIdTenants)
    }

    @Test
    fun `list evidence uses only the validated token tenant`() {
        repository.seed(evidenceFor(UUID.randomUUID(), TenantId("tenant-a"), EvidenceStatus.DRAFT))
        repository.seed(evidenceFor(UUID.randomUUID(), TenantId("tenant-b"), EvidenceStatus.DRAFT))

        mockMvc.get("/api/v1/evidence") {
            with(jwtFor("User", tenantId = "tenant-a"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.length()") { value(1) }
            jsonPath("$.data[0].tenantId") { value("tenant-a") }
        }

        assertEquals(listOf(TenantId("tenant-a")), repository.findAllTenants)
    }

    private fun jwtFor(
        role: String,
        tenantId: String = "tenant-a",
        subject: String = "${role.lowercase()}@example.com",
    ): JwtRequestPostProcessor =
        jwt()
            .jwt {
                it.subject(subject)
                    .claim("tid", tenantId)
                    .claim("roles", listOf(role))
            }
            .authorities(SimpleGrantedAuthority("ROLE_$role"))

    private fun evidenceFor(
        id: UUID,
        tenantId: TenantId,
        status: EvidenceStatus,
        approver: String? = null,
    ): Evidence {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        return Evidence(
            id = EvidenceId(id),
            tenantId = tenantId,
            title = "Access Review",
            description = "Quarterly access review",
            type = "control",
            sourceSystem = "IAM",
            owner = "owner@example.com",
            approver = approver,
            status = status,
            createdAt = now,
            updatedAt = now,
            approvedAt = if (status == EvidenceStatus.APPROVED) now else null,
        )
    }

    @TestConfiguration
    class TestBeans {
        @Bean
        fun evidenceRepository(): RecordingEvidenceRepository = RecordingEvidenceRepository()

        @Bean
        fun auditEventClient(): RecordingAuditClient = RecordingAuditClient()

        @Bean
        fun evidenceService(
            repository: EvidenceRepository,
            auditEventClient: AuditEventClient,
        ): EvidenceService = EvidenceService(repository, auditEventClient)
    }

    class RecordingEvidenceRepository : EvidenceRepository {
        private val evidence = mutableMapOf<Pair<EvidenceId, TenantId>, Evidence>()
        val findAllTenants = mutableListOf<TenantId>()
        val findByIdTenants = mutableListOf<TenantId>()
        var findByIdCalls = 0
            private set

        fun seed(evidence: Evidence) {
            save(evidence)
        }

        fun get(id: EvidenceId, tenantId: TenantId): Evidence? = evidence[id to tenantId]

        fun clear() {
            evidence.clear()
            findAllTenants.clear()
            findByIdTenants.clear()
            findByIdCalls = 0
        }

        override fun save(evidence: Evidence): Evidence {
            this.evidence[evidence.id to evidence.tenantId] = evidence
            return evidence
        }

        override fun findById(id: EvidenceId, tenantId: TenantId): Evidence? {
            findByIdCalls += 1
            findByIdTenants += tenantId
            return evidence[id to tenantId]
        }

        override fun findAll(tenantId: TenantId): List<Evidence> {
            findAllTenants += tenantId
            return evidence.values.filter { it.tenantId == tenantId }
        }

        override fun findByOwner(tenantId: TenantId, owner: String): List<Evidence> =
            evidence.values.filter { it.tenantId == tenantId && it.owner == owner }

        override fun delete(id: EvidenceId, tenantId: TenantId): Boolean =
            evidence.remove(id to tenantId) != null
    }

    class RecordingAuditClient : AuditEventClient {
        val events = mutableListOf<AuditEvent>()

        override fun save(event: AuditEvent) {
            events += event
        }

        fun clear() {
            events.clear()
        }
    }
}
