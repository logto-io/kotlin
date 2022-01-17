tasks.register("checkCodeStyle") {
    dependsOn(gradle.includedBuild("kotlin").task(":detekt"))
}
