import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("com.android.application")
}

android {
    compileSdk = 30
    defaultConfig {
        minSdk = 24
        targetSdk = 30
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    lint {
        htmlReport = false
        xmlReport = false
        textReport = false
    }
}

val libs = the<LibrariesForLibs>()

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.material)
    implementation(libs.gson)
}
