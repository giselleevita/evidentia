package com.evidentia.rating.adapters.persistence

import com.evidentia.common.domain.TenantId
import com.evidentia.rating.application.RatingRepository
import com.evidentia.rating.domain.Rating
import com.evidentia.rating.domain.RatingId
import org.springframework.stereotype.Component

@Component
class RatingRepositoryAdapter(
    private val jpaRepository: RatingJpaRepository
) : RatingRepository {
    override fun save(rating: Rating): Rating {
        val entity = RatingEntity.fromDomain(rating)
        val saved = jpaRepository.save(entity)
        return saved.toDomain()
    }
    
    override fun findById(id: RatingId): Rating? {
        return jpaRepository.findById(id.value)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    override fun findByTenantIdAndResource(
        tenantId: TenantId,
        resourceType: String,
        resourceId: String
    ): List<Rating> {
        return jpaRepository.findByTenantIdAndResourceTypeAndResourceIdOrderByCreatedAtDesc(
            tenantId.value, resourceType, resourceId
        ).map { it.toDomain() }
    }
    
    override fun findByTenantIdAndRaterId(tenantId: TenantId, raterId: String): List<Rating> {
        return jpaRepository.findByTenantIdAndRaterIdOrderByCreatedAtDesc(
            tenantId.value, raterId
        ).map { it.toDomain() }
    }
    
    override fun findByTenantIdAndResourceAndRaterId(
        tenantId: TenantId,
        resourceType: String,
        resourceId: String,
        raterId: String
    ): Rating? {
        return jpaRepository.findByTenantIdAndResourceTypeAndResourceIdAndRaterId(
            tenantId.value, resourceType, resourceId, raterId
        )?.toDomain()
    }
    
    override fun delete(id: RatingId) {
        jpaRepository.deleteById(id.value)
    }
    
    override fun getAverageRatingForResource(
        tenantId: TenantId,
        resourceType: String,
        resourceId: String
    ): Double? {
        return jpaRepository.getAverageRating(tenantId.value, resourceType, resourceId)
    }
    
    override fun getRatingCountForResource(
        tenantId: TenantId,
        resourceType: String,
        resourceId: String
    ): Int {
        return jpaRepository.countRatings(tenantId.value, resourceType, resourceId).toInt()
    }
}
