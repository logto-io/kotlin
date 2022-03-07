import org.gradle.kotlin.dsl.dependencies
import utils.DetektUtil

plugins {
    id("io.gitlab.arturbosch.detekt")
}

dependencies {
    detekt("io.gitlab.arturbosch.detekt:detekt-cli:1.19.0")
    detekt("io.gitlab.arturbosch.detekt:detekt-formatting:1.19.0")
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
