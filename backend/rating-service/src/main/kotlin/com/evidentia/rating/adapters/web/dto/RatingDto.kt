package com.evidentia.rating.adapters.web.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class CreateRatingRequest(
    @field:NotBlank(message = "Resource type is required")
    val resourceType: String,
    
    @field:NotBlank(message = "Resource ID is required")
    val resourceId: String,
    
    @field:Min(value = 1, message = "Rating value must be at least 1")
    @field:Max(value = 5, message = "Rating value must be at most 5")
    val value: Int,
    
    @field:Size(max = 1000, message = "Comment must not exceed 1000 characters")
    val comment: String? = null
)

data class UpdateRatingRequest(
    @field:Min(value = 1, message = "Rating value must be at least 1")
    @field:Max(value = 5, message = "Rating value must be at most 5")
    val value: Int? = null,
    
    @field:Size(max = 1000, message = "Comment must not exceed 1000 characters")
    val comment: String? = null
)

data class RatingDto(
    val id: UUID,
    val raterId: String,
    val resourceType: String,
    val resourceId: String,
    val value: Int,
    val comment: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class ResourceRatingSummaryDto(
    val resourceType: String,
    val resourceId: String,
    val averageRating: Double,
    val totalRatings: Int
)
