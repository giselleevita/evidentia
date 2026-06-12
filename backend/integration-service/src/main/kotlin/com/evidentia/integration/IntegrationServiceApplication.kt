package com.evidentia.integration

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.evidentia"])
class IntegrationServiceApplication

fun main(args: Array<String>) {
    runApplication<IntegrationServiceApplication>(*args)
}
