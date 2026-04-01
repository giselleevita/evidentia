package com.evidentia.common.web

import java.time.Instant

/**
 * Standardized API response wrapper for consistent response format across all services.
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null,
    val timestamp: Instant = Instant.now(),
    /** EV-1-2: Canonical evidence schema version. Enables cross-product evidence graph federation. */
    val schemaVersion: String = SCHEMA_VERSION,
) {
    companion object {
        /** Current canonical evidence schema version. Increment when the response contract changes. */
        const val SCHEMA_VERSION = "evidence/v1"

        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(success = true, data = data)
        }
        
        fun <T> error(error: ApiError): ApiResponse<T> {
            return ApiResponse(success = false, error = error)
        }
        
        fun <T> error(code: String, message: String, details: Map<String, Any>? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = ApiError(code = code, message = message, details = details)
            )
        }
    }
}

data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null
)
