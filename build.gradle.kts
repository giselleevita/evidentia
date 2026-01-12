plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.20" apply false
    id("org.jetbrains.kotlin.plugin.spring") version "1.9.20" apply false
    id("org.jetbrains.kotlin.plugin.jpa") version "1.9.20" apply false
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = "com.evidentia"
    version = "0.0.1-SNAPSHOT"
}
