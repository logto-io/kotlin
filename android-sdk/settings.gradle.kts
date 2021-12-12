pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "android-sdk"
includeBuild("../client")
include(":sdk")
