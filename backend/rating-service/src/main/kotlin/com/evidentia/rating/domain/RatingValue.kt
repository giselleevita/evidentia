package com.evidentia.rating.domain

enum class RatingValue(val value: Int) {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5);
    
    companion object {
        fun fromInt(value: Int): RatingValue {
            return when (value) {
                1 -> ONE
                2 -> TWO
                3 -> THREE
                4 -> FOUR
                5 -> FIVE
                else -> throw IllegalArgumentException("Rating value must be between 1 and 5, got: $value")
            }
        }
    }
}
