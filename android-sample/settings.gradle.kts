pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

includeBuild("../android")

rootProject.name = "android-sample"
include(":app")
