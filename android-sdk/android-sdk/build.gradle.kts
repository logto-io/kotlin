plugins {
    id("com.android.library") version "7.0.0"
    id("org.jetbrains.kotlin.android") version "1.5.32"
    id("io.gitlab.arturbosch.detekt") version "1.18.1"
}

group = "io.logto"
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

        manifestPlaceholders["logtoRedirectScheme"] = "io.logto.android"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    detekt("io.gitlab.arturbosch.detekt:detekt-formatting:1.18.1")
    detekt("io.gitlab.arturbosch.detekt:detekt-cli:1.18.1")

    implementation("io.logto:client:1.0.0")

    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.browser:browser:1.3.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.32")


    implementation("io.ktor:ktor-client-core:1.6.4")
    implementation("io.ktor:ktor-client-android:1.6.4")
    implementation("io.ktor:ktor-client-serialization:1.6.4")
    implementation("io.ktor:ktor-client-gson:1.6.4")
    implementation("io.ktor:ktor-client-logging:1.6.4")
    implementation("ch.qos.logback:logback-classic:1.2.6")

    implementation("org.bitbucket.b_c:jose4j:0.7.9")
    implementation("com.aventrix.jnanoid:jnanoid:2.0.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.4.0")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.robolectric:robolectric:4.6.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2")
    testImplementation("io.mockk:mockk:1.12.1")
    testImplementation("io.mockk:mockk-agent-jvm:1.12.1")
}
