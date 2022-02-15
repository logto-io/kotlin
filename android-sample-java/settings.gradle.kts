pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

includeBuild("../android-sdk")

rootProject.name = "android-sample-java"
include(":app")
