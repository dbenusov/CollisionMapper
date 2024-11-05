package io.initialcapacity

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

private val database_host = System.getenv("DATABASE_HOST") ?: "localhost:5432"
private val database_user = System.getenv("POSTGRES_USER") ?: "postgres"
private val database_password = System.getenv("POSTGRES_PASSWORD") ?: "password"
private val is_prod = System.getenv("IS_PROD")?.toBoolean() ?: false

class DatabaseConfiguration() {
    private val config = HikariConfig().apply {
        if (is_prod) {
            jdbcUrl = "jdbc:postgresql:///collisions"
            addDataSourceProperty("cloudSqlInstance", "collision-mappter:us-central1:collision-mapper-db")
            addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory")
            addDataSourceProperty("user", database_user)
            addDataSourceProperty("password", database_password)
        } else {
            jdbcUrl = "jdbc:postgresql://$database_host/collisions"
            username = database_user
            password = database_password
        }
    }
    private val ds = HikariDataSource(config)

    val db by lazy {
        Database.Companion.connect(ds)
    }
}
