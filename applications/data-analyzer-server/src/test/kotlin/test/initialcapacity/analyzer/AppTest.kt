package test.initialcapacity.analyzer

import io.initialcapacity.analyzer.AnalyzerDataGateway
import io.initialcapacity.analyzer.module
import io.initialcapacity.collector.testDatabaseTemplate
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import junit.framework.TestCase.assertEquals
import kotlin.test.Test
import kotlin.test.assertContains

class AppTest {
    private val dbName = "collisions"
    private val dbTemplate = testDatabaseTemplate(dbName)
    private val gateway = AnalyzerDataGateway(dbTemplate)

    fun setUp() {
        dbTemplate.execute("delete from data")
        dbTemplate.execute("delete from cluster")
    }

    @Test
    fun testEmptyHome() = testApp {
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), "hi!")
    }

    private fun testApp(block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) {
        testApplication {
            application { module(gateway) }
            block(client)
        }
    }
}