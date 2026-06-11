package com.evidentia.common.web

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * Lightweight in-memory rate limiting for API endpoints.
 */
@Component
class RateLimitFilter : OncePerRequestFilter() {
    private val maxRequests = 120
    private val windowSeconds = 60L
    private val hits = ConcurrentHashMap<String, ConcurrentLinkedDeque<Long>>()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI ?: ""
        return path.startsWith("/actuator/health") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val client = request.getHeader("X-Forwarded-For")
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: request.remoteAddr

        val path = request.requestURI ?: "unknown"
        val key = "$client:$path"
        val now = Instant.now().epochSecond
        val floor = now - windowSeconds

        val q = hits.computeIfAbsent(key) { ConcurrentLinkedDeque() }
        while (true) {
            val head = q.peekFirst() ?: break
            if (head <= floor) q.pollFirst() else break
        }

        if (q.size >= maxRequests) {
            response.status = 429
            response.contentType = "application/json"
            response.setHeader("Retry-After", windowSeconds.toString())
            response.setHeader("X-RateLimit-Limit", maxRequests.toString())
            response.setHeader("X-RateLimit-Remaining", "0")
            response.writer.write("{\"error\":\"rate_limit_exceeded\"}")
            return
        }

        q.addLast(now)
        response.setHeader("X-RateLimit-Limit", maxRequests.toString())
        response.setHeader("X-RateLimit-Remaining", (maxRequests - q.size).coerceAtLeast(0).toString())
        filterChain.doFilter(request, response)
    }
}
