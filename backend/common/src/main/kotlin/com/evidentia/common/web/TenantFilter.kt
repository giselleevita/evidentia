package com.evidentia.common.web

import com.evidentia.common.context.TenantContext
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter to extract tenant ID from JWT token and set it in TenantContext.
 * Runs after JWT authentication to ensure token is validated.
 */
@Component
class TenantFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication?.principal is Jwt) {
                val jwt = authentication.principal as Jwt
                val tenantId = jwt.getClaimAsString("tid") 
                    ?: jwt.getClaimAsString("tenant_id")
                    ?: jwt.getClaimAsString("tenantId")

                if (tenantId.isNullOrBlank()) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant claim required")
                    return
                }

                val requestedTenantId = request.getHeader("X-Tenant-Id")
                if (!requestedTenantId.isNullOrBlank() && requestedTenantId != tenantId) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant header mismatch")
                    return
                }

                TenantContext.setTenantId(tenantId)
            }
            
            filterChain.doFilter(request, response)
        } finally {
            TenantContext.clear()
        }
    }
}
