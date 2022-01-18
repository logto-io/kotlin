plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    `java-library`
    id("io.gitlab.arturbosch.detekt").version("1.19.0")
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
    config = files("../config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}

tasks {
    test {
        testLogging {
            events("failed", "skipped", "passed", "standardOut", "standardError")
            outputs.upToDateWhen { false }
        }
        reports.html.required.set(false)
        reports.junitXml.required.set(false)
    }
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.5.31"))
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.9.3"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.bitbucket.b_c:jose4j:0.7.9")
    implementation("com.aventrix.jnanoid:jnanoid:2.0.0")
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.google.code.gson:gson:2.8.9")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("io.mockk:mockk:1.12.2")
    testImplementation("com.google.truth:truth:1.1.3")
}
