import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version "_"
    idea
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "java-test-fixtures")

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    dependencies {
        testImplementation(platform(Testing.junit.bom))
        testImplementation(Testing.junit.jupiter)

        implementation(platform("dev.forkhandles:forkhandles-bom:_"))
        implementation("dev.forkhandles:result4k")
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
}
