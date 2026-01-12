package com.evidentia.audit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AuditLogServiceApplication

fun main(args: Array<String>) {
    runApplication<AuditLogServiceApplication>(*args)
}
