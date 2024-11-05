package io.initialcapacity

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

private val database_host = System.getenv("DATABASE_HOST") ?: "localhost:5432"
private val database_user = System.getenv("POSTGRES_USER") ?: "postgres"
private val database_password = System.getenv("POSTGRES_PASSWORD") ?: "password"
private val use_ssl = System.getenv("USE_SSL")?.toBoolean() ?: false

class DatabaseConfiguration() {
    private val url = "jdbc:postgresql://$database_host/collisions"
    private val config = HikariConfig().apply {
        jdbcUrl = url
        username = database_user
        password = database_password
        addDataSourceProperty("ssl", use_ssl.toString())
    }
    private val ds = HikariDataSource(config)

    val db by lazy {
        Database.Companion.connect(ds)
    }
}
