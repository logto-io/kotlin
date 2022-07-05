pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
    includeBuild("../build-logic")
}


dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
        create("logto") {
            from(files("../gradle/logto.versions.toml"))
        }
    }
}

includeBuild("../kotlin-sdk")

rootProject.name = "android-sdk"
include(":android")
