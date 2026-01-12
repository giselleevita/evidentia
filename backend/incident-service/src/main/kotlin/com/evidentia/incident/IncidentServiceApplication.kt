package com.evidentia.incident

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class IncidentServiceApplication

fun main(args: Array<String>) {
    runApplication<IncidentServiceApplication>(*args)
}
