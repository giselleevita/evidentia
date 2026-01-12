package com.evidentia.incident.adapters.audit

import com.evidentia.common.domain.AuditEvent
import com.evidentia.incident.application.AuditEventClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class AuditEventClientAdapter(
    @Value("\${audit-log-service.url:http://localhost:8081}") private val auditServiceUrl: String
) : AuditEventClient {
    private val restClient = RestClient.builder()
        .baseUrl(auditServiceUrl)
        .build()
    
    override fun save(event: AuditEvent) {
        try {
            restClient.post()
                .uri("/api/v1/audit/events")
                .contentType(MediaType.APPLICATION_JSON)
                .body(event)
                .retrieve()
                .toBodilessEntity()
        } catch (e: Exception) {
            // Log error but don't fail the main operation
            println("Failed to save audit event: ${e.message}")
        }
    }
}
