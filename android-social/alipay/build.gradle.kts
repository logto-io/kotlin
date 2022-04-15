group = "io.logto.sdk"
version = libs.versions.logtoSdk.get()

val alipaySdkName = "alipaySdk-15.7.9-20200727142846.aar"
val sdk = file(alipaySdkName)
if (!sdk.exists()) {
    val sdkUri = "https://github.com/logto-io/social-sdks/raw/8aa90c6eaf68d1c4f2278bf7cd339b7a37c4c2c0/alipay/android/alipaySdk-15.7.9-20200727142846.aar"
    uri(sdkUri).toURL().openStream().use {
        it.copyTo(java.io.FileOutputStream(sdk))
    }
}

configurations.create("default")
artifacts.add("default", file(alipaySdkName))
