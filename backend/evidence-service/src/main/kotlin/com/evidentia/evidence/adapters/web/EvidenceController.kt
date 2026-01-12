package com.evidentia.evidence.adapters.web

import com.evidentia.common.domain.TenantId
import com.evidentia.common.web.ApiResponse
import com.evidentia.evidence.adapters.web.dto.*
import com.evidentia.evidence.adapters.web.mapper.EvidenceMapper.toDto
import com.evidentia.evidence.application.*
import com.evidentia.evidence.domain.EvidenceId
import com.evidentia.evidence.domain.EvidenceStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/evidence")
@Tag(name = "Evidence", description = "Evidence lifecycle management API")
class EvidenceController(
    private val evidenceService: EvidenceService
) {
    @PostMapping
    @Operation(summary = "Create new evidence", description = "Creates a new evidence item in DRAFT status")
    fun createEvidence(
        @Valid @RequestBody request: CreateEvidenceDto,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String? = null
    ): ResponseEntity<ApiResponse<EvidenceDto>> {
        val tenantId = extractTenantId(jwt)
        val actor = extractActor(jwt)
        val corrId = correlationId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        
        val result = evidenceService.createEvidence(
            tenantId = TenantId(tenantId),
            request = CreateEvidenceRequest(
                title = request.title,
                description = request.description,
                type = request.type,
                sourceSystem = request.sourceSystem,
                owner = request.owner,
                references = request.references
            ),
            actor = actor,
            correlationId = corrId
        )
        
        return result.fold(
            onSuccess = { evidence ->
                ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(evidence.toDto()))
            },
            onFailure = { error ->
                when (error) {
                    is EvidenceError.ValidationError -> 
                        ResponseEntity.badRequest()
                            .body(ApiResponse.error("VALIDATION_ERROR", error.message))
                    else -> 
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("INTERNAL_ERROR", "Failed to create evidence"))
                }
            }
        )
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get evidence by ID")
    fun getEvidence(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<ApiResponse<EvidenceDto>> {
        val tenantId = extractTenantId(jwt)
        val evidenceId = try {
            EvidenceId(UUID.fromString(id))
        } catch (e: Exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_ID", "Invalid evidence ID format"))
        }
        
        val result = evidenceService.getEvidence(evidenceId, TenantId(tenantId))
        
        return result.fold(
            onSuccess = { evidence ->
                ResponseEntity.ok(ApiResponse.success(evidence.toDto()))
            },
            onFailure = { error ->
                when (error) {
                    is EvidenceError.NotFound -> 
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("NOT_FOUND", "Evidence not found"))
                    else -> 
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("INTERNAL_ERROR", "Failed to retrieve evidence"))
                }
            }
        )
    }
    
    @GetMapping
    @Operation(summary = "List evidence", description = "Lists all evidence for the current tenant, optionally filtered by status")
    fun listEvidence(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<ApiResponse<List<EvidenceDto>>> {
        val tenantId = extractTenantId(jwt)
        val statusFilter = status?.let { 
            try { EvidenceStatus.valueOf(it.uppercase()) } 
            catch (e: Exception) { null }
        }
        val evidence = evidenceService.listEvidence(TenantId(tenantId), statusFilter)
        
        return ResponseEntity.ok(ApiResponse.success(evidence.map { it.toDto() }))
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update evidence", description = "Updates evidence content (only allowed in DRAFT or REJECTED status)")
    fun updateEvidence(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateEvidenceDto,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String? = null
    ): ResponseEntity<ApiResponse<EvidenceDto>> {
        val tenantId = extractTenantId(jwt)
        val actor = extractActor(jwt)
        val corrId = correlationId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        
        val evidenceId = try {
            EvidenceId(UUID.fromString(id))
        } catch (e: Exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_ID", "Invalid evidence ID format"))
        }
        
        val result = evidenceService.updateEvidence(
            id = evidenceId,
            tenantId = TenantId(tenantId),
            request = UpdateEvidenceRequest(
                title = request.title,
                description = request.description,
                type = request.type,
                sourceSystem = request.sourceSystem,
                references = request.references
            ),
            actor = actor,
            correlationId = corrId
        )
        
        return result.fold(
            onSuccess = { evidence ->
                ResponseEntity.ok(ApiResponse.success(evidence.toDto()))
            },
            onFailure = { error ->
                when (error) {
                    is EvidenceError.NotFound -> 
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("NOT_FOUND", "Evidence not found"))
                    is EvidenceError.ValidationError -> 
                        ResponseEntity.badRequest()
                            .body(ApiResponse.error("VALIDATION_ERROR", error.message))
                    else -> 
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("INTERNAL_ERROR", "Failed to update evidence"))
                }
            }
        )
    }
    
    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit evidence for review", description = "Transitions evidence from DRAFT to IN_REVIEW")
    fun submitForReview(
        @PathVariable id: String,
        @RequestBody request: SubmitEvidenceDto,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String? = null
    ): ResponseEntity<ApiResponse<EvidenceDto>> {
        val tenantId = extractTenantId(jwt)
        val actor = extractActor(jwt)
        val corrId = correlationId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        
        val evidenceId = try {
            EvidenceId(UUID.fromString(id))
        } catch (e: Exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_ID", "Invalid evidence ID format"))
        }
        
        val result = evidenceService.submitForReview(
            id = evidenceId,
            tenantId = TenantId(tenantId),
            request = SubmitEvidenceRequest(request.note),
            actor = actor,
            correlationId = corrId
        )
        
        return result.fold(
            onSuccess = { evidence ->
                ResponseEntity.ok(ApiResponse.success(evidence.toDto()))
            },
            onFailure = { error ->
                when (error) {
                    is EvidenceError.NotFound -> 
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("NOT_FOUND", "Evidence not found"))
                    is EvidenceError.InvalidTransition -> 
                        ResponseEntity.badRequest()
                            .body(ApiResponse.error("INVALID_TRANSITION", "Invalid status transition"))
                    else -> 
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("INTERNAL_ERROR", "Failed to submit evidence"))
                }
            }
        )
    }
    
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('Auditor')")
    @Operation(summary = "Approve evidence", description = "Approves evidence (Auditor role required)")
    fun approveEvidence(
        @PathVariable id: String,
        @RequestBody request: ApproveEvidenceDto,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String? = null
    ): ResponseEntity<ApiResponse<EvidenceDto>> {
        val tenantId = extractTenantId(jwt)
        val approver = extractActor(jwt)
        val corrId = correlationId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        
        val evidenceId = try {
            EvidenceId(UUID.fromString(id))
        } catch (e: Exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_ID", "Invalid evidence ID format"))
        }
        
        val result = evidenceService.approveEvidence(
            id = evidenceId,
            tenantId = TenantId(tenantId),
            request = ApproveEvidenceRequest(request.note),
            approver = approver,
            correlationId = corrId
        )
        
        return result.fold(
            onSuccess = { evidence ->
                ResponseEntity.ok(ApiResponse.success(evidence.toDto()))
            },
            onFailure = { error ->
                when (error) {
                    is EvidenceError.NotFound -> 
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("NOT_FOUND", "Evidence not found"))
                    is EvidenceError.InvalidTransition -> 
                        ResponseEntity.badRequest()
                            .body(ApiResponse.error("INVALID_TRANSITION", "Invalid status transition"))
                    else -> 
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("INTERNAL_ERROR", "Failed to approve evidence"))
                }
            }
        )
    }
    
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('Auditor')")
    @Operation(summary = "Reject evidence", description = "Rejects evidence (Auditor role required)")
    fun rejectEvidence(
        @PathVariable id: String,
        @Valid @RequestBody request: RejectEvidenceDto,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String? = null
    ): ResponseEntity<ApiResponse<EvidenceDto>> {
        val tenantId = extractTenantId(jwt)
        val actor = extractActor(jwt)
        val corrId = correlationId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        
        val evidenceId = try {
            EvidenceId(UUID.fromString(id))
        } catch (e: Exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_ID", "Invalid evidence ID format"))
        }
        
        val result = evidenceService.rejectEvidence(
            id = evidenceId,
            tenantId = TenantId(tenantId),
            request = RejectEvidenceRequest(request.reason),
            actor = actor,
            correlationId = corrId
        )
        
        return result.fold(
            onSuccess = { evidence ->
                ResponseEntity.ok(ApiResponse.success(evidence.toDto()))
            },
            onFailure = { error ->
                when (error) {
                    is EvidenceError.NotFound -> 
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("NOT_FOUND", "Evidence not found"))
                    is EvidenceError.InvalidTransition -> 
                        ResponseEntity.badRequest()
                            .body(ApiResponse.error("INVALID_TRANSITION", "Invalid status transition"))
                    else -> 
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("INTERNAL_ERROR", "Failed to reject evidence"))
                }
            }
        )
    }
    
    @PostMapping("/{id}/lock")
    @PreAuthorize("hasRole('Admin')")
    @Operation(summary = "Lock evidence", description = "Locks approved evidence to prevent further changes (Admin role required)")
    fun lockEvidence(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String? = null
    ): ResponseEntity<ApiResponse<EvidenceDto>> {
        val tenantId = extractTenantId(jwt)
        val actor = extractActor(jwt)
        val corrId = correlationId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        
        val evidenceId = try {
            EvidenceId(UUID.fromString(id))
        } catch (e: Exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_ID", "Invalid evidence ID format"))
        }
        
        val result = evidenceService.lockEvidence(
            id = evidenceId,
            tenantId = TenantId(tenantId),
            actor = actor,
            correlationId = corrId
        )
        
        return result.fold(
            onSuccess = { evidence ->
                ResponseEntity.ok(ApiResponse.success(evidence.toDto()))
            },
            onFailure = { error ->
                when (error) {
                    is EvidenceError.NotFound -> 
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("NOT_FOUND", "Evidence not found"))
                    is EvidenceError.ValidationError -> 
                        ResponseEntity.badRequest()
                            .body(ApiResponse.error("VALIDATION_ERROR", error.message))
                    else -> 
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("INTERNAL_ERROR", "Failed to lock evidence"))
                }
            }
        )
    }
    
    private fun extractTenantId(jwt: Jwt): String {
        return jwt.getClaimAsString("tid") 
            ?: jwt.getClaimAsString("tenant_id") 
            ?: throw IllegalStateException("No tenant ID in token")
    }
    
    private fun extractActor(jwt: Jwt): String {
        return jwt.subject 
            ?: jwt.getClaimAsString("preferred_username") 
            ?: "unknown"
    }
}
