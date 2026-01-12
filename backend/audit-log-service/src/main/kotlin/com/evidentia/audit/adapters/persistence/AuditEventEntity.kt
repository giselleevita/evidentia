package com.evidentia.audit.adapters.persistence

import com.evidentia.common.domain.AuditEvent
import com.evidentia.common.domain.TenantId
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "audit_log",
    indexes = [
        Index(name = "idx_audit_tenant_id", columnList = "tenant_id"),
        Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        Index(name = "idx_audit_correlation_id", columnList = "correlation_id")
    ]
)
data class AuditEventEntity(
    @Id
    val id: UUID,
    
    @Column(name = "tenant_id", nullable = false)
    val tenantId: String,
    
    @Column(nullable = false)
    val actor: String,
    
    @Column(nullable = false)
    val action: String,
    
    @Column(name = "resource_type", nullable = false)
    val resourceType: String,
    
    @Column(name = "resource_id", nullable = false)
    val resourceId: String,
    
    @Column(name = "correlation_id", nullable = false)
    val correlationId: UUID,
    
    @Column(nullable = false)
    val timestamp: Instant,
    
    @Column(columnDefinition = "jsonb")
    @Convert(converter = MapConverter::class)
    val metadata: Map<String, String> = emptyMap()
) {
    fun toDomain(): AuditEvent {
        return AuditEvent(
            id = id,
            tenantId = TenantId(tenantId),
            actor = actor,
            action = action,
            resourceType = resourceType,
            resourceId = resourceId,
            correlationId = correlationId,
            timestamp = timestamp,
            metadata = metadata
        )
    }
    
    companion object {
        fun fromDomain(event: AuditEvent): AuditEventEntity {
            return AuditEventEntity(
                id = event.id,
                tenantId = event.tenantId.value,
                actor = event.actor,
                action = event.action,
                resourceType = event.resourceType,
                resourceId = event.resourceId,
                correlationId = event.correlationId,
                timestamp = event.timestamp,
                metadata = event.metadata
            )
        }
    }
}

@Converter
class MapConverter : AttributeConverter<Map<String, String>, String> {
    private val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()
    
    override fun convertToDatabaseColumn(attribute: Map<String, String>?): String? {
        return attribute?.let { objectMapper.writeValueAsString(it) }
    }
    
    override fun convertToEntityAttribute(dbData: String?): Map<String, String>? {
        return dbData?.let { 
            @Suppress("UNCHECKED_CAST")
            objectMapper.readValue(it, Map::class.java) as Map<String, String>
        }
    }
}
