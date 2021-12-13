tasks.register("test") {
    dependsOn(gradle.includedBuild("client").task(":test"))
    dependsOn(gradle.includedBuild("android-sdk").task(":android-sdk:test"))
}
