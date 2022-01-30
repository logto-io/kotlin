pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

includeBuild("../kotlin-sdk")

rootProject.name = "android-sdk"
include(":android")
