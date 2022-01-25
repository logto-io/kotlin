plugins {
    id("com.android.library") version "7.0.0"
    id("org.jetbrains.kotlin.android") version "1.5.32"
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
}
