package com.evidentia.incident.adapters.audit

import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.toSubmission
import com.evidentia.common.security.AuthenticatedServiceRestClientFactory
import com.evidentia.incident.application.AuditEventClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

@Component
class AuditEventClientAdapter(
    restClientFactory: AuthenticatedServiceRestClientFactory,
    @Value("\${audit-log-service.url:http://localhost:8081}") auditServiceUrl: String,
    @Value("\${audit-log-service.client-registration-id:auditlog}") clientRegistrationId: String,
) : AuditEventClient {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = restClientFactory.create(auditServiceUrl, clientRegistrationId)
    
    override fun save(event: AuditEvent) {
        try {
            restClient.post()
                .uri("/api/v1/audit/events")
                .contentType(MediaType.APPLICATION_JSON)
                .body(event.toSubmission())
                .retrieve()
                .toBodilessEntity()
        } catch (e: Exception) {
            log.error("Failed to deliver audit event {}", event.id, e)
        }
    }
}
