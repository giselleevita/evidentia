package com.evidentia.rating.adapters.web.mapper

import com.evidentia.rating.adapters.web.dto.*
import com.evidentia.rating.application.ResourceRatingSummary
import com.evidentia.rating.application.UserRatingStatistics
import com.evidentia.rating.domain.Rating
import com.evidentia.rating.domain.RatingValue

object RatingMapper {
    fun toDto(rating: Rating): RatingDto {
        return RatingDto(
            id = rating.id.value,
            raterId = rating.raterId,
            resourceType = rating.resourceType,
            resourceId = rating.resourceId,
            value = rating.value.value,
            comment = rating.comment,
            createdAt = rating.createdAt,
            updatedAt = rating.updatedAt
        )
    }
    
    fun toDto(summary: ResourceRatingSummary): ResourceRatingSummaryDto {
        return ResourceRatingSummaryDto(
            resourceType = summary.resourceType,
            resourceId = summary.resourceId,
            averageRating = summary.averageRating,
            totalRatings = summary.totalRatings
        )
    }
    
    fun toDto(stats: UserRatingStatistics): UserRatingStatisticsDto {
        return UserRatingStatisticsDto(
            userId = stats.userId,
            totalRatingsGiven = stats.totalRatingsGiven,
            averageRatingGiven = stats.averageRatingGiven,
            ratingDistribution = stats.ratingDistribution
        )
    }
    
    fun toRatingValue(value: Int): RatingValue {
        return RatingValue.fromInt(value)
    }
}
