import org.gradle.accessors.dm.LibrariesForLibs
import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
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

    if (keystorePropertiesFile.exists()) {
        signingConfigs {
            create("default") {
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
            }
        }
        buildTypes {
            getByName("debug") {
                signingConfig = signingConfigs.getByName("default")
            }

            getByName("release"){
                signingConfig = signingConfigs.getByName("default")
            }
        }
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
