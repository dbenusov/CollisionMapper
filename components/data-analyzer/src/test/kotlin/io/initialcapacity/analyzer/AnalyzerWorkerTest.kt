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
        dbTemplate.execute("delete from processed_data")
    }

    // This is an integration test
    // It tests the connections between the collector workers, analyzer workers, database, and the real data API
    @Test
    fun testExecute() {
        val worker = CollectorWorker(collectorGateway)
        val task = CollectorTask("/crashes/GetCrashesByLocation?fromCaseYear=2014&toCaseYear=2015&state=1&county=1&format=json", false, CollectorMetrics("1234", "1234", "1"))
        worker.execute(task)

        val worker1 = AnalyzerWorker(gateway)
        val task1 = AnalyzerTask("0.1") // 11.1km clusters
        worker1.execute(task1)

        val clusters = gateway.getAll()
        assertEquals(4, clusters.size)
        val large_cluster = clusters[1] // One cluster always has more points! How deadly!
        assertEquals(12, large_cluster.core.data_points.size)
        assertEquals(32.544567F, large_cluster.latitude)
        assertEquals(-86.4841F, large_cluster.longitude)
    }
}
