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
import kotlin.time.DurationUnit

private val collector_url = System.getenv("COLLECTOR_URL") ?: "localhost:8886"
private val work_finder = AnalyzerWorkFinder(collector_url)

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

        get("/health-check") {
        if (work_finder.checkStatus())
            call.respondText("Ready", ContentType.Text.Html)
        else
            call.respondText("Processing", ContentType.Text.Html)
        }

        get("metrics") {
            val metrics = work_finder.getMetrics()
            val jsonArray = buildJsonArray {
                for (metric in metrics) {
                    add(buildJsonObject {
                        put("available_clusters", metric.available_clusters)
                        put("processed_clusters", metric.processed_clusters)
                        put("duration_ms", metric.time.toString(DurationUnit.MILLISECONDS))
                    })
                }
            }
            call.respond(jsonArray.toString())
        }
        staticResources("/static/styles", "static/styles")
        staticResources("/static/images", "static/images")
    }
    val scheduler = WorkScheduler(work_finder, mutableListOf(AnalyzerWorker(gateway)), 30)
    scheduler.start()
}

private fun PipelineContext<Unit, ApplicationCall>.headers(): MutableMap<String, String> {
    val headers = mutableMapOf<String, String>()
    call.request.headers.entries().forEach { entry ->
        headers[entry.key] = entry.value.joinToString()
    }
    return headers
}

private fun data(gateway: AnalyzerDataGateway): MutableList<String> {
    val list = mutableListOf<String>()
    val dbData = gateway.getAll()
    for (data in dbData) {
        list.add(data.toString())
    }
    return list
}

private fun jsondata(gateway: AnalyzerDataGateway): JsonObject {
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