package com.evidentia.evidence.adapters.web.mapper

import com.evidentia.evidence.adapters.web.dto.EvidenceDto
import com.evidentia.evidence.domain.Evidence

/**
 * Mapper for converting between Evidence domain objects and DTOs.
 */
object EvidenceMapper {
    fun Evidence.toDto(): EvidenceDto {
        return EvidenceDto(
            id = id.value.toString(),
            tenantId = tenantId.value,
            title = title,
            description = description,
            type = type,
            sourceSystem = sourceSystem,
            owner = owner,
            approver = approver,
            status = status.name,
            version = version,
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
            approvedAt = approvedAt?.toString(),
            references = references,
            attachmentIds = attachmentIds
        )
    }
}
