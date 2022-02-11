plugins {
    id("com.android.library") version "7.0.0"
    id("org.jetbrains.kotlin.android") version "1.5.32"
    id("io.gitlab.arturbosch.detekt").version("1.19.0")
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.apply {
                    testLogging {
                        events("failed", "skipped", "passed", "standardOut", "standardError")
                    }
                    outputs.upToDateWhen { false }
                    reports.html.required.set(false)
                    reports.junitXml.required.set(false)
                }
            }
        }
    }

    lintOptions.apply {
        htmlReport = false
        xmlReport = false
    }
}

detekt {
    toolVersion = "1.19.0"
    config = files("../../config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}

dependencies {
    detekt("io.gitlab.arturbosch.detekt:detekt-formatting:1.19.0")
    detekt("io.gitlab.arturbosch.detekt:detekt-cli:1.19.0")

    api("io.logto.sdk:kotlin:1.0.0")

    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.browser:browser:1.3.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.5.32")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("io.mockk:mockk:1.12.2")
}
