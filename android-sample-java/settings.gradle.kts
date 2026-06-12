pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
    includeBuild("../build-logic")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
        create("logto") {
            from(files("../gradle/logto.versions.toml"))
        }
    }
}

includeBuild("../android-sdk")

rootProject.name = "android-sample-java"
include(":app")
