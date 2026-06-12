package com.evidentia.common.web

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

/**
 * Global exception handler for all services.
 * Provides consistent error responses across the platform.
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        val errors = ex.bindingResult.allErrors.associate { error ->
            val fieldName = (error as? FieldError)?.field ?: error.objectName
            fieldName to (error.defaultMessage ?: "Invalid value")
        }
        
        logger.warn("Validation error: {}", errors)
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiResponse.error(
                code = "VALIDATION_ERROR",
                message = "Request validation failed",
                details = errors
            )
        )
    }
    
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Illegal argument: {}", ex.message)
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiResponse.error(
                code = "INVALID_ARGUMENT",
                message = ex.message ?: "Invalid argument provided"
            )
        )
    }
    
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(
        ex: IllegalStateException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Illegal state: {}", ex.message)
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ApiResponse.error(
                code = "ILLEGAL_STATE",
                message = ex.message ?: "Operation not allowed in current state"
            )
        )
    }
    
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(
        ex: AccessDeniedException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Access denied: {}", ex.message)

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ApiResponse.error(
                code = "FORBIDDEN",
                message = "Access denied"
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Unexpected error", ex)
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiResponse.error(
                code = "INTERNAL_ERROR",
                message = "An unexpected error occurred"
            )
        )
    }
}
