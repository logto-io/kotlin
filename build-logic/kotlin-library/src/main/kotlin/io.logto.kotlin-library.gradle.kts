import org.gradle.api.JavaVersion

plugins {
    id("org.jetbrains.kotlin.jvm")
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
