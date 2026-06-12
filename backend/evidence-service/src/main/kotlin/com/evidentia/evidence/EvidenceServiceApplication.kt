package com.evidentia.evidence

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.evidentia"])
class EvidenceServiceApplication

fun main(args: Array<String>) {
    runApplication<EvidenceServiceApplication>(*args)
}
