package com.evidentia.rating.adapters.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RatingJpaRepository : JpaRepository<RatingEntity, UUID> {
    fun findByTenantIdAndResourceTypeAndResourceIdOrderByCreatedAtDesc(
        tenantId: String,
        resourceType: String,
        resourceId: String
    ): List<RatingEntity>
    
    fun findByTenantIdAndRaterIdOrderByCreatedAtDesc(
        tenantId: String,
        raterId: String
    ): List<RatingEntity>
    
    fun findByTenantIdAndResourceTypeAndResourceIdAndRaterId(
        tenantId: String,
        resourceType: String,
        resourceId: String,
        raterId: String
    ): RatingEntity?
    
    @Query(
        "SELECT CAST(AVG(r.value) AS DOUBLE PRECISION) FROM RatingEntity r " +
        "WHERE r.tenantId = :tenantId AND r.resourceType = :resourceType AND r.resourceId = :resourceId"
    )
    fun getAverageRating(
        @Param("tenantId") tenantId: String,
        @Param("resourceType") resourceType: String,
        @Param("resourceId") resourceId: String
    ): Double?
    
    @Query(
        "SELECT COUNT(r) FROM RatingEntity r " +
        "WHERE r.tenantId = :tenantId AND r.resourceType = :resourceType AND r.resourceId = :resourceId"
    )
    fun countRatings(
        @Param("tenantId") tenantId: String,
        @Param("resourceType") resourceType: String,
        @Param("resourceId") resourceId: String
    ): Long
}
