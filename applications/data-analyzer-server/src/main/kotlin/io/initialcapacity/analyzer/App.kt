package io.initialcapacity.analyzer

import freemarker.cache.ClassTemplateLoader
import io.initialcapacity.DatabaseConfiguration
import io.initialcapacity.DatabaseTemplate
import io.initialcapacity.workflow.WorkScheduler
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.util.pipeline.*
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import java.util.*

private val collector_url = System.getenv("COLLECTOR_URL") ?: "localhost:8886"

fun Application.module(gateway: AnalyzerDataGateway) {
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }
    install(Routing) {
        get("/") {
            call.respondText("hi!", ContentType.Text.Html)
        }

        get("/ping") {
            call.respondText("Data Analyzer Responding!", ContentType.Text.Plain)
        }

        get("/view-data") {
            val map = mapOf(
                "headers" to headers(),
                "data" to data(gateway),
            )
            call.respond(FreeMarkerContent("index.ftl", map))
        }

        get("/json-data") {
            call.respond(jsondata(gateway).toString())
        }
        staticResources("/static/styles", "static/styles")
        staticResources("/static/images", "static/images")
    }
    val scheduler = WorkScheduler<AnalyzerTask>(AnalyzerWorkFinder(collector_url), mutableListOf(AnalyzerWorker(gateway)), 30)
    scheduler.start()
}

private fun PipelineContext<Unit, ApplicationCall>.headers(): MutableMap<String, String> {
    val headers = mutableMapOf<String, String>()
    call.request.headers.entries().forEach { entry ->
        headers[entry.key] = entry.value.joinToString()
    }
    return headers
}

private fun PipelineContext<Unit, ApplicationCall>.data(gateway: AnalyzerDataGateway): MutableList<String> {
    val list = mutableListOf<String>()
    val dbData = gateway.getAll()
    for (data in dbData) {
        list.add(data.toString())
    }
    return list
}

private fun PipelineContext<Unit, ApplicationCall>.jsondata(gateway: AnalyzerDataGateway): JsonObject {
    val list = mutableListOf<String>()
    val dbData = gateway.getAll()
    val jsonArray = buildJsonArray {
        for (data in dbData) {
            add(buildJsonObject {
                put("id", data.core.id)
                put("latitude", data.latitude)
                put("longitude", data.longitude)
                val points = buildJsonArray {
                    for (point in data.core.data_points) {
                        add(buildJsonObject {
                            put("id", point)
                        })
                    }
                }
                put("points", points)
            })
            list.add(data.toString())
        }
    }

    return buildJsonObject {
        put("clusters", jsonArray)
    }
}

private val logger = LoggerFactory.getLogger("main")

fun main() {
    val database = DatabaseConfiguration()
    val dbTemplate = DatabaseTemplate(database.db)
    val gateway = AnalyzerDataGateway(dbTemplate)
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    val port = System.getenv("PORT")?.toInt() ?: 8887
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = { module(gateway) }).start(wait = true)
}