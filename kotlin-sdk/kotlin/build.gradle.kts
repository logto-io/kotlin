// https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
    id("io.logto.detekt")
    signing
    `maven-publish`
}

group = "io.logto.sdk"
version = libs.versions.logtoSdk.get()

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
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
    api(libs.jose4j)

    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.logbackClassic)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.mockwebserver)
}

// The content below follows the requirements before we publish this artifact to Maven Central
// Reference: https://central.sonatype.org/publish/requirements
publishing {
    publications {
        create<MavenPublication>("kotlin") {
            from(components["java"])

            pom {
                name.set("$groupId:$artifactId")
                description.set("the Logto kotlin sdk")
                url.set("https://github.com/logto-io/kotlin")

                licenses {
                    license {
                        name.set("Mozilla Public License 2.0")
                        url.set("https://opensource.org/licenses/MPL-2.0")
                    }
                }

                developers {
                    developer {
                        name.set("Xiao Yijun")
                        email.set("xiaoyijun@silverhand.io")
                        organization.set("Silverhand Inc.")
                        organizationUrl.set("https://github.com/silverhand-io")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/logto-io/kotlin.git")
                    developerConnection.set("scm:git:ssh://github.com:logto-io/kotlin.git")
                    url.set("https://github.com/logto-io/kotlin/tree/master")
                }
            }
        }
    }

    repositories {
        maven(url = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
            val ossrhUsername: String by project
            val ossrhPassword: String by project

            name = "sonatype"
            credentials {
                username = ossrhUsername
                password = ossrhPassword
            }
        }
    }
}

signing {
    sign(publishing.publications["kotlin"])
}
