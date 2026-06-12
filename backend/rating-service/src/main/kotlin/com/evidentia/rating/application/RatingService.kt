package com.evidentia.rating.application

import com.evidentia.common.domain.TenantId
import com.evidentia.rating.domain.Rating
import com.evidentia.rating.domain.RatingId
import com.evidentia.rating.domain.RatingValue
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.util.UUID

interface RatingRepository {
    fun save(rating: Rating): Rating
    fun findById(id: RatingId): Rating?
    fun findByTenantIdAndResource(tenantId: TenantId, resourceType: String, resourceId: String): List<Rating>
    fun findByTenantIdAndRaterId(tenantId: TenantId, raterId: String): List<Rating>
    fun findByTenantIdAndResourceAndRaterId(tenantId: TenantId, resourceType: String, resourceId: String, raterId: String): Rating?
    fun delete(id: RatingId)
    fun getAverageRatingForResource(tenantId: TenantId, resourceType: String, resourceId: String): Double?
    fun getRatingCountForResource(tenantId: TenantId, resourceType: String, resourceId: String): Int
}

@Service
class RatingService(
    private val repository: RatingRepository
) {
    fun createRating(
        tenantId: TenantId,
        raterId: String,
        resourceType: String,
        resourceId: String,
        value: RatingValue,
        comment: String? = null
    ): Rating {
        // Check if user already rated this resource
        val existingRating = repository.findByTenantIdAndResourceAndRaterId(
            tenantId, resourceType, resourceId, raterId
        )
        
        if (existingRating != null) {
            throw IllegalArgumentException("Rating already exists for this resource by this user")
        }
        
        val rating = Rating(
            id = RatingId.generate(),
            tenantId = tenantId,
            raterId = raterId,
            resourceType = resourceType,
            resourceId = resourceId,
            value = value,
            comment = comment
        )
        
        return try {
            repository.save(rating)
        } catch (e: DataIntegrityViolationException) {
            // Handle race condition where duplicate was created between check and save
            throw IllegalArgumentException("Rating already exists for this resource by this user", e)
        }
    }
    
    fun updateRating(
        ratingId: RatingId,
        tenantId: TenantId,
        newValue: RatingValue? = null,
        newComment: String? = null
    ): Rating {
        val existingRating = repository.findById(ratingId)
            ?: throw IllegalArgumentException("Rating not found")
        
        if (existingRating.tenantId != tenantId) {
            throw IllegalArgumentException("Rating does not belong to this tenant")
        }
        
        // Ensure at least one field is being updated
        if (newValue == null && newComment == null) {
            throw IllegalArgumentException("At least one field (value or comment) must be provided for update")
        }
        
        var updatedRating = existingRating
        if (newValue != null) {
            updatedRating = updatedRating.updateValue(newValue)
        }
        // Update comment if provided
        // Note: In JSON, we can't distinguish "field not provided" from "field is null"
        // So we update the comment if newComment is explicitly provided (not null)
        // To clear a comment, send empty string ""
        if (newComment != null) {
            // Treat empty string as clearing the comment (set to null)
            val commentToSet = if (newComment.isEmpty()) null else newComment
            updatedRating = updatedRating.updateComment(commentToSet)
        }
        
        return repository.save(updatedRating)
    }
    
    fun deleteRating(ratingId: RatingId, tenantId: TenantId) {
        val rating = repository.findById(ratingId)
            ?: throw IllegalArgumentException("Rating not found")
        
        if (rating.tenantId != tenantId) {
            throw IllegalArgumentException("Rating does not belong to this tenant")
        }
        
        repository.delete(ratingId)
    }
    
    fun getRating(ratingId: RatingId, tenantId: TenantId): Rating {
        val rating = repository.findById(ratingId)
            ?: throw IllegalArgumentException("Rating not found")
        
        if (rating.tenantId != tenantId) {
            throw IllegalArgumentException("Rating does not belong to this tenant")
        }
        
        return rating
    }
    
    fun getRatingsForResource(
        tenantId: TenantId,
        resourceType: String,
        resourceId: String
    ): List<Rating> {
        return repository.findByTenantIdAndResource(tenantId, resourceType, resourceId)
    }
    
    fun getRatingsByUser(tenantId: TenantId, raterId: String): List<Rating> {
        return repository.findByTenantIdAndRaterId(tenantId, raterId)
    }
    
    fun getResourceRatingSummary(
        tenantId: TenantId,
        resourceType: String,
        resourceId: String
    ): ResourceRatingSummary {
        val average = repository.getAverageRatingForResource(tenantId, resourceType, resourceId)
        val count = repository.getRatingCountForResource(tenantId, resourceType, resourceId)
        
        return ResourceRatingSummary(
            resourceType = resourceType,
            resourceId = resourceId,
            averageRating = average ?: 0.0,
            totalRatings = count
        )
    }
    
    fun getOrCreateUserRating(
        tenantId: TenantId,
        raterId: String,
        resourceType: String,
        resourceId: String
    ): Rating? {
        return repository.findByTenantIdAndResourceAndRaterId(tenantId, resourceType, resourceId, raterId)
    }
    
    fun getUserRatingStatistics(tenantId: TenantId, raterId: String): UserRatingStatistics {
        val ratings = repository.findByTenantIdAndRaterId(tenantId, raterId)
        
        if (ratings.isEmpty()) {
            return UserRatingStatistics(
                userId = raterId,
                totalRatingsGiven = 0,
                averageRatingGiven = 0.0,
                ratingDistribution = emptyMap()
            )
        }
        
        val totalRatings = ratings.size
        val averageRating = ratings.map { it.value.value }.average()
        val distribution = ratings.groupingBy { it.value.value }.eachCount()
        
        return UserRatingStatistics(
            userId = raterId,
            totalRatingsGiven = totalRatings,
            averageRatingGiven = averageRating,
            ratingDistribution = distribution
        )
    }
}

data class UserRatingStatistics(
    val userId: String,
    val totalRatingsGiven: Int,
    val averageRatingGiven: Double,
    val ratingDistribution: Map<Int, Int> // rating value -> count
)

data class ResourceRatingSummary(
    val resourceType: String,
    val resourceId: String,
    val averageRating: Double,
    val totalRatings: Int
)
