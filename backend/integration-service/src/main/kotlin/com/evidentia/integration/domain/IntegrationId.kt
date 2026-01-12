package com.evidentia.integration.domain

import java.util.UUID

data class IntegrationId(val value: UUID = UUID.randomUUID()) {
    override fun toString(): String = value.toString()
}
