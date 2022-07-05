// https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("io.logto.android-sample")
    alias(libs.plugins.kotlin.android)
}

android {
    defaultConfig {
        applicationId = "io.logto.android.sample4k"
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    // TODO - Use Custom Plugins
    implementation(logto.logtoSdk.alipay)
    api("com.tencent.mm.opensdk:wechat-sdk-android:6.8.0")

    implementation(logto.logtoSdk.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
}
