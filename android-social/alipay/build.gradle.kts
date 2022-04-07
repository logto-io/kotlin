group = "io.logto.sdk"
version = libs.versions.logtoSdk.get()

configurations.create("default")
artifacts.add("default", file("alipaySdk-15.7.9-20200727142846.aar"))
