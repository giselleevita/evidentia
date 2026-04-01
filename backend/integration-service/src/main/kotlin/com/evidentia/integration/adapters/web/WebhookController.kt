package com.evidentia.integration.adapters.web

import com.evidentia.common.context.TenantContext
import com.evidentia.common.web.ApiResponse
import com.evidentia.integration.application.WebhookSubscriptionService
import com.evidentia.integration.domain.WebhookSubscription
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

data class CreateWebhookRequest(
    @field:NotBlank val targetUrl: String,
    @field:NotEmpty val eventTypes: Set<String>,
)

data class WebhookDto(
    val id: UUID,
    val targetUrl: String,
    val eventTypes: Set<String>,
    val status: String,
    val createdAt: String,
)

@RestController
@RequestMapping("/api/v1/webhooks")
@Tag(name = "Webhooks", description = "Webhook subscription management")
class WebhookController(private val svc: WebhookSubscriptionService) {

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'Auditor')")
    fun list(): ApiResponse<List<WebhookDto>> {
        val tenantId = TenantContext.current()
        val items = svc.listForTenant(tenantId).map(::toDto)
        return ApiResponse.ok(items)
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody req: CreateWebhookRequest): ApiResponse<WebhookDto> {
        val tenantId = TenantContext.current()
        val sub = svc.create(tenantId, req.targetUrl, req.eventTypes)
        return ApiResponse.ok(toDto(sub))
    }

    @PatchMapping("/{id}/pause")
    @PreAuthorize("hasRole('Admin')")
    fun pause(@PathVariable id: UUID): ApiResponse<WebhookDto> {
        val tenantId = TenantContext.current()
        val sub = svc.pause(id, tenantId)
        return ApiResponse.ok(toDto(sub))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID) {
        val tenantId = TenantContext.current()
        svc.delete(id, tenantId)
    }

    private fun toDto(s: WebhookSubscription) = WebhookDto(
        id = s.id,
        targetUrl = s.targetUrl,
        eventTypes = s.eventTypes,
        status = s.status.name,
        createdAt = s.createdAt.toString(),
    )
}
