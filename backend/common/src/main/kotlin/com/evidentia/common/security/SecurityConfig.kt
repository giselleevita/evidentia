package com.evidentia.common.security

import com.evidentia.common.web.TenantFilter
import com.evidentia.common.web.RateLimitFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * Common security configuration for all services.
 * 
 * Features:
 * - OAuth2 Resource Server with JWT validation
 * - Method-level security enabled (@PreAuthorize)
 * - Tenant context extraction via TenantFilter
 * - CORS configuration for frontend
 * 
 * Role-based access control:
 * - Admin: Full access to manage incidents, escalate, resolve
 * - Auditor: Can approve evidence, review incidents, view audit logs
 * - User: Can create evidence and incidents, view own items
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val tenantFilter: TenantFilter,
    private val rateLimitFilter: RateLimitFilter,
    @Value("\${evidentia.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private val allowedOrigins: String,
) {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationConverter: JwtAuthenticationConverter,
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authz ->
                authz
                    // Public endpoints
                    .requestMatchers(
                        "/actuator/health",
                        "/actuator/info"
                    ).permitAll()
                    // All other endpoints require authentication
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { resourceServer ->
                resourceServer.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                }
            }
            .addFilterAfter(rateLimitFilter, BearerTokenAuthenticationFilter::class.java)
            // Add tenant filter after JWT authentication
            .addFilterAfter(tenantFilter, UsernamePasswordAuthenticationFilter::class.java)
        
        return http.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val scopeConverter = JwtGrantedAuthoritiesConverter()
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            val authorities = scopeConverter.convert(jwt)?.toMutableList() ?: mutableListOf()
            jwt.getClaimAsStringList("roles")
                .orEmpty()
                .mapTo(authorities) { SimpleGrantedAuthority("ROLE_$it") }
            authorities
        }
        return converter
    }
    
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = allowedOrigins.split(",").map(String::trim).filter(String::isNotBlank)
        configuration.allowedMethods = listOf("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
