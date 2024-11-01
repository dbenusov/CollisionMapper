package io.initialcapacity.web

import freemarker.cache.ClassTemplateLoader
import io.initialcapacity.DatabaseConfiguration
import io.initialcapacity.DatabaseTemplate
import io.initialcapacity.display.DisplayDataGateway
import io.initialcapacity.display.DisplayMetrics
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.freemarker.FreeMarker
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.util.pipeline.PipelineContext
import org.slf4j.LoggerFactory
import java.util.*
import io.ktor.server.plugins.cors.*
import kotlinx.serialization.json.*
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

private val logger = LoggerFactory.getLogger(object {}.javaClass.enclosingClass)
private val port = System.getenv("PORT")?.toInt() ?: 8888
private val localUrl = "http://localhost:$port"
private val googleProjectId = System.getenv("PROJECT_NUMBER") ?: ""
private val googleServiceName = System.getenv("K_SERVICE") ?: ""
private val googleProjectRegion = System.getenv("PROJECT_REGION") ?: ""

// Metrics
private val time_source = TimeSource.Monotonic
private val metric_buffer: Queue<DisplayMetrics> = LinkedList<DisplayMetrics>()

fun Application.module(gateway: DisplayDataGateway) {
    logger.info("starting the app")

    install(CORS) {
        // Allow only requests from localhost
        allowHost("127.0.0.1:$port")
    }
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
    install(Routing) {
        get("/") {
            call.respondRedirect("/static/html/index.html")
        }

        get("/test") {
            val map = mapOf(
                "headers" to headers(),
                "variables" to variables()
            )
            call.respond(FreeMarkerContent("index.ftl", map))
        }

        get("/map") {
            call.respondRedirect("/static/html/index.html")
        }
        staticResources("/static/styles", "static/styles")
        staticResources("/static/images", "static/images")
        staticResources("/static/scripts", "static/scripts")
        staticResources("/static/html", "static/html")

        get("/json-data/{lower-lat}/{lower-long}/{upper-lat}/{upper-long}") {
            val start = time_source.markNow()
            val lower_lat = call.parameters["lower-lat"]
            val lower_long = call.parameters["lower-long"]
            val upper_lat = call.parameters["upper-lat"]
            val upper_long = call.parameters["upper-long"]
            if (lower_lat == null || lower_long == null || upper_lat == null || upper_long == null) {
                call.respond(HttpStatusCode.UnprocessableEntity, "Missing or invalid parameters")
                return@get
            }
            val data = jsondata(gateway, lower_lat, lower_long, upper_lat, upper_long)
            call.respond(data.first.toString())
            metric_buffer.add(DisplayMetrics(data.second, time_source.markNow() - start))
            if (metric_buffer.size >= 10)
                metric_buffer.remove()
        }

        get("/metrics") {
            val jsonArray = buildJsonArray {
                for (metric in metric_buffer) {
                    add(buildJsonObject {
                        put("clusters", metric.clusters)
                        put("duration_ms", metric.time.toString(DurationUnit.MILLISECONDS))
                    })
                }
            }
            call.respond(jsonArray.toString())
        }

        get("/ping") {
            call.respondText("Basic Server Responding!", ContentType.Text.Plain)
        }
    }
}

private fun jsondata(gateway: DisplayDataGateway, min_lat: String, min_long: String, max_lat: String, max_long: String): Pair<JsonObject, Int> {
    val dbData = gateway.getTopTen(min_lat, min_long, max_lat, max_long)
    val jsonArray = buildJsonArray {
        for (data in dbData) {
            add(buildJsonObject {
                put("id", data.core.id)
                put("latitude", data.latitude)
                put("longitude", data.longitude)
                put("collisions", data.core.data_points.size)
            })
        }
    }

    return Pair(buildJsonObject {
        put("clusters", jsonArray)
    }, dbData.size)
}

private fun PipelineContext<Unit, ApplicationCall>.headers(): MutableMap<String, String> {
    val headers = mutableMapOf<String, String>()
    call.request.headers.entries().forEach { entry ->
        headers[entry.key] = entry.value.joinToString()
    }
    return headers
}

private fun variables(): MutableMap<String, String> {
    val variables = mutableMapOf<String, String>()
    variables["project_id"] = googleProjectId
    variables["project_region"] = googleProjectRegion
    variables["service_name"] = googleServiceName
    // Set the url for this web server
    if (googleProjectId.isEmpty() || googleProjectRegion.isEmpty() || googleServiceName.isEmpty())
        variables["local_url"] = localUrl
    else
        variables["local_url"] = "https://$googleServiceName-$googleProjectId.$googleProjectRegion.run.app/"
    return variables
}

fun main() {
    val database = DatabaseConfiguration()
    val dbTemplate = DatabaseTemplate(database.db)
    val gateway = DisplayDataGateway(dbTemplate)
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    embeddedServer(Netty, port = port, host = "0.0.0.0", module = { module(gateway) }).start(wait = true)
}
