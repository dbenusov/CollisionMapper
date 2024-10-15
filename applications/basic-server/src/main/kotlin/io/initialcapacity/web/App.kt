package io.initialcapacity.web

import freemarker.cache.ClassTemplateLoader
import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.freemarker.FreeMarker
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.util.pipeline.PipelineContext
import org.slf4j.LoggerFactory
import java.util.*
import io.ktor.server.plugins.cors.*

private val logger = LoggerFactory.getLogger(object {}.javaClass.enclosingClass)
private val port = System.getenv("PORT")?.toInt() ?: 8888
private val localUrl = "http://localhost:$port"
private val googleProjectId = System.getenv("PROJECT_NUMBER") ?: ""
private val googleServiceName = System.getenv("K_SERVICE") ?: ""
private val googleProjectRegion = System.getenv("PROJECT_REGION") ?: ""

fun Application.module() {
    logger.info("starting the app")

    install(CORS) {
        // Allow only requests from localhost
        allowHost("127.0.0.1:$port")
    }
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
    install(Routing) {
        get("/") {
            val map = mapOf(
                "headers" to headers(),
                "variables" to variables()
            )
            call.respond(FreeMarkerContent("index.ftl", map))
        }
        staticResources("/static/styles", "static/styles")
        staticResources("/static/images", "static/images")

        get("/ping") {
            call.respondText("Basic Server Responding!", ContentType.Text.Plain)
        }
    }
}

private fun PipelineContext<Unit, ApplicationCall>.headers(): MutableMap<String, String> {
    val headers = mutableMapOf<String, String>()
    call.request.headers.entries().forEach { entry ->
        headers[entry.key] = entry.value.joinToString()
    }
    return headers
}

private fun PipelineContext<Unit, ApplicationCall>.variables(): MutableMap<String, String> {
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
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    embeddedServer(Netty, port = port, host = "0.0.0.0", module = { module() }).start(wait = true)
}
