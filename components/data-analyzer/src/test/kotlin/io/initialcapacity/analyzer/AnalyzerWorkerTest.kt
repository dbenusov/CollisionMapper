package io.initialcapacity.analyzer

import io.initialcapacity.collector.*
import org.junit.Before
import kotlin.math.exp
import kotlin.test.Test
import kotlin.test.assertEquals

class AnalyzerWorkerTest {
    private val dbName = "collisions"
    private val dbTemplate = testDatabaseTemplate(dbName)
    private val gateway = AnalyzerDataGateway(dbTemplate)
    private val collectorGateway = CollectorDataGateway(dbTemplate)

    @Before
    fun setUp() {
        dbTemplate.execute("delete from data")
        dbTemplate.execute("delete from cluster")
    }

    @Test
    fun testExecute() {
        val worker = CollectorWorker(collectorGateway)
        val task = CollectorTask("/crashes/GetCrashesByLocation?fromCaseYear=2014&toCaseYear=2015&state=1&county=1&format=json")
        worker.execute(task)

        val worker1 = AnalyzerWorker(gateway)
        val task1 = AnalyzerTask("test")
        worker1.execute(task1)

        val expected_data = listOf(CollisionData("10018", 32.52434444F, -86.672119440F, "2015"), CollisionData("10124", 32.58379167F, -86.464288890F, "2014"), CollisionData("10318", 32.64268056F, -86.756822220F, "2014"))
        val clusters = gateway.getAll()
        for (cluster in clusters) {
            println(cluster)
        }
    }
}
