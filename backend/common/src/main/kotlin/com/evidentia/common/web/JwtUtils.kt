package com.evidentia.common.web

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt

/**
 * Utility functions for extracting information from JWT tokens.
 */
object JwtUtils {
    fun extractTenantId(jwt: Jwt): String {
        return jwt.getClaimAsString("tid") 
            ?: jwt.getClaimAsString("tenant_id") 
            ?: jwt.getClaimAsString("tenantId")
            ?: throw IllegalStateException("No tenant ID in token")
    }
    
    fun extractActor(jwt: Jwt): String {
        return jwt.subject 
            ?: jwt.getClaimAsString("preferred_username")
            ?: jwt.getClaimAsString("email")
            ?: jwt.getClaimAsString("upn")
            ?: "unknown"
    }
    
    fun getCurrentJwt(): Jwt? {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.principal as? Jwt
    }
    
    fun getCurrentTenantId(): String? {
        return getCurrentJwt()?.let { extractTenantId(it) }
    }
    
    fun getCurrentActor(): String? {
        return getCurrentJwt()?.let { extractActor(it) }
    }
}
