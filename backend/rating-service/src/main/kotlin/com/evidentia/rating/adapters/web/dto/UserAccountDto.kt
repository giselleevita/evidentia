package com.evidentia.rating.adapters.web.dto

data class UserAccountDto(
    val userId: String,
    val email: String?,
    val name: String?,
    val tenantId: String
)

data class UserRatingStatisticsDto(
    val userId: String,
    val totalRatingsGiven: Int,
    val averageRatingGiven: Double,
    val ratingDistribution: Map<Int, Int> // rating value -> count
)
