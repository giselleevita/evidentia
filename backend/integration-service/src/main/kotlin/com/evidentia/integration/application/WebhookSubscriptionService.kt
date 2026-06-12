package com.evidentia.integration.application

import com.evidentia.integration.adapters.persistence.WebhookDeliveryEntity
import com.evidentia.integration.adapters.persistence.WebhookDeliveryJpaRepository
import com.evidentia.integration.adapters.persistence.WebhookSubscriptionEntity
import com.evidentia.integration.adapters.persistence.WebhookSubscriptionJpaRepository
import com.evidentia.integration.domain.WebhookStatus
import com.evidentia.integration.domain.WebhookSubscription
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Instant
import java.util.*

@Service
class WebhookSubscriptionService(
    private val subRepo: WebhookSubscriptionJpaRepository,
    private val deliveryRepo: WebhookDeliveryJpaRepository,
    private val targetValidator: WebhookTargetValidator,
) {
    private val rng = SecureRandom()

    fun listForTenant(tenantId: String): List<WebhookSubscription> =
        subRepo.findAllByTenantId(tenantId).map(::toDomain)

    @Transactional
    fun create(tenantId: String, targetUrl: String, eventTypes: Set<String>): WebhookSubscription {
        targetValidator.validate(targetUrl)
        val secret = generateSecret()
        val entity = WebhookSubscriptionEntity(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            targetUrl = targetUrl,
            eventTypes = eventTypes,
            status = WebhookStatus.ACTIVE.name,
            secret = secret,
        )
        return toDomain(subRepo.save(entity))
    }

    @Transactional
    fun pause(id: UUID, tenantId: String): WebhookSubscription {
        val entity = subRepo.findById(id).orElseThrow { NoSuchElementException("Subscription $id not found") }
        require(entity.tenantId == tenantId) { "Forbidden" }
        entity.status = WebhookStatus.PAUSED.name
        entity.updatedAt = Instant.now()
        return toDomain(subRepo.save(entity))
    }

    @Transactional
    fun delete(id: UUID, tenantId: String) {
        val entity = subRepo.findById(id).orElseThrow { NoSuchElementException("Subscription $id not found") }
        require(entity.tenantId == tenantId) { "Forbidden" }
        subRepo.delete(entity)
    }

    /** Enqueue a delivery record for all ACTIVE subscriptions matching the event type. */
    @Transactional
    fun enqueueEvent(tenantId: String, eventType: String, payloadJson: String) {
        val subs = subRepo.findAllByTenantId(tenantId)
            .filter { it.status == WebhookStatus.ACTIVE.name && (it.eventTypes.isEmpty() || eventType in it.eventTypes) }
        subs.forEach { sub ->
            deliveryRepo.save(
                WebhookDeliveryEntity(
                    subscriptionId = sub.id,
                    eventType = eventType,
                    payloadJson = payloadJson,
                )
            )
        }
    }

    private fun generateSecret(): String {
        val bytes = ByteArray(32)
        rng.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun toDomain(e: WebhookSubscriptionEntity) = WebhookSubscription(
        id = e.id,
        tenantId = e.tenantId,
        targetUrl = e.targetUrl,
        eventTypes = e.eventTypes,
        status = WebhookStatus.valueOf(e.status),
        secret = e.secret,
        createdAt = e.createdAt,
        updatedAt = e.updatedAt,
    )
}
