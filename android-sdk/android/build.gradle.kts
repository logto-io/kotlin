plugins {
    id("com.android.library") version "7.0.0"
    id("org.jetbrains.kotlin.android") version "1.5.32"
    id("io.logto.detekt")
    id("org.jetbrains.kotlinx.kover").version("0.5.0")
}

group = "io.logto.sdk"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
}

android {
    compileSdk = 30
    defaultConfig {
        minSdk = 24
        targetSdk = 30
    }

    sourceSets {
        getByName("main") {
            java.srcDir("src/main/kotlin")
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.apply {
                    testLogging {
                        events("failed", "skipped", "passed", "standardOut", "standardError")
                    }
                    outputs.upToDateWhen { false }
                    reports.html.required.set(true)
                    reports.junitXml.required.set(true)
                }
            }
        }
    }

    lintOptions.apply {
        htmlReport = false
        xmlReport = false
    }
}

dependencies {
    api("io.logto.sdk:kotlin:1.0.0")

    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.browser:browser:1.3.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.5.32")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("io.mockk:mockk:1.12.2")
}
