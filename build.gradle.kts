tasks.register("checkCodeStyle") {
    dependsOn(gradle.includedBuild("kotlin").task(":detekt"))
}

tasks.register("lintAndroidSdk") {
    dependsOn(gradle.includedBuild("android-sdk").task(":android-sdk:lint"))
}

tasks.register("test") {
    dependsOn(gradle.includedBuild("client").task(":test"))
    dependsOn(gradle.includedBuild("android-sdk").task(":android-sdk:test"))
}
