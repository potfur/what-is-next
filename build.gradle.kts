import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm")
    idea
    `java-test-fixtures`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(platform("dev.forkhandles:forkhandles-bom:_"))
    api("dev.forkhandles:result4k")
    api("dev.forkhandles:values4k")

    api(platform("org.http4k:http4k-bom:_"))
    api("org.http4k:http4k-core")
    api("org.http4k:http4k-contract")
    api("org.http4k:http4k-format-jackson")

    testApi(platform("org.junit:junit-bom:_"))
    testApi("org.junit.jupiter:junit-jupiter")
    testApi("org.junit.jupiter:junit-jupiter")
    testApi("org.http4k:http4k-testing-tracerbullet")
}

tasks {
    test {
        useJUnitPlatform()
        outputs.upToDateWhen { false }
        testLogging {
            events = TestLogEvent.values().toSet()
            exceptionFormat = FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
    }
}
