plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.19.0")
}
