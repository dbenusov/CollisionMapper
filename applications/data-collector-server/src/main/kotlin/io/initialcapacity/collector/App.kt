package io.initialcapacity.collector

import io.initialcapacity.workflow.WorkScheduler
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import java.util.*
import org.jetbrains.exposed.sql.Database

private val databaseName = "collisions"
private val database = Database.connect("jdbc:postgresql://localhost:5432/${databaseName}?user=postgres&password=password")
private val dbTemplate = DatabaseTemplate(database)
private val gateway = CollectorDataGateway(dbTemplate)

fun Application.module() {
    install(Routing) {
        get("/") {
            call.respondText("hi!", ContentType.Text.Html)
        }
    }
    val scheduler = WorkScheduler<CollectorTask>(CollectorWorkFinder(), mutableListOf(CollectorWorker(gateway)), 30)
    scheduler.start()
}

fun main() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    val port = System.getenv("PORT")?.toInt() ?: 8886
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = { module() }).start(wait = true)
}