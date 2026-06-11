package com.evidentia.common.domain

sealed interface Result<out T, out E> {
    data class Success<T>(val value: T) : Result<T, Nothing>
    data class Failure<E>(val error: E) : Result<Nothing, E>

    fun <R> fold(onSuccess: (T) -> R, onFailure: (E) -> R): R =
        when (this) {
            is Success -> onSuccess(value)
            is Failure -> onFailure(error)
        }

    companion object {
        fun <T> success(value: T): Result<T, Nothing> = Success(value)
        fun <E> failure(error: E): Result<Nothing, E> = Failure(error)
    }
}
