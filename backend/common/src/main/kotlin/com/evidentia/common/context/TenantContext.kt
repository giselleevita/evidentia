package com.evidentia.common.context

import com.evidentia.common.domain.TenantId
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt

object TenantContext {
    private val tenantIdThreadLocal = ThreadLocal<String?>()
    
    fun setTenantId(tenantId: String) {
        tenantIdThreadLocal.set(tenantId)
    }
    
    fun getTenantId(): TenantId? {
        val tenantId = tenantIdThreadLocal.get()
            ?: extractFromSecurityContext()
        return tenantId?.let { TenantId(it) }
    }
    
    fun getTenantIdOrThrow(): TenantId {
        return getTenantId() ?: throw IllegalStateException("No tenant ID available in context")
    }
    
    fun clear() {
        tenantIdThreadLocal.remove()
    }
    
    private fun extractFromSecurityContext(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication?.principal is Jwt) {
            val jwt = authentication.principal as Jwt
            return jwt.getClaimAsString("tid") 
                ?: jwt.getClaimAsString("tenant_id")
                ?: jwt.getClaimAsString("tenantId")
        }
        return null
    }
}
