package com.evidentia.rating.domain

import com.evidentia.common.domain.TenantId
import java.time.Instant

data class Rating(
    val id: RatingId,
    val tenantId: TenantId,
    val raterId: String,
    val resourceType: String,
    val resourceId: String,
    val value: RatingValue,
    val comment: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    fun updateValue(newValue: RatingValue): Rating {
        return this.copy(
            value = newValue,
            updatedAt = Instant.now()
        )
    }
    
    fun updateComment(newComment: String?): Rating {
        return this.copy(
            comment = newComment,
            updatedAt = Instant.now()
        )
    }
}
