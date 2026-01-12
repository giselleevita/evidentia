package com.evidentia.integration.adapters.web

import com.evidentia.common.domain.TenantId
import com.evidentia.common.web.ApiResponse
import com.evidentia.common.web.JwtUtils
import com.evidentia.integration.adapters.web.dto.*
import com.evidentia.integration.adapters.web.mapper.IntegrationMapper.toDto
import com.evidentia.integration.application.*
import com.evidentia.integration.domain.IntegrationId
import com.evidentia.integration.domain.IntegrationType
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
@RequestMapping("/api/v1/integrations")
@Tag(name = "Integration", description = "External system integrations management API")
class IntegrationController(
    private val integrationService: IntegrationService
) {
    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    @Operation(summary = "Create new integration", description = "Creates a new integration configuration (Admin role required)")
    fun createIntegration(
        @Valid @RequestBody request: CreateIntegrationDto,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String? = null
    ): ResponseEntity<ApiResponse<IntegrationDto>> {
        val tenantId = JwtUtils.extractTenantId(jwt)
        val actor = JwtUtils.extractActor(jwt)
        val corrId = correlationId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        
        val type = try {
            IntegrationType.valueOf(request.type.uppercase())
        } catch (e: Exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", "Invalid integration type"))
        }
        
        val result = integrationService.createIntegration(
            tenantId = TenantId(tenantId),
            request = CreateIntegrationRequest(
                type = type,
                name = request.name,
                description = request.description,
                configuration = request.configuration
            ),
            actor = actor,
            correlationId = corrId
        )
        
        return result.fold(
            onSuccess = { integration ->
                ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(integration.toDto()))
            },
            onFailure = { error ->
                when (error) {
                    is IntegrationError.ValidationError ->
                        ResponseEntity.badRequest()
                            .body(ApiResponse.error("VALIDATION_ERROR", error.message))
                    else ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("INTERNAL_ERROR", "Failed to create integration"))
                }
            }
        )
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get integration by ID")
    fun getIntegration(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<ApiResponse<IntegrationDto>> {
        val tenantId = JwtUtils.extractTenantId(jwt)
        val integrationId = try {
            IntegrationId(UUID.fromString(id))
        } catch (e: Exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_ID", "Invalid integration ID format"))
        }
        
        val result = integrationService.getIntegration(integrationId, TenantId(tenantId))
        
        return result.fold(
            onSuccess = { integration ->
                ResponseEntity.ok(ApiResponse.success(integration.toDto()))
            },
            onFailure = { error ->
                when (error) {
                    is IntegrationError.NotFound ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("NOT_FOUND", "Integration not found"))
                    else ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("INTERNAL_ERROR", "Failed to get integration"))
                }
            }
        )
    }
    
    @GetMapping
    @Operation(summary = "List integrations", description = "Lists all integrations, optionally filtered by type")
    fun listIntegrations(
        @RequestParam(required = false) type: String?,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<ApiResponse<List<IntegrationDto>>> {
        val tenantId = JwtUtils.extractTenantId(jwt)
        
        val integrationType = type?.let {
            try {
                IntegrationType.valueOf(it.uppercase())
            } catch (e: Exception) {
                null
            }
        }
        
        val integrations = integrationService.listIntegrations(TenantId(tenantId), integrationType)
        
        return ResponseEntity.ok(ApiResponse.success(integrations.map { it.toDto() }))
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    @Operation(summary = "Update integration", description = "Updates integration configuration (Admin role required)")
    fun updateIntegration(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateIntegrationDto,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String? = null
    ): ResponseEntity<ApiResponse<IntegrationDto>> {
        val tenantId = JwtUtils.extractTenantId(jwt)
        val actor = JwtUtils.extractActor(jwt)
        val corrId = correlationId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        
        val integrationId = try {
            IntegrationId(UUID.fromString(id))
        } catch (e: Exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_ID", "Invalid integration ID format"))
        }
        
        val result = integrationService.updateIntegration(
            id = integrationId,
            tenantId = TenantId(tenantId),
            request = UpdateIntegrationRequest(
                name = request.name,
                description = request.description,
                configuration = request.configuration
            ),
            actor = actor,
            correlationId = corrId
        )
        
        return result.fold(
            onSuccess = { integration ->
                ResponseEntity.ok(ApiResponse.success(integration.toDto()))
            },
            onFailure = { error ->
                when (error) {
                    is IntegrationError.NotFound ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("NOT_FOUND", "Integration not found"))
                    else ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("INTERNAL_ERROR", "Failed to update integration"))
                }
            }
        )
    }
    
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('Admin')")
    @Operation(summary = "Activate integration", description = "Activates an integration (Admin role required)")
    fun activateIntegration(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String? = null
    ): ResponseEntity<ApiResponse<IntegrationDto>> {
        val tenantId = JwtUtils.extractTenantId(jwt)
        val actor = JwtUtils.extractActor(jwt)
        val corrId = correlationId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        
        val integrationId = try {
            IntegrationId(UUID.fromString(id))
        } catch (e: Exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_ID", "Invalid integration ID format"))
        }
        
        val result = integrationService.activateIntegration(integrationId, TenantId(tenantId), actor, corrId)
        
        return result.fold(
            onSuccess = { integration ->
                ResponseEntity.ok(ApiResponse.success(integration.toDto()))
            },
            onFailure = { error ->
                when (error) {
                    is IntegrationError.NotFound ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("NOT_FOUND", "Integration not found"))
                    else ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("INTERNAL_ERROR", "Failed to activate integration"))
                }
            }
        )
    }
    
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('Admin')")
    @Operation(summary = "Deactivate integration", description = "Deactivates an integration (Admin role required)")
    fun deactivateIntegration(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String? = null
    ): ResponseEntity<ApiResponse<IntegrationDto>> {
        val tenantId = JwtUtils.extractTenantId(jwt)
        val actor = JwtUtils.extractActor(jwt)
        val corrId = correlationId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        
        val integrationId = try {
            IntegrationId(UUID.fromString(id))
        } catch (e: Exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_ID", "Invalid integration ID format"))
        }
        
        val result = integrationService.deactivateIntegration(integrationId, TenantId(tenantId), actor, corrId)
        
        return result.fold(
            onSuccess = { integration ->
                ResponseEntity.ok(ApiResponse.success(integration.toDto()))
            },
            onFailure = { error ->
                when (error) {
                    is IntegrationError.NotFound ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("NOT_FOUND", "Integration not found"))
                    else ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("INTERNAL_ERROR", "Failed to deactivate integration"))
                }
            }
        )
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    @Operation(summary = "Delete integration", description = "Deletes an integration (Admin role required)")
    fun deleteIntegration(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(value = "X-Correlation-Id", required = false) correlationId: String? = null
    ): ResponseEntity<ApiResponse<Unit>> {
        val tenantId = JwtUtils.extractTenantId(jwt)
        val actor = JwtUtils.extractActor(jwt)
        val corrId = correlationId?.let { UUID.fromString(it) } ?: UUID.randomUUID()
        
        val integrationId = try {
            IntegrationId(UUID.fromString(id))
        } catch (e: Exception) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("INVALID_ID", "Invalid integration ID format"))
        }
        
        val result = integrationService.deleteIntegration(integrationId, TenantId(tenantId), actor, corrId)
        
        return result.fold(
            onSuccess = {
                ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success(Unit))
            },
            onFailure = { error ->
                when (error) {
                    is IntegrationError.NotFound ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("NOT_FOUND", "Integration not found"))
                    else ->
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("INTERNAL_ERROR", "Failed to delete integration"))
                }
            }
        )
    }
}
