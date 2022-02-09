plugins {
    id("com.android.application") version "7.0.0"
    id("org.jetbrains.kotlin.android") version "1.5.32"
}

repositories {
    google()
    mavenCentral()
}

android {
    compileSdk = 30
    defaultConfig {
        applicationId = "io.logto.android.sample"
        minSdk = 24
        targetSdk = 30
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation("io.logto.sdk:android:1.0.0")

    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.code.gson:gson:2.8.9")

    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.2")
}