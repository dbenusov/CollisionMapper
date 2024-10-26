package io.initialcapacity.collector

import freemarker.cache.ClassTemplateLoader
import io.initialcapacity.DatabaseTemplate
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
import org.slf4j.LoggerFactory

fun Application.module(gateway: CollectorDataGateway) {
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
    install(Routing) {
        get("/") {
            call.respondText("hi collector!", ContentType.Text.Html)
        }

        get("/view-data") {
            val map = mapOf(
                "headers" to headers(),
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

private fun PipelineContext<Unit, ApplicationCall>.headers(): MutableMap<String, String> {
    val headers = mutableMapOf<String, String>()
    call.request.headers.entries().forEach { entry ->
        headers[entry.key] = entry.value.joinToString()
    }
    return headers
}

private fun PipelineContext<Unit, ApplicationCall>.data(gateway: CollectorDataGateway): MutableList<String> {
    val list = mutableListOf<String>()
    val dbData = gateway.getAll()
    for (data in dbData) {
        list.add(data.toString())
    }
    return list
}

private val database_host = System.getenv("DATABASE_HOST") ?: "localhost:5432"
private val logger = LoggerFactory.getLogger("main")

fun main() {
    val databaseName = "collisions"
    logger.info(System.getenv("DATABASE_HOST"))
    System.getenv("APP")
    val database = DatabaseConfiguration("jdbc:postgresql://${database_host}/${databaseName}?user=postgres&password=password")
    val dbTemplate = DatabaseTemplate(database.db)
    val gateway = CollectorDataGateway(dbTemplate)
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    val port = System.getenv("PORT")?.toInt() ?: 8886
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = { module(gateway) }).start(wait = true)
}