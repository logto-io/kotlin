pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

includeBuild("../kotlin")

rootProject.name = "android"
include(":logto-client")
