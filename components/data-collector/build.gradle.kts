dependencies {
    implementation(project(":support:workflow-support"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    implementation(project(":components:database-support"))

    testImplementation(project(":components:test-database-support"))
}