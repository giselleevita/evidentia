package com.evidentia.integration.adapters.web.dto

import jakarta.validation.constraints.NotBlank

data class CreateIntegrationDto(
    @field:NotBlank(message = "Type is required")
    val type: String,
    
    @field:NotBlank(message = "Name is required")
    val name: String,
    
    val description: String? = null,
    
    val configuration: Map<String, String> = emptyMap()
)

data class UpdateIntegrationDto(
    val name: String? = null,
    val description: String? = null,
    val configuration: Map<String, String>? = null
)

data class IntegrationDto(
    val id: String,
    val tenantId: String,
    val type: String,
    val name: String,
    val description: String?,
    val status: String,
    val configuration: Map<String, String>,
    val createdAt: String,
    val updatedAt: String,
    val lastSyncAt: String?,
    val errorMessage: String?
)
