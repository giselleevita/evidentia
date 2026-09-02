package com.evidentia.integration.application

import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.URI

@Component
class WebhookTargetValidator {
    fun validate(targetUrl: String) {
        val uri = runCatching { URI(targetUrl) }
            .getOrElse { throw IllegalArgumentException("Webhook target URL is invalid") }

        require(uri.scheme.equals("https", ignoreCase = true)) {
            "Webhook target URL must use HTTPS"
        }
        require(uri.host != null && uri.userInfo == null && uri.fragment == null) {
            "Webhook target URL must have a valid public host"
        }

        val addresses = runCatching { InetAddress.getAllByName(uri.host).toList() }
            .getOrElse { throw IllegalArgumentException("Webhook target host cannot be resolved") }
        require(addresses.isNotEmpty() && addresses.none(::isPrivateAddress)) {
            "Webhook target URL must resolve only to public addresses"
        }
    }

    private fun isPrivateAddress(address: InetAddress): Boolean {
        if (
            address.isAnyLocalAddress ||
            address.isLoopbackAddress ||
            address.isLinkLocalAddress ||
            address.isSiteLocalAddress ||
            address.isMulticastAddress
        ) {
            return true
        }

        val bytes = address.address
        return bytes.size == 16 && (bytes[0].toInt() and 0xfe) == 0xfc
    }
}
