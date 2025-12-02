// https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kover)
    id("io.logto.detekt")
    signing
    `maven-publish`
}

group = "io.logto.sdk"
version = logto.versions.logtoSdk.get()

repositories {
    google()
    mavenCentral()
}

android {
    compileSdk = 30
    defaultConfig {
        minSdk = 24
        targetSdk = 30

        consumerProguardFile("./proguard-rules.pro")
    }

    sourceSets {
        getByName("main") {
            java.srcDir("src/main/kotlin")
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.apply {
                    testLogging {
                        events("failed", "skipped", "passed", "standardOut", "standardError")
                    }
                    outputs.upToDateWhen { false }
                    reports.html.required.set(true)
                    reports.junitXml.required.set(true)
                }
            }
        }
    }

    lint {
        htmlReport = false
        xmlReport = false
    }

    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }
}

dependencies {
    api(logto.logtoSdk.kotlin)

    compileOnly(logto.logtoSdk.alipay)
    compileOnly(libs.wechatSdkAndroid)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.browser)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(logto.logtoSdk.alipay)
    testImplementation(libs.wechatSdkAndroid)
}

// The content below follows the requirements before we publish this artifact to Maven Central
// Reference: https://central.sonatype.org/publish/requirements
publishing {
    publications {
        create<MavenPublication>("android") {
            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("$groupId:$artifactId")

                description.set("the Logto Android SDK")
                url.set("https://github.com/logto-io/kotlin")

                licenses {
                    license {
                        name.set("Mozilla Public License 2.0")
                        url.set("https://opensource.org/licenses/MPL-2.0")
                    }
                }

                developers {
                    developer {
                        name.set("Silverhand Inc.")
                        email.set("contact@silverhand.io")
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
        maven(url = "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/") {
            name = "sonatype"
            credentials {
                username = (project.properties["ossrhUsername"] as String?)?: ""
                password = (project.properties["ossrhPassword"] as String?)?: ""
            }
        }
    }
}

signing {
    sign(publishing.publications["android"])
}
