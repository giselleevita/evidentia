package com.evidentia.integration.adapters.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface WebhookSubscriptionJpaRepository : JpaRepository<WebhookSubscriptionEntity, UUID> {
    fun findAllByTenantId(tenantId: String): List<WebhookSubscriptionEntity>
    fun findAllByStatus(status: String): List<WebhookSubscriptionEntity>
}

interface WebhookDeliveryJpaRepository : JpaRepository<WebhookDeliveryEntity, UUID> {
    @Query(
        value = """
            SELECT * FROM webhook_deliveries
            WHERE delivered_at IS NULL
              AND failed_at IS NULL
              AND attempt_count < 5
              AND next_attempt_at <= :now
              AND (lease_until IS NULL OR lease_until < :now)
            ORDER BY next_attempt_at ASC, created_at ASC
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true,
    )
    fun findClaimableDeliveries(
        @Param("now") now: Instant,
        @Param("batchSize") batchSize: Int,
    ): List<WebhookDeliveryEntity>
}
