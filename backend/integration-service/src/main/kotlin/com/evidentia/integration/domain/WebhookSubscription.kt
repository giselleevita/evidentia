package com.evidentia.integration.domain

import java.time.Instant
import java.util.UUID

enum class WebhookStatus { ACTIVE, PAUSED, FAILED }

data class WebhookSubscription(
    val id: UUID,
    val tenantId: String,
    val targetUrl: String,
    val eventTypes: Set<String>,
    val status: WebhookStatus,
    val secret: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class WebhookDelivery(
    val id: UUID,
    val subscriptionId: UUID,
    val eventType: String,
    val payloadJson: String,
    val attemptCount: Int,
    val lastAttemptedAt: Instant?,
    val lastResponseCode: Int?,
    val deliveredAt: Instant?,
    val failedAt: Instant?,
    val createdAt: Instant,
)
