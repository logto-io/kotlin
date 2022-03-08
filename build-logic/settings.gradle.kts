dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "build-logic"
include("detekt")
include("android-sample")
