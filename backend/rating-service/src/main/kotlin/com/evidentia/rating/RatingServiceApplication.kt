package com.evidentia.rating

import com.evidentia.common.config.CommonConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(CommonConfiguration::class)
class RatingServiceApplication

fun main(args: Array<String>) {
    runApplication<RatingServiceApplication>(*args)
}
