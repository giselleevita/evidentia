package com.evidentia.integration.application

import com.evidentia.integration.adapters.persistence.WebhookDeliveryJpaRepository
import com.evidentia.integration.adapters.persistence.WebhookSubscriptionJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClient
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Background dispatcher that polls for pending webhook deliveries and attempts outbound HTTP POST.
 * Uses HMAC-SHA256 signing on X-Evidentia-Signature to allow subscribers to verify authenticity.
 */
@Service
class WebhookDispatchService(
    private val deliveryRepo: WebhookDeliveryJpaRepository,
    private val subRepo: WebhookSubscriptionJpaRepository,
    private val targetValidator: WebhookTargetValidator,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val http = RestClient.create()

    @Scheduled(fixedDelay = 5_000)   // poll every 5 seconds
    @Transactional
    fun dispatchPending() {
        val pending = deliveryRepo.findPendingDeliveries()
        for (delivery in pending) {
            val sub = subRepo.findById(delivery.subscriptionId).orElse(null) ?: continue
            try {
                targetValidator.validate(sub.targetUrl)
                val sig = hmacSha256(sub.secret, delivery.payloadJson)
                val resp = http.post()
                    .uri(sub.targetUrl)
                    .header("Content-Type", "application/json")
                    .header("X-Evidentia-Event", delivery.eventType)
                    .header("X-Evidentia-Signature", "sha256=$sig")
                    .header("X-Evidentia-Delivery", delivery.id.toString())
                    .body(delivery.payloadJson)
                    .retrieve()
                    .toBodilessEntity()

                delivery.lastResponseCode = resp.statusCode.value()
                delivery.lastAttemptedAt = Instant.now()
                delivery.attemptCount++
                if (resp.statusCode.is2xxSuccessful) {
                    delivery.deliveredAt = Instant.now()
                    log.info("Webhook delivered: delivery={} sub={}", delivery.id, sub.id)
                } else {
                    log.warn("Webhook non-2xx: delivery={} status={}", delivery.id, resp.statusCode)
                    if (delivery.attemptCount >= 5) delivery.failedAt = Instant.now()
                }
            } catch (ex: Exception) {
                log.error("Webhook dispatch error: delivery={} err={}", delivery.id, ex.message)
                delivery.attemptCount++
                delivery.lastAttemptedAt = Instant.now()
                if (delivery.attemptCount >= 5) delivery.failedAt = Instant.now()
            }
            deliveryRepo.save(delivery)
        }
    }

    private fun hmacSha256(secret: String, payload: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return mac.doFinal(payload.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}
