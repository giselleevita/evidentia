package com.evidentia.integration.adapters.web

import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.TenantId
import com.evidentia.common.security.SecurityConfig
import com.evidentia.common.web.RateLimitFilter
import com.evidentia.common.web.TenantFilter
import com.evidentia.integration.application.AuditEventClient
import com.evidentia.integration.application.IntegrationRepository
import com.evidentia.integration.application.IntegrationService
import com.evidentia.integration.domain.Integration
import com.evidentia.integration.domain.IntegrationId
import com.evidentia.integration.domain.IntegrationStatus
import com.evidentia.integration.domain.IntegrationType
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

@WebMvcTest(IntegrationController::class)
@Import(
    SecurityConfig::class,
    TenantFilter::class,
    RateLimitFilter::class,
    IntegrationControllerSecurityTest.TestBeans::class,
)
class IntegrationControllerSecurityTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var repository: RecordingIntegrationRepository

    @Autowired
    private lateinit var auditClient: RecordingAuditClient

    @AfterEach
    fun resetState() {
        repository.clear()
        auditClient.clear()
    }

    @Test
    fun `list integrations requires authentication`() {
        mockMvc.get("/api/v1/integrations")
            .andExpect {
                status { isUnauthorized() }
            }

        assertEquals(emptyList<TenantId>(), repository.findAllTenants)
    }

    @Test
    fun `authenticated integration requests require a tenant claim`() {
        mockMvc.get("/api/v1/integrations") {
            with(jwt().authorities(SimpleGrantedAuthority("ROLE_Auditor")))
        }.andExpect {
            status { isForbidden() }
        }

        assertEquals(emptyList<TenantId>(), repository.findAllTenants)
    }

    @Test
    fun `user role cannot list integrations`() {
        mockMvc.get("/api/v1/integrations") {
            with(jwtFor("User"))
        }.andExpect {
            status { isForbidden() }
        }

        assertEquals(emptyList<TenantId>(), repository.findAllTenants)
    }

    @Test
    fun `tenant header mismatch is rejected before integration service access`() {
        mockMvc.get("/api/v1/integrations") {
            with(jwtFor("Auditor", tenantId = "tenant-a"))
            header("X-Tenant-Id", "tenant-b")
        }.andExpect {
            status { isForbidden() }
        }

        assertEquals(emptyList<TenantId>(), repository.findAllTenants)
    }

    @Test
    fun `auditor role cannot create integrations`() {
        mockMvc.post("/api/v1/integrations") {
            with(jwtFor("Auditor"))
            contentType = MediaType.APPLICATION_JSON
            content = """{"type":"GITHUB","name":"GitHub"}"""
        }.andExpect {
            status { isForbidden() }
        }

        assertEquals(0, repository.savedCount)
    }

    @Test
    fun `admin role can create integration for the validated tenant`() {
        mockMvc.post("/api/v1/integrations") {
            with(jwtFor("Admin", subject = "admin@example.com"))
            contentType = MediaType.APPLICATION_JSON
            content = """{"type":"GITHUB","name":"GitHub","configuration":{"organization":"acme"}}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.tenantId") { value("tenant-a") }
            jsonPath("$.data.type") { value("GITHUB") }
        }

        assertEquals(listOf(TenantId("tenant-a")), repository.savedTenants)
        assertEquals(listOf("integration.created"), auditClient.events.map { it.action })
        assertEquals(TenantId("tenant-a"), auditClient.events.single().tenantId)
    }

    @Test
    fun `list integrations uses only the validated token tenant`() {
        repository.seed(integrationFor(UUID.randomUUID(), TenantId("tenant-a"), IntegrationType.GITHUB))
        repository.seed(integrationFor(UUID.randomUUID(), TenantId("tenant-b"), IntegrationType.JIRA))

        mockMvc.get("/api/v1/integrations") {
            with(jwtFor("Auditor", tenantId = "tenant-a"))
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

    private fun integrationFor(
        id: UUID,
        tenantId: TenantId,
        type: IntegrationType,
    ): Integration {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        return Integration(
            id = IntegrationId(id),
            tenantId = tenantId,
            type = type,
            name = type.name,
            status = IntegrationStatus.INACTIVE,
            createdAt = now,
            updatedAt = now,
        )
    }

    @TestConfiguration
    class TestBeans {
        @Bean
        fun integrationRepository(): RecordingIntegrationRepository = RecordingIntegrationRepository()

        @Bean
        fun auditEventClient(): RecordingAuditClient = RecordingAuditClient()

        @Bean
        fun integrationService(
            repository: IntegrationRepository,
            auditEventClient: AuditEventClient,
        ): IntegrationService = IntegrationService(repository, auditEventClient)
    }

    class RecordingIntegrationRepository : IntegrationRepository {
        private val integrations = mutableMapOf<Pair<IntegrationId, TenantId>, Integration>()
        val findAllTenants = mutableListOf<TenantId>()
        val savedTenants = mutableListOf<TenantId>()
        var savedCount = 0
            private set

        fun seed(integration: Integration) {
            integrations[integration.id to integration.tenantId] = integration
        }

        fun clear() {
            integrations.clear()
            findAllTenants.clear()
            savedTenants.clear()
            savedCount = 0
        }

        override fun save(integration: Integration): Integration {
            savedCount += 1
            savedTenants += integration.tenantId
            integrations[integration.id to integration.tenantId] = integration
            return integration
        }

        override fun findById(id: IntegrationId, tenantId: TenantId): Integration? =
            integrations[id to tenantId]

        override fun findAll(tenantId: TenantId): List<Integration> {
            findAllTenants += tenantId
            return integrations.values.filter { it.tenantId == tenantId }
        }

        override fun findByType(tenantId: TenantId, type: IntegrationType): List<Integration> =
            integrations.values.filter { it.tenantId == tenantId && it.type == type }

        override fun delete(id: IntegrationId, tenantId: TenantId): Boolean =
            integrations.remove(id to tenantId) != null
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
