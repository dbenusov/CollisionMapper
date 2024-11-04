package io.initialcapacity.collector

import freemarker.cache.ClassTemplateLoader
import io.initialcapacity.DatabaseConfiguration
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
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Gauge
import io.prometheus.client.exporter.common.TextFormat
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.*
import org.slf4j.LoggerFactory
import kotlin.time.DurationUnit

private val work_finder = CollectorWorkFinder()

// Define gauges for collisions and duration, with labels
val collisionsGauge = Gauge.build()
    .name("collisions_total")
    .help("Total number of collisions")
    .labelNames("start_year", "end_year")  // Define labels
    .register()

val durationGauge = Gauge.build()
    .name("operation_duration_ms")
    .help("Duration of the operation in milliseconds")
    .labelNames("start_year", "end_year")  // Define labels
    .register()

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

        get("/health-check") {
            if (work_finder.checkStatus())
                call.respondText("Ready", ContentType.Text.Html)
            else
                call.respondText("Processing", ContentType.Text.Html)
        }

        get("metrics") {
            val metrics = work_finder.getMetrics()
            for (metric in metrics) {
                collisionsGauge.labels(metric.start_year, metric.end_year).set(metric.collisions.toDouble())
                durationGauge.labels(metric.start_year, metric.end_year).set(metric.time.toDouble(DurationUnit.MILLISECONDS))
            }

            // Expose metrics in Prometheus format
            call.respondTextWriter(ContentType.Text.Plain) {
                TextFormat.write004(this, CollectorRegistry.defaultRegistry.metricFamilySamples())
            }
        }
        staticResources("/static/styles", "static/styles")
        staticResources("/static/images", "static/images")
    }
    val scheduler = WorkScheduler<CollectorTask>(work_finder, mutableListOf(CollectorWorker(gateway)), 30)
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

private val logger = LoggerFactory.getLogger("main")

fun main() {
    val database = DatabaseConfiguration()
    val dbTemplate = DatabaseTemplate(database.db)
    val gateway = CollectorDataGateway(dbTemplate)
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    val port = System.getenv("PORT")?.toInt() ?: 8886
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = { module(gateway) }).start(wait = true)
}