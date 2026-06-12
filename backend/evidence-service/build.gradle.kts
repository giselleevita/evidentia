import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.flywaydb.flyway") version "10.0.0"
}

dependencies {
    implementation(project(":backend:common"))
    
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // Database
    implementation("org.postgresql:postgresql:42.7.0")
    implementation("org.flywaydb:flyway-core:9.22.1")
    
    // OpenAPI/Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
    
    // Object Storage
    implementation("com.azure:azure-storage-blob:12.23.0")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:postgresql:1.21.4")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

springBoot {
    mainClass.set("com.evidentia.evidence.EvidenceServiceApplicationKt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
