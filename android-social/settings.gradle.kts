dependencyResolutionManagement {
    versionCatalogs {
        create("logto") {
            from(files("../gradle/logto.versions.toml"))
        }
    }
}

rootProject.name = "android-social"
include(":alipay")
