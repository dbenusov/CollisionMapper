package io.initialcapacity.collector

import io.initialcapacity.DatabaseTemplate
import org.jetbrains.exposed.sql.Database

private val database_host = System.getenv("DATABASE_HOST") ?: "localhost:5432"
private val database_user = System.getenv("POSTGRES_USER") ?: "postgres"
private val database_password = System.getenv("POSTGRES_PASSWORD") ?: "password"

fun testDatabaseTemplate(databaseName: String) = DatabaseTemplate(Database.connect(
    url = "jdbc:postgresql://$database_host/${databaseName}_test?user=$database_user&password=$database_password"
))
