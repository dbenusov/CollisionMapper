package io.initialcapacity.collector

import io.initialcapacity.DatabaseTemplate
import org.jetbrains.exposed.sql.Database

fun testDatabaseTemplate(databaseName: String) = DatabaseTemplate(Database.connect(
    url = "jdbc:postgresql://localhost:5432/${databaseName}_test?user=postgres&password=password"
))
