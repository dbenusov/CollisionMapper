package io.initialcapacity

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

class DatabaseConfiguration(private val dbUrl: String) {
    private val config = com.zaxxer.hikari.HikariConfig().apply { jdbcUrl = dbUrl }
    private val ds = com.zaxxer.hikari.HikariDataSource(config)

    val db by lazy {
        Database.Companion.connect(ds)
    }
}
