package com.evidentia.evidence.adapters.persistence

import com.evidentia.common.domain.TenantId
import com.evidentia.evidence.domain.Evidence
import com.evidentia.evidence.domain.EvidenceId
import com.evidentia.evidence.domain.EvidenceStatus
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "evidence", indexes = [Index(name = "idx_tenant_id", columnList = "tenant_id")])
data class EvidenceEntity(
    @Id
    val id: UUID,
    
    @Column(name = "tenant_id", nullable = false)
    val tenantId: String,
    
    @Column(nullable = false)
    val title: String,
    
    @Column(columnDefinition = "TEXT")
    val description: String,
    
    @Column(nullable = false)
    val type: String,
    
    @Column(name = "source_system", nullable = false)
    val sourceSystem: String,
    
    @Column(nullable = false)
    val owner: String,
    
    @Column
    val approver: String?,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: EvidenceStatus,
    
    @Column(nullable = false)
    val version: Int,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant,
    
    @Column(name = "approved_at")
    val approvedAt: Instant?,
    
    @Column(columnDefinition = "jsonb")
    @Convert(converter = MapConverter::class)
    val references: Map<String, String> = emptyMap(),
    
    @Column(name = "attachment_ids", columnDefinition = "text[]")
    val attachmentIds: List<String> = emptyList()
) {
    fun toDomain(): Evidence {
        return Evidence(
            id = EvidenceId(id),
            tenantId = TenantId(tenantId),
            title = title,
            description = description,
            type = type,
            sourceSystem = sourceSystem,
            owner = owner,
            approver = approver,
            status = status,
            version = version,
            createdAt = createdAt,
            updatedAt = updatedAt,
            approvedAt = approvedAt,
            references = references,
            attachmentIds = attachmentIds
        )
    }
    
    companion object {
        fun fromDomain(evidence: Evidence): EvidenceEntity {
            return EvidenceEntity(
                id = evidence.id.value,
                tenantId = evidence.tenantId.value,
                title = evidence.title,
                description = evidence.description,
                type = evidence.type,
                sourceSystem = evidence.sourceSystem,
                owner = evidence.owner,
                approver = evidence.approver,
                status = evidence.status,
                version = evidence.version,
                createdAt = evidence.createdAt,
                updatedAt = evidence.updatedAt,
                approvedAt = evidence.approvedAt,
                references = evidence.references,
                attachmentIds = evidence.attachmentIds
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
