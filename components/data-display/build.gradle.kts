val ktorVersion: String by project

dependencies {
    implementation(project(":support:workflow-support"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")

    implementation(project(":components:database-support"))

    testImplementation(project(":components:test-database-support"))

}