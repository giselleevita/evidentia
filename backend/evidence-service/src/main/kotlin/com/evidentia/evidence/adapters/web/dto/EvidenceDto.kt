package com.evidentia.evidence.adapters.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateEvidenceDto(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 500, message = "Title must not exceed 500 characters")
    val title: String,
    
    @field:NotBlank(message = "Description is required")
    val description: String,
    
    @field:NotBlank(message = "Type is required")
    val type: String,
    
    @field:NotBlank(message = "Source system is required")
    val sourceSystem: String,
    
    @field:NotBlank(message = "Owner is required")
    val owner: String,
    
    val references: Map<String, String> = emptyMap()
)

data class UpdateEvidenceDto(
    @field:Size(max = 500, message = "Title must not exceed 500 characters")
    val title: String? = null,
    
    val description: String? = null,
    
    val type: String? = null,
    
    val sourceSystem: String? = null,
    
    val references: Map<String, String>? = null
)

data class SubmitEvidenceDto(
    val note: String? = null
)

data class ApproveEvidenceDto(
    val note: String? = null
)

data class RejectEvidenceDto(
    @field:NotBlank(message = "Rejection reason is required")
    val reason: String
)

data class EvidenceDto(
    val id: String,
    val tenantId: String,
    val title: String,
    val description: String,
    val type: String,
    val sourceSystem: String,
    val owner: String,
    val approver: String?,
    val status: String,
    val version: Int,
    val createdAt: String,
    val updatedAt: String,
    val approvedAt: String?,
    val references: Map<String, String>,
    val attachmentIds: List<String>
)
