tasks.register("clean") {
    dependsOn(gradle.includedBuild("kotlin-sdk").task(":kotlin:clean"))
    dependsOn(gradle.includedBuild("android-sdk").task(":android:clean"))
}

tasks.register("checkCodeStyle") {
    dependsOn(gradle.includedBuild("kotlin-sdk").task(":kotlin:detekt"))
    dependsOn(gradle.includedBuild("android-sdk").task(":android:detekt"))
}

tasks.register("test") {
    dependsOn(gradle.includedBuild("kotlin-sdk").task(":kotlin:test"))
    dependsOn(gradle.includedBuild("android-sdk").task(":android:testDebugUnitTest"))
}
