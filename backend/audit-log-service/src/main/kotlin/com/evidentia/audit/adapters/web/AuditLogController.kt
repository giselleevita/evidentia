package com.evidentia.audit.adapters.web

import com.evidentia.audit.application.AuditLogService
import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.TenantId
import com.evidentia.common.context.TenantContext
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/audit")
class AuditLogController(
    private val auditLogService: AuditLogService
) {
    @PostMapping("/events")
    @PreAuthorize("hasAnyRole('Admin', 'Service')")
    fun recordEvent(@RequestBody event: AuditEvent): ResponseEntity<AuditEvent> {
        val tenantEvent = event.copy(tenantId = TenantContext.getTenantIdOrThrow())
        auditLogService.recordEvent(tenantEvent)
        return ResponseEntity.ok(tenantEvent)
    }
    
    @GetMapping("/trail")
    @PreAuthorize("hasAnyRole('Admin', 'Auditor')")
    fun getAuditTrail(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(defaultValue = "100") limit: Int
    ): ResponseEntity<List<AuditEvent>> {
        val tenantId = extractTenantId(jwt)
        val events = auditLogService.getAuditTrail(TenantId(tenantId), limit.coerceIn(1, 100))
        return ResponseEntity.ok(events)
    }
    
    @GetMapping("/correlation/{correlationId}")
    @PreAuthorize("hasAnyRole('Admin', 'Auditor')")
    fun getEventsByCorrelationId(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable correlationId: UUID
    ): ResponseEntity<List<AuditEvent>> {
        val events = auditLogService.getEventsByCorrelationId(TenantId(extractTenantId(jwt)), correlationId)
        return ResponseEntity.ok(events)
    }
    
    @GetMapping("/resource/{resourceType}/{resourceId}")
    @PreAuthorize("hasAnyRole('Admin', 'Auditor')")
    fun getEventsByResource(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable resourceType: String,
        @PathVariable resourceId: String
    ): ResponseEntity<List<AuditEvent>> {
        val events = auditLogService.getEventsByResource(TenantId(extractTenantId(jwt)), resourceType, resourceId)
        return ResponseEntity.ok(events)
    }
    
    private fun extractTenantId(jwt: Jwt): String {
        return jwt.getClaimAsString("tid") ?: jwt.getClaimAsString("tenant_id") ?: throw IllegalStateException("No tenant ID in token")
    }
}
