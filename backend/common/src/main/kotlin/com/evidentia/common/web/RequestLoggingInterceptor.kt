package com.evidentia.common.web

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.UUID

/**
 * Interceptor to log requests and set correlation ID in MDC for distributed tracing.
 */
@Component
class RequestLoggingInterceptor : HandlerInterceptor {
    private val logger = LoggerFactory.getLogger(RequestLoggingInterceptor::class.java)
    
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val correlationId = request.getHeader("X-Correlation-Id") 
            ?: UUID.randomUUID().toString()
        
        MDC.put("correlationId", correlationId)
        response.setHeader("X-Correlation-Id", correlationId)
        
        logger.debug(
            "Request: {} {} from {}",
            request.method,
            request.requestURI,
            request.remoteAddr
        )
        
        return true
    }
    
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        MDC.remove("correlationId")
    }
}
