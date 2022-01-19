tasks.register("clean") {
    dependsOn(gradle.includedBuild("kotlin").task(":clean"))
}

tasks.register("checkCodeStyle") {
    dependsOn(gradle.includedBuild("kotlin").task(":detekt"))
}

tasks.register("test") {
    dependsOn(gradle.includedBuild("kotlin").task(":test"))
}
