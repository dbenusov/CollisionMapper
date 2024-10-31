package io.initialcapacity

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

private val database_host = System.getenv("DATABASE_HOST") ?: "localhost"
private val database_port = System.getenv("DATABASE_PORT") ?: "5432"
private val database_user = System.getenv("POSTGRES_USER") ?: "postgres"
private val database_password = System.getenv("POSTGRES_PASSWORD") ?: "password"

class DatabaseConfiguration() {
    private val url = "jdbc:postgresql://$database_host:$database_port/collisions?user=$database_user&password=$database_password"
    private val config = HikariConfig().apply { jdbcUrl = url }
    private val ds = HikariDataSource(config)

    val db by lazy {
        Database.Companion.connect(ds)
    }
}
