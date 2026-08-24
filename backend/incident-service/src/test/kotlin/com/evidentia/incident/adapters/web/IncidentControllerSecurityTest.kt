package com.evidentia.incident.adapters.web

import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.TenantId
import com.evidentia.common.security.SecurityConfig
import com.evidentia.common.web.RateLimitFilter
import com.evidentia.common.web.TenantFilter
import com.evidentia.incident.application.AuditEventClient
import com.evidentia.incident.application.IncidentRepository
import com.evidentia.incident.application.IncidentService
import com.evidentia.incident.domain.Incident
import com.evidentia.incident.domain.IncidentId
import com.evidentia.incident.domain.IncidentSeverity
import com.evidentia.incident.domain.IncidentStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
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

@WebMvcTest(IncidentController::class)
@Import(
    SecurityConfig::class,
    TenantFilter::class,
    RateLimitFilter::class,
    IncidentControllerSecurityTest.TestBeans::class,
)
class IncidentControllerSecurityTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var repository: RecordingIncidentRepository

    @Autowired
    private lateinit var auditClient: RecordingAuditClient

    @AfterEach
    fun resetState() {
        repository.clear()
        auditClient.clear()
    }

    @Test
    fun `list incidents requires authentication`() {
        mockMvc.get("/api/v1/incidents")
            .andExpect {
                status { isUnauthorized() }
            }

        assertEquals(emptyList<TenantId>(), repository.findAllTenants)
    }

    @Test
    fun `authenticated incident requests require a tenant claim`() {
        mockMvc.get("/api/v1/incidents") {
            with(jwt().authorities(SimpleGrantedAuthority("ROLE_User")))
        }.andExpect {
            status { isForbidden() }
        }

        assertEquals(emptyList<TenantId>(), repository.findAllTenants)
    }

    @Test
    fun `service role cannot access incident endpoints`() {
        mockMvc.get("/api/v1/incidents") {
            with(jwtFor("Service"))
        }.andExpect {
            status { isForbidden() }
        }

        assertEquals(emptyList<TenantId>(), repository.findAllTenants)
    }

    @Test
    fun `tenant header mismatch is rejected before incident service access`() {
        mockMvc.get("/api/v1/incidents") {
            with(jwtFor("User", tenantId = "tenant-a"))
            header("X-Tenant-Id", "tenant-b")
        }.andExpect {
            status { isForbidden() }
        }

        assertEquals(emptyList<TenantId>(), repository.findAllTenants)
    }

    @Test
    fun `user role cannot escalate incidents`() {
        val incidentId = UUID.randomUUID()

        mockMvc.post("/api/v1/incidents/$incidentId/escalate") {
            with(jwtFor("User"))
            contentType = MediaType.APPLICATION_JSON
            content = """{"escalationNote":"needs escalation"}"""
        }.andExpect {
            status { isForbidden() }
        }

        assertEquals(0, repository.findByIdCalls)
    }

    @Test
    fun `auditor role can resolve incident for the validated tenant`() {
        val incidentId = UUID.randomUUID()
        repository.seed(incidentFor(incidentId, TenantId("tenant-a"), IncidentStatus.ESCALATED))

        mockMvc.post("/api/v1/incidents/$incidentId/resolve") {
            with(jwtFor("Auditor", subject = "auditor@example.com"))
            contentType = MediaType.APPLICATION_JSON
            content = """{"resolutionNote":"contained"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.tenantId") { value("tenant-a") }
            jsonPath("$.status") { value("RESOLVED") }
            jsonPath("$.resolvedBy") { value("auditor@example.com") }
        }

        val resolved = repository.get(IncidentId(incidentId), TenantId("tenant-a"))
        assertEquals(IncidentStatus.RESOLVED, resolved?.status)
        assertEquals("auditor@example.com", resolved?.resolvedBy)
        assertEquals(listOf("incident.resolved"), auditClient.events.map { it.action })
        assertEquals(listOf(TenantId("tenant-a")), repository.findByIdTenants)
    }

    @Test
    fun `list incidents uses only the validated token tenant`() {
        repository.seed(incidentFor(UUID.randomUUID(), TenantId("tenant-a"), IncidentStatus.OPEN))
        repository.seed(incidentFor(UUID.randomUUID(), TenantId("tenant-b"), IncidentStatus.OPEN))

        mockMvc.get("/api/v1/incidents") {
            with(jwtFor("User", tenantId = "tenant-a"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].tenantId") { value("tenant-a") }
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

    private fun incidentFor(
        id: UUID,
        tenantId: TenantId,
        status: IncidentStatus,
    ): Incident {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        return Incident(
            id = IncidentId(id),
            tenantId = tenantId,
            title = "Unauthorized Access",
            description = "Investigate suspicious access",
            severity = IncidentSeverity.HIGH,
            status = status,
            createdAt = now,
            updatedAt = now,
            createdBy = "reporter@example.com",
        )
    }

    @TestConfiguration
    class TestBeans {
        @Bean
        fun incidentRepository(): RecordingIncidentRepository = RecordingIncidentRepository()

        @Bean
        fun auditEventClient(): RecordingAuditClient = RecordingAuditClient()

        @Bean
        fun incidentService(
            repository: IncidentRepository,
            auditEventClient: AuditEventClient,
        ): IncidentService = IncidentService(repository, auditEventClient)
    }

    class RecordingIncidentRepository : IncidentRepository {
        private val incidents = mutableMapOf<Pair<IncidentId, TenantId>, Incident>()
        val findAllTenants = mutableListOf<TenantId>()
        val findByIdTenants = mutableListOf<TenantId>()
        var findByIdCalls = 0
            private set

        fun seed(incident: Incident) {
            save(incident)
        }

        fun get(id: IncidentId, tenantId: TenantId): Incident? = incidents[id to tenantId]

        fun clear() {
            incidents.clear()
            findAllTenants.clear()
            findByIdTenants.clear()
            findByIdCalls = 0
        }

        override fun save(incident: Incident): Incident {
            incidents[incident.id to incident.tenantId] = incident
            return incident
        }

        override fun findById(id: IncidentId, tenantId: TenantId): Incident? {
            findByIdCalls += 1
            findByIdTenants += tenantId
            return incidents[id to tenantId]
        }

        override fun findAll(tenantId: TenantId): List<Incident> {
            findAllTenants += tenantId
            return incidents.values.filter { it.tenantId == tenantId }
        }

        override fun findByStatus(tenantId: TenantId, status: IncidentStatus): List<Incident> =
            findAll(tenantId).filter { it.status == status }

        override fun findBySeverity(tenantId: TenantId, severity: IncidentSeverity): List<Incident> =
            incidents.values.filter { it.tenantId == tenantId && it.severity == severity }
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
