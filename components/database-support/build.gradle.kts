val exposedVersion: String by project
val postgresVersion: String by project
val hikariVersion: String by project

dependencies {
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")

    testImplementation("org.postgresql:postgresql:$postgresVersion")
}
