package com.evidentia.incident

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.evidentia"])
class IncidentServiceApplication

fun main(args: Array<String>) {
    runApplication<IncidentServiceApplication>(*args)
}
