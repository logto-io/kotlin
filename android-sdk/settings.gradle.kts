pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
    includeBuild("../build-logic")
}

includeBuild("../kotlin-sdk")

rootProject.name = "android-sdk"
include(":android")
