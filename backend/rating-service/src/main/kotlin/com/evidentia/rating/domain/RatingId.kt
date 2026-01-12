package com.evidentia.rating.domain

import java.util.UUID

@JvmInline
value class RatingId(val value: UUID) {
    companion object {
        fun generate(): RatingId = RatingId(UUID.randomUUID())
        fun from(value: String): RatingId = RatingId(UUID.fromString(value))
    }
}
