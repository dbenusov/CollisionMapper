package test.initialcapacity.web

import io.initialcapacity.collector.testDatabaseTemplate
import io.initialcapacity.display.DisplayDataGateway
import io.initialcapacity.web.module
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class AppTest {
    private val dbName = "collisions"
    private val dbTemplate = testDatabaseTemplate(dbName)
    private val gateway = DisplayDataGateway(dbTemplate)

    fun setUp() {
        dbTemplate.execute("delete from data")
        dbTemplate.execute("delete from cluster")
    }

    @Test
    fun testEmptyHome() = testApp {
        val response = client.get("/ping")
        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), "Basic Server Responding!")
    }

    private fun testApp(block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) {
        testApplication {
            application { module(gateway) }
            block(client)
        }
    }
}
