package com.evidentia.integration.application

import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.TenantId
import com.evidentia.integration.domain.Integration
import com.evidentia.integration.domain.IntegrationId
import com.evidentia.integration.domain.IntegrationStatus
import com.evidentia.integration.domain.IntegrationType
import java.time.Instant

interface IntegrationRepository {
    fun save(integration: Integration): Integration
    fun findById(id: IntegrationId, tenantId: TenantId): Integration?
    fun findAll(tenantId: TenantId): List<Integration>
    fun findByType(tenantId: TenantId, type: IntegrationType): List<Integration>
    fun delete(id: IntegrationId, tenantId: TenantId): Boolean
}

interface AuditEventClient {
    fun save(event: AuditEvent)
}

data class CreateIntegrationRequest(
    val type: IntegrationType,
    val name: String,
    val description: String? = null,
    val configuration: Map<String, String> = emptyMap()
)

data class UpdateIntegrationRequest(
    val name: String? = null,
    val description: String? = null,
    val configuration: Map<String, String>? = null
)

sealed class IntegrationError {
    data object NotFound : IntegrationError()
    data class ValidationError(val message: String) : IntegrationError()
}

class IntegrationService(
    private val integrationRepository: IntegrationRepository,
    private val auditEventClient: AuditEventClient
) {
    fun createIntegration(
        tenantId: TenantId,
        request: CreateIntegrationRequest,
        actor: String,
        correlationId: java.util.UUID
    ): Result<Integration, IntegrationError> {
        val integration = Integration(
            id = IntegrationId(),
            tenantId = tenantId,
            type = request.type,
            name = request.name,
            description = request.description,
            status = IntegrationStatus.INACTIVE,
            configuration = request.configuration,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        
        val saved = integrationRepository.save(integration)
        
        auditEventClient.save(
            AuditEvent(
                tenantId = tenantId,
                actor = actor,
                action = "integration.created",
                resourceType = "Integration",
                resourceId = saved.id.value.toString(),
                correlationId = correlationId,
                metadata = mapOf(
                    "type" to request.type.name,
                    "name" to request.name
                )
            )
        )
        
        return Result.success(saved)
    }
    
    fun updateIntegration(
        id: IntegrationId,
        tenantId: TenantId,
        request: UpdateIntegrationRequest,
        actor: String,
        correlationId: java.util.UUID
    ): Result<Integration, IntegrationError> {
        val integration = integrationRepository.findById(id, tenantId)
            ?: return Result.failure(IntegrationError.NotFound)
        
        val updated = integration.copy(
            name = request.name ?: integration.name,
            description = request.description ?: integration.description,
            configuration = request.configuration ?: integration.configuration,
            updatedAt = Instant.now()
        )
        
        val saved = integrationRepository.save(updated)
        
        auditEventClient.save(
            AuditEvent(
                tenantId = tenantId,
                actor = actor,
                action = "integration.updated",
                resourceType = "Integration",
                resourceId = saved.id.value.toString(),
                correlationId = correlationId,
                metadata = mapOf("name" to saved.name)
            )
        )
        
        return Result.success(saved)
    }
    
    fun activateIntegration(
        id: IntegrationId,
        tenantId: TenantId,
        actor: String,
        correlationId: java.util.UUID
    ): Result<Integration, IntegrationError> {
        val integration = integrationRepository.findById(id, tenantId)
            ?: return Result.failure(IntegrationError.NotFound)
        
        val updated = integration.copy(
            status = IntegrationStatus.ACTIVE,
            updatedAt = Instant.now(),
            errorMessage = null
        )
        
        val saved = integrationRepository.save(updated)
        
        auditEventClient.save(
            AuditEvent(
                tenantId = tenantId,
                actor = actor,
                action = "integration.activated",
                resourceType = "Integration",
                resourceId = saved.id.value.toString(),
                correlationId = correlationId
            )
        )
        
        return Result.success(saved)
    }
    
    fun deactivateIntegration(
        id: IntegrationId,
        tenantId: TenantId,
        actor: String,
        correlationId: java.util.UUID
    ): Result<Integration, IntegrationError> {
        val integration = integrationRepository.findById(id, tenantId)
            ?: return Result.failure(IntegrationError.NotFound)
        
        val updated = integration.copy(
            status = IntegrationStatus.INACTIVE,
            updatedAt = Instant.now()
        )
        
        val saved = integrationRepository.save(updated)
        
        auditEventClient.save(
            AuditEvent(
                tenantId = tenantId,
                actor = actor,
                action = "integration.deactivated",
                resourceType = "Integration",
                resourceId = saved.id.value.toString(),
                correlationId = correlationId
            )
        )
        
        return Result.success(saved)
    }
    
    fun deleteIntegration(
        id: IntegrationId,
        tenantId: TenantId,
        actor: String,
        correlationId: java.util.UUID
    ): Result<Unit, IntegrationError> {
        val integration = integrationRepository.findById(id, tenantId)
            ?: return Result.failure(IntegrationError.NotFound)
        
        val deleted = integrationRepository.delete(id, tenantId)
        if (!deleted) {
            return Result.failure(IntegrationError.NotFound)
        }
        
        auditEventClient.save(
            AuditEvent(
                tenantId = tenantId,
                actor = actor,
                action = "integration.deleted",
                resourceType = "Integration",
                resourceId = id.value.toString(),
                correlationId = correlationId,
                metadata = mapOf("name" to integration.name)
            )
        )
        
        return Result.success(Unit)
    }
    
    fun getIntegration(id: IntegrationId, tenantId: TenantId): Result<Integration, IntegrationError> {
        return integrationRepository.findById(id, tenantId)
            ?.let { Result.success(it) }
            ?: Result.failure(IntegrationError.NotFound)
    }
    
    fun listIntegrations(tenantId: TenantId, type: IntegrationType? = null): List<Integration> {
        return if (type != null) {
            integrationRepository.findByType(tenantId, type)
        } else {
            integrationRepository.findAll(tenantId)
        }
    }
}
