package io.initialcapacity.collector

import io.initialcapacity.workflow.WorkScheduler
import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.Netty
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.util.pipeline.*
import java.util.*
import org.jetbrains.exposed.sql.Database

fun Application.module(gateway: CollectorDataGateway) {
    install(Routing) {
        get("/") {
            call.respondText("hi!", ContentType.Text.Html)
        }

        get("/view-data") {
            val map = mapOf(
                "data" to data(gateway),
            )
            call.respond(FreeMarkerContent("index.ftl", map))
        }
        staticResources("/static/styles", "static/styles")
        staticResources("/static/images", "static/images")
    }
    val scheduler = WorkScheduler<CollectorTask>(CollectorWorkFinder(), mutableListOf(CollectorWorker(gateway)), 30)
    scheduler.start()
}

private fun PipelineContext<Unit, ApplicationCall>.data(gateway: CollectorDataGateway): MutableList<String> {
    val list = mutableListOf<String>()
    val dbData = gateway.getAll()
    for (data in dbData) {
        list.add(data.toString())
    }
    return list
}

fun main() {
    val databaseName = "collisions"
    val database by lazy {
        Database.connect("jdbc:postgresql://localhost:5432/${databaseName}?user=postgres&password=password")
    }
    val dbTemplate = DatabaseTemplate(database)
    val gateway = CollectorDataGateway(dbTemplate)
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    val port = System.getenv("PORT")?.toInt() ?: 8886
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = { module(gateway) }).start(wait = true)
}