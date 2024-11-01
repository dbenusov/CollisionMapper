package io.initialcapacity.collector

import io.initialcapacity.workflow.Worker
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import kotlin.time.TimeSource

class CollectorWorker(val gateway: CollectorDataGateway, override val name: String = "data-collector") :
    Worker<CollectorTask> {
    private val time_source = TimeSource.Monotonic
    private var start = time_source.markNow()
    private val logger = LoggerFactory.getLogger(this.javaClass)
    val client = HttpClient(CIO)

    fun removeExtraQuotes(input: String): String {
        return if (input.startsWith("\"") && input.endsWith("\"")) {
            input.substring(1, input.length - 1) // Remove the outer quotes
        } else {
            input
        }
    }

    override fun execute(task: CollectorTask) {
        start = time_source.markNow()
        runBlocking {
            logger.info("starting data collection.")

            // todo - data collection happens here
            val response: HttpResponse = client.get("https://crashviewer.nhtsa.dot.gov/CrashAPI/${task.queryUrl}")
            val content = response.bodyAsChannel().readRemaining().readText()
            val json_data = Json.parseToJsonElement(content).jsonObject

            val collisions = json_data["Results"]?.jsonArray
            if (collisions == null) {
                logger.error("Failed to complete $task")
                return@runBlocking
            }

            task.metrics.collisions = collisions.get(0).jsonArray.size
            for (collision in collisions.get(0).jsonArray) {
                val collision_json = collision.jsonObject
                val case_number = removeExtraQuotes(collision_json["ST_CASE"].toString())
                val latitude = collision_json["LATITUDE"]?.jsonPrimitive?.float
                val longitude = collision_json["LONGITUD"]?.jsonPrimitive?.float
                val year = removeExtraQuotes(collision_json["CaseYear"].toString())
                if (latitude == null || longitude == null || year.isEmpty()) {
                    logger.error("Invalid data for $case_number")
                    continue
                }

                gateway.save(CollisionData(case_number, latitude, longitude, year))
            }

            task.metrics.time = time_source.markNow() - start
            logger.info("completed data collection.")
        }
    }
}