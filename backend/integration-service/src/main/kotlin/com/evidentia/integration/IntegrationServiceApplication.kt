package com.evidentia.integration

import com.evidentia.common.security.SecurityConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(SecurityConfig::class)
class IntegrationServiceApplication

fun main(args: Array<String>) {
    runApplication<IntegrationServiceApplication>(*args)
}
