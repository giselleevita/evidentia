package com.evidentia.rating.application

import com.evidentia.common.domain.TenantId
import com.evidentia.rating.domain.Rating
import com.evidentia.rating.domain.RatingId
import com.evidentia.rating.domain.RatingValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class RatingServiceTest {
    private val repository = InMemoryRatingRepository()
    private val service = RatingService(repository)
    private val tenantId = TenantId("tenant-a")

    @Test
    fun `duplicate ratings are rejected and statistics summarize saved ratings`() {
        service.createRating(tenantId, "alice", "Evidence", "ev-1", RatingValue.FIVE)
        service.createRating(tenantId, "alice", "Evidence", "ev-2", RatingValue.THREE)

        assertThrows(IllegalArgumentException::class.java) {
            service.createRating(tenantId, "alice", "Evidence", "ev-1", RatingValue.ONE)
        }

        val statistics = service.getUserRatingStatistics(tenantId, "alice")
        assertEquals(2, statistics.totalRatingsGiven)
        assertEquals(4.0, statistics.averageRatingGiven)
        assertEquals(mapOf(5 to 1, 3 to 1), statistics.ratingDistribution)
    }

    @Test
    fun `rating cannot be accessed through another tenant`() {
        val rating = service.createRating(tenantId, "alice", "Evidence", "ev-1", RatingValue.FOUR)

        assertThrows(IllegalArgumentException::class.java) {
            service.getRating(rating.id, TenantId("tenant-b"))
        }
    }

    private class InMemoryRatingRepository : RatingRepository {
        private val ratings = mutableMapOf<RatingId, Rating>()

        override fun save(rating: Rating): Rating {
            ratings[rating.id] = rating
            return rating
        }

        override fun findById(id: RatingId): Rating? = ratings[id]
        override fun findByTenantIdAndResource(tenantId: TenantId, resourceType: String, resourceId: String) =
            ratings.values.filter {
                it.tenantId == tenantId && it.resourceType == resourceType && it.resourceId == resourceId
            }

        override fun findByTenantIdAndRaterId(tenantId: TenantId, raterId: String) =
            ratings.values.filter { it.tenantId == tenantId && it.raterId == raterId }

        override fun findByTenantIdAndResourceAndRaterId(
            tenantId: TenantId,
            resourceType: String,
            resourceId: String,
            raterId: String
        ) = ratings.values.find {
            it.tenantId == tenantId &&
                it.resourceType == resourceType &&
                it.resourceId == resourceId &&
                it.raterId == raterId
        }

        override fun delete(id: RatingId) {
            ratings.remove(id)
        }

        override fun getAverageRatingForResource(tenantId: TenantId, resourceType: String, resourceId: String) =
            findByTenantIdAndResource(tenantId, resourceType, resourceId).map { it.value.value }.average()
                .takeUnless { it.isNaN() }

        override fun getRatingCountForResource(tenantId: TenantId, resourceType: String, resourceId: String) =
            findByTenantIdAndResource(tenantId, resourceType, resourceId).size
    }
}
