package io.initialcapacity.collector

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory

class CollectorDataSource : CollectorDataSourceInterface {
    private val client = HttpClient(CIO)
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun removeExtraQuotes(input: String): String {
        return if (input.startsWith("\"") && input.endsWith("\"")) {
            input.substring(1, input.length - 1) // Remove the outer quotes
        } else {
            input
        }
    }

    override fun getCollisionData(url: String) : List<CollisionData> {
        var list = mutableListOf<CollisionData>()
        runBlocking {
            val response = client.get("https://crashviewer.nhtsa.dot.gov/CrashAPI/${url}")
            val content = response.bodyAsChannel().readRemaining().readText()
            val json_data = Json.parseToJsonElement(content).jsonObject

            val collisions = json_data["Results"]?.jsonArray
            if (collisions == null) {
                logger.error("Failed to complete $url")
                return@runBlocking list
            }

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

                list.add(CollisionData(case_number, latitude, longitude, year))
            }

        }
        return list
    }
}