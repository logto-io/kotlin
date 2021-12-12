plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    `java-library`
}

group = "io.logto"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("io.ktor:ktor-client-core:1.6.4")
    implementation("io.ktor:ktor-client-serialization:1.6.4")
    implementation("io.ktor:ktor-client-gson:1.6.4")
    implementation("io.ktor:ktor-client-logging:1.6.4")
    implementation("ch.qos.logback:logback-classic:1.2.6")

    implementation("org.bitbucket.b_c:jose4j:0.7.9")
    implementation("com.aventrix.jnanoid:jnanoid:2.0.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("io.mockk:mockk:1.12.1")
}

tasks {
    test {
        testLogging {
            events("failed", "skipped", "passed", "standardOut", "standardError")
            outputs.upToDateWhen { false }
        }
    }
}
