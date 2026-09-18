package com.evidentia.integration.application

import com.evidentia.integration.adapters.persistence.WebhookDeliveryJpaRepository
import com.evidentia.integration.adapters.persistence.WebhookSubscriptionJpaRepository
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val MAX_ATTEMPTS = 5
private const val MAX_RESPONSE_BYTES = 4096
private val BACKOFFS = listOf(5L, 30L, 120L, 600L)

internal enum class DeliveryOutcome { DELIVERED, RETRY, TERMINAL }

internal fun classifyDelivery(status: Int?, networkFailure: Boolean = false): DeliveryOutcome = when {
    networkFailure -> DeliveryOutcome.RETRY
    status != null && status in 200..299 -> DeliveryOutcome.DELIVERED
    status == 408 || status == 429 || (status != null && status >= 500) -> DeliveryOutcome.RETRY
    else -> DeliveryOutcome.TERMINAL
}

internal fun retryDelay(attemptCount: Int, retryAfter: String?, now: Instant): Duration {
    if (!retryAfter.isNullOrBlank()) {
        val seconds = retryAfter.toLongOrNull()
        val parsed = if (seconds != null) {
            Duration.ofSeconds(seconds.coerceAtLeast(0))
        } else {
            runCatching {
                Duration.between(now, ZonedDateTime.parse(retryAfter, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant())
            }.getOrNull()
        }
        if (parsed != null && !parsed.isNegative) return parsed.coerceAtMost(Duration.ofMinutes(15))
    }
    return Duration.ofSeconds(BACKOFFS[(attemptCount - 1).coerceIn(0, BACKOFFS.lastIndex)])
}

private data class HttpResult(val status: Int, val body: String, val retryAfter: String?)

@Service
class WebhookDispatchService(
    private val deliveryRepo: WebhookDeliveryJpaRepository,
    private val subRepo: WebhookSubscriptionJpaRepository,
    private val targetValidator: WebhookTargetValidator,
    transactionManager: PlatformTransactionManager,
    meterRegistry: MeterRegistry,
    @Value("\${evidentia.webhooks.batch-size:50}") private val batchSize: Int,
    @Value("\${evidentia.webhooks.lease-seconds:30}") private val leaseSeconds: Long,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val transactions = TransactionTemplate(transactionManager)
    private val workerId = UUID.randomUUID().toString()
    private val delivered = meterRegistry.counter("evidentia.webhooks.delivered")
    private val retried = meterRegistry.counter("evidentia.webhooks.retried")
    private val terminalFailures = meterRegistry.counter("evidentia.webhooks.terminal_failures")
    private val recoveredLeases = meterRegistry.counter("evidentia.webhooks.leases_recovered")
    private val http = RestClient.builder()
        .requestFactory(
            JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
            ).apply { setReadTimeout(Duration.ofSeconds(10)) }
        )
        .build()

    @Scheduled(fixedDelayString = "\${evidentia.webhooks.poll-delay-ms:5000}")
    fun dispatchPending() {
        claimDue(Instant.now()).forEach(::dispatchOne)
    }

    private fun claimDue(now: Instant): List<UUID> = transactions.execute {
        deliveryRepo.findClaimableDeliveries(now, batchSize.coerceIn(1, 50)).map { delivery ->
            if (delivery.leaseUntil != null) recoveredLeases.increment()
            delivery.leaseOwner = workerId
            delivery.leaseUntil = now.plusSeconds(leaseSeconds.coerceIn(15, 300))
            deliveryRepo.save(delivery)
            delivery.id
        }
    } ?: emptyList()

    private fun dispatchOne(deliveryId: UUID) {
        val delivery = deliveryRepo.findById(deliveryId).orElse(null) ?: return
        if (delivery.leaseOwner != workerId) return
        val subscription = subRepo.findById(delivery.subscriptionId).orElse(null)
        if (subscription == null) {
            complete(deliveryId, DeliveryOutcome.TERMINAL, null, "subscription missing", null)
            return
        }

        try {
            targetValidator.validate(subscription.targetUrl)
            val signature = hmacSha256(subscription.secret, delivery.payloadJson)
            val result = http.post()
                .uri(subscription.targetUrl)
                .header("Content-Type", "application/json")
                .header("X-Evidentia-Event", delivery.eventType)
                .header("X-Evidentia-Signature", "sha256=$signature")
                .header("X-Evidentia-Delivery", delivery.id.toString())
                .header("X-Evidentia-Timestamp", Instant.now().epochSecond.toString())
                .body(delivery.payloadJson)
                .exchange { _, response ->
                    val bytes = response.body.readNBytes(MAX_RESPONSE_BYTES + 1)
                    HttpResult(
                        response.statusCode.value(),
                        String(bytes.copyOfRange(0, minOf(bytes.size, MAX_RESPONSE_BYTES)), StandardCharsets.UTF_8),
                        response.headers.getFirst("Retry-After"),
                    )
                }
            complete(deliveryId, classifyDelivery(result.status), result.status, result.body, result.retryAfter)
        } catch (exception: IllegalArgumentException) {
            log.warn("Webhook target rejected: delivery={} type={}", deliveryId, exception.javaClass.simpleName)
            complete(deliveryId, DeliveryOutcome.TERMINAL, null, exception.javaClass.simpleName, null)
        } catch (exception: Exception) {
            log.warn("Webhook dispatch failed: delivery={} type={}", deliveryId, exception.javaClass.simpleName)
            complete(deliveryId, DeliveryOutcome.RETRY, null, exception.javaClass.simpleName, null)
        }
    }

    private fun complete(
        deliveryId: UUID,
        outcome: DeliveryOutcome,
        status: Int?,
        responseBody: String?,
        retryAfter: String?,
    ) {
        transactions.executeWithoutResult {
            val delivery = deliveryRepo.findById(deliveryId).orElse(null) ?: return@executeWithoutResult
            if (delivery.leaseOwner != workerId) return@executeWithoutResult
            val now = Instant.now()
            delivery.attemptCount += 1
            delivery.lastAttemptedAt = now
            delivery.lastResponseCode = status
            delivery.lastResponseBody = responseBody?.take(MAX_RESPONSE_BYTES)
            delivery.leaseOwner = null
            delivery.leaseUntil = null
            when {
                outcome == DeliveryOutcome.DELIVERED -> {
                    delivery.deliveredAt = now
                    delivered.increment()
                }
                outcome == DeliveryOutcome.TERMINAL || delivery.attemptCount >= MAX_ATTEMPTS -> {
                    delivery.failedAt = now
                    terminalFailures.increment()
                }
                else -> {
                    delivery.nextAttemptAt = now.plus(retryDelay(delivery.attemptCount, retryAfter, now))
                    retried.increment()
                }
            }
            deliveryRepo.save(delivery)
        }
    }

    private fun hmacSha256(secret: String, payload: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return mac.doFinal(payload.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}
