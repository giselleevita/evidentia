package com.evidentia.audit.adapters.web

import com.evidentia.audit.application.AuditLogService
import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.TenantId
import com.evidentia.common.domain.toSubmission
import com.evidentia.common.security.SecurityConfig
import com.evidentia.common.web.RateLimitFilter
import com.evidentia.common.web.TenantFilter
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockingDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.UUID

@WebMvcTest(AuditLogController::class)
@Import(SecurityConfig::class, TenantFilter::class, RateLimitFilter::class)
class AuditLogControllerSecurityTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var auditLogService: AuditLogService

    @Test
    fun `service identity can ingest only for its validated tenant`() {
        val payload = eventFor("spoofed-tenant").toSubmission()

        mockMvc.post("/api/v1/audit/events") {
            with(
                jwt()
                    .jwt { it.claim("tid", "validated-tenant").claim("roles", listOf("Service")) }
                    .authorities(SimpleGrantedAuthority("ROLE_Service")),
            )
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsBytes(payload)
        }.andExpect {
            status { isOk() }
            jsonPath("$.tenantId") { value("validated-tenant") }
        }

        val recorded = mockingDetails(auditLogService).invocations
            .single { it.method.name == "recordEvent" }
            .arguments
            .single() as AuditEvent
        assertEquals(TenantId("validated-tenant"), recorded.tenantId)
    }

    @Test
    fun `ordinary user cannot inject audit events`() {
        mockMvc.post("/api/v1/audit/events") {
            with(
                jwt()
                    .jwt { it.claim("tid", "tenant-a").claim("roles", listOf("User")) }
                    .authorities(SimpleGrantedAuthority("ROLE_User")),
            )
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsBytes(eventFor("tenant-a").toSubmission())
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `conflicting tenant header is rejected before audit ingestion`() {
        mockMvc.post("/api/v1/audit/events") {
            with(
                jwt()
                    .jwt { it.claim("tid", "tenant-a").claim("roles", listOf("Service")) }
                    .authorities(SimpleGrantedAuthority("ROLE_Service")),
            )
            header("X-Tenant-Id", "tenant-b")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsBytes(eventFor("tenant-a").toSubmission())
        }.andExpect {
            status { isForbidden() }
        }
    }

    private fun eventFor(tenantId: String) = AuditEvent(
        tenantId = TenantId(tenantId),
        actor = "service",
        action = "evidence.updated",
        resourceType = "Evidence",
        resourceId = "evidence-1",
        correlationId = UUID.randomUUID(),
    )
}
