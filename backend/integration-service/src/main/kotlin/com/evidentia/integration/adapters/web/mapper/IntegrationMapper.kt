package com.evidentia.integration.adapters.web.mapper

import com.evidentia.integration.adapters.web.dto.IntegrationDto
import com.evidentia.integration.domain.Integration
import java.time.format.DateTimeFormatter

object IntegrationMapper {
    private val formatter = DateTimeFormatter.ISO_INSTANT
    
    fun Integration.toDto(): IntegrationDto {
        return IntegrationDto(
            id = id.value.toString(),
            tenantId = tenantId.value,
            type = type.name,
            name = name,
            description = description,
            status = status.name,
            configuration = configuration,
            createdAt = formatter.format(createdAt),
            updatedAt = formatter.format(updatedAt),
            lastSyncAt = lastSyncAt?.let { formatter.format(it) },
            errorMessage = errorMessage
        )
    }
}
