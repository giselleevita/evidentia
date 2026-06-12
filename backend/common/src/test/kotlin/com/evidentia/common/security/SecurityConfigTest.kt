package com.evidentia.common.security

import com.evidentia.common.web.RateLimitFilter
import com.evidentia.common.web.TenantFilter
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant

class SecurityConfigTest {
    @Test
    fun `maps Azure application roles to Spring role authorities`() {
        val converter = SecurityConfig(TenantFilter(), RateLimitFilter(), "http://localhost")
            .jwtAuthenticationConverter()
        val jwt = Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(60),
            mapOf("alg" to "none"),
            mapOf("sub" to "service", "roles" to listOf("Service")),
        )

        val authentication = requireNotNull(converter.convert(jwt))

        assertTrue(authentication.authorities.any { it.authority == "ROLE_Service" })
    }
}
