plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.android.gradle.tool)

    // https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
