package com.evidentia.integration.application

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class WebhookDispatchPolicyTest {
    private val now = Instant.parse("2026-09-18T10:00:00Z")

    @Test
    fun `classifies retryable and terminal responses`() {
        assertEquals(DeliveryOutcome.DELIVERED, classifyDelivery(204))
        assertEquals(DeliveryOutcome.RETRY, classifyDelivery(408))
        assertEquals(DeliveryOutcome.RETRY, classifyDelivery(429))
        assertEquals(DeliveryOutcome.RETRY, classifyDelivery(503))
        assertEquals(DeliveryOutcome.RETRY, classifyDelivery(null, networkFailure = true))
        assertEquals(DeliveryOutcome.TERMINAL, classifyDelivery(400))
        assertEquals(DeliveryOutcome.TERMINAL, classifyDelivery(404))
    }

    @Test
    fun `uses exact bounded exponential backoff`() {
        assertEquals(Duration.ofSeconds(5), retryDelay(1, null, now))
        assertEquals(Duration.ofSeconds(30), retryDelay(2, null, now))
        assertEquals(Duration.ofMinutes(2), retryDelay(3, null, now))
        assertEquals(Duration.ofMinutes(10), retryDelay(4, null, now))
    }

    @Test
    fun `honors retry after seconds and caps long delays`() {
        assertEquals(Duration.ofSeconds(45), retryDelay(1, "45", now))
        assertEquals(Duration.ofMinutes(15), retryDelay(1, "7200", now))
    }

    @Test
    fun `honors retry after http dates`() {
        val retryAt = now.plusSeconds(90)
            .atZone(ZoneOffset.UTC)
            .format(DateTimeFormatter.RFC_1123_DATE_TIME)
        assertEquals(Duration.ofSeconds(90), retryDelay(1, retryAt, now))
    }

    @Test
    fun `falls back when retry after is invalid`() {
        assertEquals(Duration.ofSeconds(30), retryDelay(2, "invalid", now))
    }
}
