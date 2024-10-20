plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}

group = "io.initialcapacity.collector"

val ktorVersion: String by project
val exposedVersion: String by project
val postgresVersion: String by project

dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.7")

    implementation(project(":components:data-collector"))
    implementation(project(":components:database-support"))
    implementation(project(":support:workflow-support"))

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-freemarker-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")

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