group = "io.logto.sdk"
version = logto.versions.logtoSdk.get()

val dependencies = mapOf(
    "alipaySdk-15.7.9-20200727142846.aar" to "https://github.com/logto-io/social-sdks/raw/8aa90c6eaf68d1c4f2278bf7cd339b7a37c4c2c0/alipay/android/alipaySdk-15.7.9-20200727142846.aar"
)

dependencies.forEach { (name, url) ->
    val dependency = "$projectDir/$name"
    if (!file(dependency).exists()) {
        uri(url).toURL().openStream().use {
            it.copyTo(java.io.FileOutputStream(dependency))
        }
    }
    configurations.maybeCreate("default")
    artifacts.add("default", file(dependency))
}
