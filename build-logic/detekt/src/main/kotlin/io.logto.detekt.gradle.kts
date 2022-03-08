import org.gradle.accessors.dm.LibrariesForLibs
import utils.DetektUtil

plugins {
    id("io.gitlab.arturbosch.detekt")
}

val libs = the<LibrariesForLibs>()

dependencies {
    detekt(libs.detekt.cli)
    detekt(libs.detekt.formatting)
}

detekt {
    toolVersion = "1.19.0"
    config = files(resources.text.fromString(DetektUtil.getDetektConfig("/detekt.yml")))
    buildUponDefaultConfig = true
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        xml.required.set(false)
        html.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
    }
}
