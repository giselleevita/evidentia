package com.evidentia.integration.adapters.persistence

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "webhook_subscriptions")
class WebhookSubscriptionEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "tenant_id", nullable = false)
    val tenantId: String = "",

    @Column(name = "target_url", nullable = false)
    val targetUrl: String = "",

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "webhook_subscriptions", joinColumns = [JoinColumn(name = "id")])
    @Column(name = "event_types")
    val eventTypes: Set<String> = emptySet(),

    @Column(nullable = false)
    var status: String = "ACTIVE",

    @Column(nullable = false)
    val secret: String = "",

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)

@Entity
@Table(name = "webhook_deliveries")
class WebhookDeliveryEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "subscription_id", nullable = false)
    val subscriptionId: UUID = UUID.randomUUID(),

    @Column(name = "event_type", nullable = false)
    val eventType: String = "",

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    val payloadJson: String = "",

    @Column(name = "attempt_count", nullable = false)
    var attemptCount: Int = 0,

    @Column(name = "last_attempted_at")
    var lastAttemptedAt: Instant? = null,

    @Column(name = "last_response_code")
    var lastResponseCode: Int? = null,

    @Column(name = "last_response_body", columnDefinition = "TEXT")
    var lastResponseBody: String? = null,

    @Column(name = "delivered_at")
    var deliveredAt: Instant? = null,

    @Column(name = "failed_at")
    var failedAt: Instant? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)
