pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}
includeBuild("../android-sdk")
rootProject.name = "android-sample"
include(":app")
