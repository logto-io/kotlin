plugins {
    id("io.logto.android-sample")
    id("org.jetbrains.kotlin.android") version "1.5.32"
}

android {
    defaultConfig {
        applicationId = "io.logto.android.sample4k"
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation("io.logto.sdk:android:1.0.0")

    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
}
