package com.evidentia.common.web

import com.evidentia.common.context.TenantContext
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.time.Instant

class TenantFilterTest {
    private val filter = TenantFilter()

    @AfterEach
    fun clearContext() {
        SecurityContextHolder.clearContext()
        TenantContext.clear()
    }

    @Test
    fun `rejects a tenant header that conflicts with the validated token`() {
        authenticateAs("tenant-a")
        val request = MockHttpServletRequest().apply { addHeader("X-Tenant-Id", "tenant-b") }
        val response = MockHttpServletResponse()
        var invoked = false

        filter.doFilter(request, response, FilterChain { _, _ -> invoked = true })

        assertEquals(403, response.status)
        assertTrue(!invoked)
        SecurityContextHolder.clearContext()
        assertNull(TenantContext.getTenantId())
    }

    @Test
    fun `uses the validated token tenant and clears it after the request`() {
        authenticateAs("tenant-a")
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        var observedTenant: String? = null

        filter.doFilter(request, response, FilterChain { _, _ ->
            observedTenant = TenantContext.getTenantIdOrThrow().value
        })

        assertEquals("tenant-a", observedTenant)
        SecurityContextHolder.clearContext()
        assertNull(TenantContext.getTenantId())
    }

    private fun authenticateAs(tenantId: String) {
        val jwt = Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(60),
            mapOf("alg" to "none"),
            mapOf("sub" to "service", "tid" to tenantId),
        )
        SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(jwt)
    }
}
