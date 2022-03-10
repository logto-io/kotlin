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
    implementation(libs.logtoSdk.android)
    implementation(libs.androidx.core)
    implementation(libs.androidx.navigation.fragment)
}
