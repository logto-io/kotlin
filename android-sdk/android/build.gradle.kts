// https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kover)
    id("io.logto.detekt")
}

group = "io.logto.sdk"
version = libs.versions.logtoSdk.get()

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

    lint {
        htmlReport = false
        xmlReport = false
    }
}

dependencies {
    api(libs.logtoSdk.kotlin)

    compileOnly(libs.logtoSdk.alipay)
    compileOnly(libs.wechatSdkAndroid)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.logtoSdk.alipay)
    testImplementation(libs.wechatSdkAndroid)
}
