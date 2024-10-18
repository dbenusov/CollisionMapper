val exposedVersion: String by project
val postgresVersion: String by project

dependencies {
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")

    testImplementation("org.postgresql:postgresql:$postgresVersion")
}
