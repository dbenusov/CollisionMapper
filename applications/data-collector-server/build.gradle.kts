plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}

group = "io.initialcapacity.collector"

val ktorVersion: String by project
val exposedVersion: String by project
val postgresVersion: String by project
val hikariVersion: String by project
val prometheusVersion: String by project

dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.7")

    implementation(project(":components:data-collector"))
    implementation(project(":components:database-support"))
    implementation(project(":support:workflow-support"))

    implementation("com.zaxxer:HikariCP:$hikariVersion")

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-freemarker-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.prometheus:simpleclient:$prometheusVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_common:$prometheusVersion")

    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation(project(":components:test-database-support"))
}

task<JavaExec>("run") {
    classpath = files(tasks.jar)
}

tasks {
    jar {
        manifest { attributes("Main-Class" to "io.initialcapacity.collector.AppKt") }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from({
            configurations.runtimeClasspath.get()
                .filter { it.name.endsWith("jar") }
                .map(::zipTree)
        })
    }
}