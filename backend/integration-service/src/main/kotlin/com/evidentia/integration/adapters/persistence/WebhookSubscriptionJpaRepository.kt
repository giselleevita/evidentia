package com.evidentia.integration.adapters.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface WebhookSubscriptionJpaRepository : JpaRepository<WebhookSubscriptionEntity, UUID> {
    fun findAllByTenantId(tenantId: String): List<WebhookSubscriptionEntity>
    fun findAllByStatus(status: String): List<WebhookSubscriptionEntity>
}

interface WebhookDeliveryJpaRepository : JpaRepository<WebhookDeliveryEntity, UUID> {
    @Query(
        "SELECT d FROM WebhookDeliveryEntity d " +
        "WHERE d.deliveredAt IS NULL AND d.failedAt IS NULL AND d.attemptCount < 5 " +
        "ORDER BY d.createdAt ASC"
    )
    fun findPendingDeliveries(): List<WebhookDeliveryEntity>
}
