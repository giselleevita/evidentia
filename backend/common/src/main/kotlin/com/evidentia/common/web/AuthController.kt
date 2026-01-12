package com.evidentia.common.web

import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.TenantId
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import java.util.UUID

/**
 * Common authentication controller providing logout functionality.
 * This controller can be used across all services that need logout endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    @Value("\${audit-log-service.url:}") private val auditServiceUrl: String = ""
) {
    
    private val auditClient: RestClient? = if (auditServiceUrl.isNotBlank()) {
        RestClient.builder()
            .baseUrl(auditServiceUrl)
            .build()
    } else {
        null
    }
    
    /**
     * Logout endpoint.
     * 
     * Note: With JWT-based stateless authentication, logout is primarily handled client-side:
     * 1. Client should discard the JWT token from local storage
     * 2. Client should redirect to Azure AD logout endpoint (if using Azure AD)
     * 3. This endpoint logs the logout event for audit purposes
     * 
     * Azure AD logout URL format:
     * https://login.microsoftonline.com/{tenant-id}/oauth2/v2.0/logout?post_logout_redirect_uri={redirect-uri}
     */
    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<LogoutResponse> {
        val tenantId = extractTenantId(jwt)
        val userId = extractUserId(jwt)
        val actor = extractActor(jwt)
        
        // Log logout event to audit service (optional, async)
        logLogoutEvent(tenantId, userId, actor)
        
        val response = LogoutResponse(
            message = "Logout successful. Please discard your token and clear local storage.",
            loggedOutAt = java.time.Instant.now(),
            azureAdLogoutUrl = buildAzureAdLogoutUrl(tenantId)
        )
        
        return ResponseEntity.ok(response)
    }
    
    private fun logLogoutEvent(tenantId: String, userId: String, actor: String) {
        if (auditClient == null) return // Skip if audit service not configured
        
        try {
            val event = AuditEvent(
                tenantId = TenantId(tenantId),
                actor = actor,
                action = "LOGOUT",
                resourceType = "USER_SESSION",
                resourceId = userId,
                correlationId = UUID.randomUUID(),
                metadata = mapOf(
                    "userId" to userId,
                    "tenantId" to tenantId
                )
            )
            
            auditClient.post()
                .uri("/api/v1/audit/events")
                .contentType(MediaType.APPLICATION_JSON)
                .body(event)
                .retrieve()
                .toBodilessEntity()
        } catch (e: Exception) {
            // Log error but don't fail logout operation
            // In production, consider using async messaging for resilience
            println("Failed to log logout event: ${e.message}")
        }
    }
    
    private fun buildAzureAdLogoutUrl(tenantId: String): String? {
        // Extract tenant ID from JWT or use configured tenant
        // Format: https://login.microsoftonline.com/{tenant-id}/oauth2/v2.0/logout
        // Clients should append ?post_logout_redirect_uri={redirect-uri}
        return if (tenantId.isNotEmpty()) {
            "https://login.microsoftonline.com/$tenantId/oauth2/v2.0/logout"
        } else {
            null
        }
    }
    
    private fun extractTenantId(jwt: Jwt): String {
        return jwt.getClaimAsString("tid")
            ?: jwt.getClaimAsString("tenant_id")
            ?: jwt.getClaimAsString("tenantId")
            ?: ""
    }
    
    private fun extractUserId(jwt: Jwt): String {
        return jwt.getClaimAsString("sub")
            ?: jwt.getClaimAsString("oid")
            ?: jwt.subject
            ?: "unknown"
    }
    
    private fun extractActor(jwt: Jwt): String {
        return jwt.getClaimAsString("preferred_username")
            ?: jwt.getClaimAsString("email")
            ?: jwt.getClaimAsString("upn")
            ?: jwt.subject
            ?: "unknown"
    }
}

data class LogoutResponse(
    val message: String,
    val loggedOutAt: java.time.Instant,
    val azureAdLogoutUrl: String? = null
)
