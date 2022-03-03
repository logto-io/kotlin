plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    `java-library`
    id("io.gitlab.arturbosch.detekt").version("1.19.0")
    id("org.jetbrains.kotlinx.kover").version("0.5.0")
}

group = "io.logto.sdk"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

detekt {
    toolVersion = "1.19.0"
    config = files("../../config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}

tasks {
    test {
        testLogging {
            events("failed", "skipped", "passed", "standardOut", "standardError")
            outputs.upToDateWhen { false }
        }
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        xml.required.set(false)
        html.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
    }
}

dependencies {
    detekt("io.gitlab.arturbosch.detekt:detekt-cli:1.19.0")
    detekt("io.gitlab.arturbosch.detekt:detekt-formatting:1.19.0")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.5.31"))
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.9.3"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("ch.qos.logback:logback-classic:1.2.10")

    api("org.bitbucket.b_c:jose4j:0.7.9")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("io.mockk:mockk:1.12.2")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("com.squareup.okhttp3:mockwebserver")
}
