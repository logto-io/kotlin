tasks.register("clean") {
    dependsOn(gradle.includedBuild("kotlin").task(":clean"))
    dependsOn(gradle.includedBuild("android").task(":logto-client:clean"))
}

tasks.register("checkCodeStyle") {
    dependsOn(gradle.includedBuild("kotlin").task(":detekt"))
    dependsOn(gradle.includedBuild("android").task(":logto-client:detekt"))
}

tasks.register("test") {
    dependsOn(gradle.includedBuild("kotlin").task(":test"))
    dependsOn(gradle.includedBuild("android").task(":logto-client:testDebugUnitTest"))
}
