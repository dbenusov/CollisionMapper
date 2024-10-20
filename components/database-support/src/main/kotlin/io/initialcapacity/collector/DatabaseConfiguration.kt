package io.initialcapacity.collector

import org.jetbrains.exposed.sql.Database

class DatabaseConfiguration(private val name: String) {
    val db by lazy {
        Database.connect("jdbc:postgresql://localhost:5432/${name}?user=postgres&password=password")
    }
}
