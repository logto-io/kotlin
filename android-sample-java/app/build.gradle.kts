plugins {
    id("io.logto.android-sample")
}

android {
    defaultConfig {
        applicationId = "io.logto.android.sample4j"
        versionCode = 1
        versionName = "1.0.0"
        // Must equal the scheme of the redirect URI passed to `signIn` / `signOut`.
        manifestPlaceholders["logtoRedirectScheme"] = "io.logto.android"
    }
}

dependencies {
    implementation(logto.logtoSdk.android)
    implementation(libs.androidx.core)
    implementation(libs.androidx.navigation.fragment)
}
