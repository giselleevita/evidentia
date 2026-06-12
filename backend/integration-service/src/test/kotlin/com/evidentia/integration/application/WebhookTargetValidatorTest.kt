package com.evidentia.integration.application

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class WebhookTargetValidatorTest {
    private val validator = WebhookTargetValidator()

    @Test
    fun `accepts public HTTPS targets`() {
        assertDoesNotThrow {
            validator.validate("https://93.184.216.34/webhooks/evidentia")
        }
    }

    @Test
    fun `rejects non HTTPS and private targets`() {
        assertThrows(IllegalArgumentException::class.java) {
            validator.validate("http://93.184.216.34/webhooks/evidentia")
        }
        assertThrows(IllegalArgumentException::class.java) {
            validator.validate("https://127.0.0.1/internal")
        }
        assertThrows(IllegalArgumentException::class.java) {
            validator.validate("https://169.254.169.254/latest/meta-data")
        }
    }
}
