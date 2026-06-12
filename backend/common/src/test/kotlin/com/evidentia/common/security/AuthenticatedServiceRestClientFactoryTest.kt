package com.evidentia.common.security

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicReference

class AuthenticatedServiceRestClientFactoryTest {
    @Test
    fun `adds a service bearer token to outbound requests`() {
        val authorization = AtomicReference<String>()
        val server = HttpServer.create(InetSocketAddress(0), 0).apply {
            createContext("/audit") { exchange ->
                authorization.set(exchange.requestHeaders.getFirst("Authorization"))
                exchange.sendResponseHeaders(204, -1)
                exchange.close()
            }
            start()
        }

        try {
            val factory = AuthenticatedServiceRestClientFactory(
                ServiceAccessTokenProvider { registrationId ->
                    assertEquals("audit-log-service", registrationId)
                    "service-token"
                },
            )

            factory.create("http://localhost:${server.address.port}", "audit-log-service")
                .post()
                .uri("/audit")
                .retrieve()
                .toBodilessEntity()

            assertEquals("Bearer service-token", authorization.get())
        } finally {
            server.stop(0)
        }
    }
}
