package com.evidentia.rating.adapters.persistence

import com.evidentia.common.domain.TenantId
import com.evidentia.rating.domain.Rating
import com.evidentia.rating.domain.RatingId
import com.evidentia.rating.domain.RatingValue
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "ratings",
    indexes = [
        Index(name = "idx_rating_tenant_resource", columnList = "tenant_id,resource_type,resource_id"),
        Index(name = "idx_rating_tenant_rater", columnList = "tenant_id,rater_id"),
        Index(name = "idx_rating_resource_unique", columnList = "tenant_id,resource_type,resource_id,rater_id", unique = true)
    ]
)
data class RatingEntity(
    @Id
    val id: UUID,
    
    @Column(name = "tenant_id", nullable = false)
    val tenantId: String,
    
    @Column(name = "rater_id", nullable = false)
    val raterId: String,
    
    @Column(name = "resource_type", nullable = false)
    val resourceType: String,
    
    @Column(name = "resource_id", nullable = false)
    val resourceId: String,
    
    @Column(nullable = false)
    val value: Int,
    
    @Column(columnDefinition = "TEXT")
    val comment: String? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant
) {
    fun toDomain(): Rating {
        return Rating(
            id = RatingId(id),
            tenantId = TenantId(tenantId),
            raterId = raterId,
            resourceType = resourceType,
            resourceId = resourceId,
            value = RatingValue.fromInt(value),
            comment = comment,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        fun fromDomain(rating: Rating): RatingEntity {
            return RatingEntity(
                id = rating.id.value,
                tenantId = rating.tenantId.value,
                raterId = rating.raterId,
                resourceType = rating.resourceType,
                resourceId = rating.resourceId,
                value = rating.value.value,
                comment = rating.comment,
                createdAt = rating.createdAt,
                updatedAt = rating.updatedAt
            )
        }
    }
}
