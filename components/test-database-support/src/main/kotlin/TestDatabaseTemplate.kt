package io.initialcapacity.collector

import io.initialcapacity.DatabaseTemplate
import org.jetbrains.exposed.sql.Database

private val database_host = System.getenv("DATABASE_HOST") ?: "localhost"
private val database_port = System.getenv("DATABASE_PORT") ?: "5432"

fun testDatabaseTemplate(databaseName: String) = DatabaseTemplate(Database.connect(
    url = "jdbc:postgresql://$database_host:$database_port/${databaseName}_test?user=postgres&password=password"
))
