package com.evidentia.audit.adapters.web

import com.evidentia.audit.application.AuditLogService
import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.TenantId
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
    fun recordEvent(@RequestBody event: AuditEvent): ResponseEntity<AuditEvent> {
        auditLogService.recordEvent(event)
        return ResponseEntity.ok(event)
    }
    
    @GetMapping("/trail")
    fun getAuditTrail(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(defaultValue = "100") limit: Int
    ): ResponseEntity<List<AuditEvent>> {
        val tenantId = extractTenantId(jwt)
        val events = auditLogService.getAuditTrail(TenantId(tenantId), limit)
        return ResponseEntity.ok(events)
    }
    
    @GetMapping("/correlation/{correlationId}")
    fun getEventsByCorrelationId(
        @PathVariable correlationId: UUID
    ): ResponseEntity<List<AuditEvent>> {
        val events = auditLogService.getEventsByCorrelationId(correlationId)
        return ResponseEntity.ok(events)
    }
    
    @GetMapping("/resource/{resourceType}/{resourceId}")
    fun getEventsByResource(
        @PathVariable resourceType: String,
        @PathVariable resourceId: String
    ): ResponseEntity<List<AuditEvent>> {
        val events = auditLogService.getEventsByResource(resourceType, resourceId)
        return ResponseEntity.ok(events)
    }
    
    private fun extractTenantId(jwt: Jwt): String {
        return jwt.getClaimAsString("tid") ?: jwt.getClaimAsString("tenant_id") ?: throw IllegalStateException("No tenant ID in token")
    }
}
