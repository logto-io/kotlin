plugins {
    id("io.logto.kotlin-library")
    id("io.logto.detekt")
    id("org.jetbrains.kotlinx.kover").version("0.5.0")
}

group = "io.logto.sdk"
version = "1.0.0"

repositories {
    mavenCentral()
}

tasks {
    test {
        testLogging {
            events("failed", "skipped", "passed", "standardOut", "standardError")
            outputs.upToDateWhen { false }
        }
    }
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.5.31"))
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.9.3"))

    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("ch.qos.logback:logback-classic:1.2.10")

    api("org.bitbucket.b_c:jose4j:0.7.9")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("io.mockk:mockk:1.12.2")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("com.squareup.okhttp3:mockwebserver")
}
