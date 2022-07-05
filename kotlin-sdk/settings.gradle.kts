pluginManagement {
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

rootProject.name = "kotlin-sdk"
include(":kotlin")
