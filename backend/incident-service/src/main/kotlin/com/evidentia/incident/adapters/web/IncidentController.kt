package com.evidentia.incident.adapters.web

import com.evidentia.common.domain.TenantId
import com.evidentia.incident.application.*
import com.evidentia.incident.domain.IncidentId
import com.evidentia.incident.domain.IncidentSeverity
import com.evidentia.incident.domain.IncidentStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.util.UUID

data class CreateIncidentDto(
    val title: String,
    val description: String,
    val severity: String
)

data class EscalateIncidentDto(
    val escalationNote: String
)

data class ResolveIncidentDto(
    val resolutionNote: String
)

data class ReviewIncidentDto(
    val reviewNotes: String
)

data class IncidentDto(
    val id: String,
    val tenantId: String,
    val title: String,
    val description: String,
    val severity: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val createdBy: String,
    val resolvedAt: String?,
    val resolvedBy: String?,
    val reviewedAt: String?,
    val reviewedBy: String?,
    val reviewNotes: String?,
    val escalationNote: String?
)

@RestController
@RequestMapping("/api/v1/incidents")
class IncidentController(
    private val incidentService: IncidentService
) {
    @PostMapping
    fun createIncident(
        @RequestBody request: CreateIncidentDto,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String? = null
    ): ResponseEntity<*> {
        val tenantId = extractTenantId(jwt)
        val actor = extractActor(jwt)
        val corrId = correlationId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        
        val severity = try {
            IncidentSeverity.valueOf(request.severity.uppercase())
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Invalid severity"))
        }
        
        val result = incidentService.createIncident(
            tenantId = TenantId(tenantId),
            request = CreateIncidentRequest(
                title = request.title,
                description = request.description,
                severity = severity
            ),
            createdBy = actor,
            correlationId = corrId
        )
        
        return result.fold(
            onSuccess = { incident ->
                ResponseEntity.status(HttpStatus.CREATED).body(incident.toDto())
            },
            onFailure = { error ->
                when (error) {
                    is IncidentError.ValidationError -> ResponseEntity.badRequest().body(mapOf("error" to error.message))
                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Internal error"))
                }
            }
        )
    }
    
    @GetMapping
    fun listIncidents(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(required = false) status: String?
    ): List<IncidentDto> {
        val tenantId = extractTenantId(jwt)
        val incidentStatus = status?.let { 
            try {
                IncidentStatus.valueOf(it.uppercase())
            } catch (e: Exception) {
                null
            }
        }
        
        return incidentService.listIncidents(TenantId(tenantId), incidentStatus)
            .map { it.toDto() }
    }
    
    @GetMapping("/{id}")
    fun getIncident(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<*> {
        val tenantId = extractTenantId(jwt)
        val incidentId = try {
            IncidentId(UUID.fromString(id))
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Invalid incident ID"))
        }
        
        val result = incidentService.getIncident(incidentId, TenantId(tenantId))
        
        return result.fold(
            onSuccess = { incident ->
                ResponseEntity.ok(incident.toDto())
            },
            onFailure = { error ->
                when (error) {
                    is IncidentError.NotFound -> ResponseEntity.notFound().build()
                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Internal error"))
                }
            }
        )
    }
    
    @PostMapping("/{id}/escalate")
    @PreAuthorize("hasRole('Admin')")
    fun escalateIncident(
        @PathVariable id: String,
        @RequestBody request: EscalateIncidentDto,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String? = null
    ): ResponseEntity<*> {
        val tenantId = extractTenantId(jwt)
        val actor = extractActor(jwt)
        val corrId = correlationId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        
        val incidentId = try {
            IncidentId(UUID.fromString(id))
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Invalid incident ID"))
        }
        
        val result = incidentService.escalateIncident(
            id = incidentId,
            tenantId = TenantId(tenantId),
            request = EscalateIncidentRequest(request.escalationNote),
            actor = actor,
            correlationId = corrId
        )
        
        return result.fold(
            onSuccess = { incident ->
                ResponseEntity.ok(incident.toDto())
            },
            onFailure = { error ->
                when (error) {
                    is IncidentError.NotFound -> ResponseEntity.notFound().build()
                    is IncidentError.InvalidState -> ResponseEntity.badRequest().body(mapOf("error" to error.message))
                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Internal error"))
                }
            }
        )
    }
    
    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('Admin')")
    fun resolveIncident(
        @PathVariable id: String,
        @RequestBody request: ResolveIncidentDto,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String? = null
    ): ResponseEntity<*> {
        val tenantId = extractTenantId(jwt)
        val actor = extractActor(jwt)
        val corrId = correlationId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        
        val incidentId = try {
            IncidentId(UUID.fromString(id))
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Invalid incident ID"))
        }
        
        val result = incidentService.resolveIncident(
            id = incidentId,
            tenantId = TenantId(tenantId),
            request = ResolveIncidentRequest(request.resolutionNote),
            actor = actor,
            correlationId = corrId
        )
        
        return result.fold(
            onSuccess = { incident ->
                ResponseEntity.ok(incident.toDto())
            },
            onFailure = { error ->
                when (error) {
                    is IncidentError.NotFound -> ResponseEntity.notFound().build()
                    is IncidentError.InvalidState -> ResponseEntity.badRequest().body(mapOf("error" to error.message))
                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Internal error"))
                }
            }
        )
    }
    
    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('Auditor')")
    fun reviewIncident(
        @PathVariable id: String,
        @RequestBody request: ReviewIncidentDto,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String? = null
    ): ResponseEntity<*> {
        val tenantId = extractTenantId(jwt)
        val actor = extractActor(jwt)
        val corrId = correlationId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        
        val incidentId = try {
            IncidentId(UUID.fromString(id))
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Invalid incident ID"))
        }
        
        val result = incidentService.reviewIncident(
            id = incidentId,
            tenantId = TenantId(tenantId),
            request = ReviewIncidentRequest(request.reviewNotes),
            actor = actor,
            correlationId = corrId
        )
        
        return result.fold(
            onSuccess = { incident ->
                ResponseEntity.ok(incident.toDto())
            },
            onFailure = { error ->
                when (error) {
                    is IncidentError.NotFound -> ResponseEntity.notFound().build()
                    is IncidentError.InvalidState -> ResponseEntity.badRequest().body(mapOf("error" to error.message))
                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Internal error"))
                }
            }
        )
    }
    
    @GetMapping("/severity/{severity}")
    fun listIncidentsBySeverity(
        @PathVariable severity: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<*> {
        val tenantId = extractTenantId(jwt)
        val incidentSeverity = try {
            IncidentSeverity.valueOf(severity.uppercase())
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Invalid severity"))
        }
        
        val incidents = incidentService.listIncidentsBySeverity(TenantId(tenantId), incidentSeverity)
        return ResponseEntity.ok(incidents.map { it.toDto() })
    }
    
    private fun extractTenantId(jwt: Jwt): String {
        return jwt.getClaimAsString("tid") ?: jwt.getClaimAsString("tenant_id") ?: throw IllegalStateException("No tenant ID in token")
    }
    
    private fun extractActor(jwt: Jwt): String {
        return jwt.subject ?: jwt.getClaimAsString("preferred_username") ?: "unknown"
    }
}

private fun com.evidentia.incident.domain.Incident.toDto(): IncidentDto {
    return IncidentDto(
        id = id.value.toString(),
        tenantId = tenantId.value,
        title = title,
        description = description,
        severity = severity.name,
        status = status.name,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        createdBy = createdBy,
        resolvedAt = resolvedAt?.toString(),
        resolvedBy = resolvedBy,
        reviewedAt = reviewedAt?.toString(),
        reviewedBy = reviewedBy,
        reviewNotes = reviewNotes,
        escalationNote = escalationNote
    )
}
