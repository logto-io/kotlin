pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

includeBuild("../client")

rootProject.name = "android-sdk"
include(":android-sdk")
