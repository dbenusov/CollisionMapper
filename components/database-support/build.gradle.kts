val exposedVersion: String by project
val postgresVersion: String by project
val hikariVersion: String by project

dependencies {
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    // Used for Google SQL connections. Look in the Database configuration prudction
    // setup.
    implementation("com.google.cloud.sql:postgres-socket-factory:1.21.0")

    testImplementation("org.postgresql:postgresql:$postgresVersion")
}
