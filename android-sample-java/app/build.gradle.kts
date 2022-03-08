plugins {
    id("io.logto.android-sample")
}

android {
    defaultConfig {
        applicationId = "io.logto.android.sample4j"
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation("io.logto.sdk:android:1.0.0")

    implementation("androidx.core:core:1.6.0")
    implementation("androidx.navigation:navigation-fragment:2.3.5")
}
