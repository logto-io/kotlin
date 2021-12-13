tasks.register("test") {
    dependsOn(gradle.includedBuild("client").task(":test"))
    dependsOn(gradle.includedBuild("android-sdk").task(":sdk:test"))
}
