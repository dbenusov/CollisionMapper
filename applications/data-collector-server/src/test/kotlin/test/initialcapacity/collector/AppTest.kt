package test.initialcapacity.collector

import io.initialcapacity.collector.CollectorDataGateway
import io.initialcapacity.collector.DatabaseTemplate
import io.initialcapacity.collector.module
import io.initialcapacity.collector.testDatabaseTemplate
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.Database
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class AppTest {
    private val dbName = "collisions"
    private val tableName = "data"
    private val dbTemplate = testDatabaseTemplate(dbName)
    private val gateway = CollectorDataGateway(dbTemplate)

    fun setUp() {
        dbTemplate.execute("delete from $tableName")
    }

    @Test
    fun testEmptyHome() = testApp {
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), "hi collector!")
    }

    private fun testApp(block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) {
        testApplication {
            application { module(gateway) }
            block(client)
        }
    }
}