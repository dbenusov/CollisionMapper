package io.initialcapacity.analyzer

import io.initialcapacity.collector.CollectorDataGateway
import io.initialcapacity.collector.CollisionData
import io.initialcapacity.collector.testDatabaseTemplate
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class AnalyzerDataGatewayTest {
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
    fun testSave() {
        val core = ClusterCore("12344321", "MULTIPOINT((-122.025146484375 37.36759948730469))", mutableListOf<String>().toList())
        val expected_data = ClusterData(core, 37.36760F, -122.02515F)
        val id = gateway.save(expected_data.core)

        val data = dbTemplate
            .queryOne("select id, ST_AsText(points) as points, ST_Y(ST_Centroid(points)), ST_X(ST_Centroid(points)) from cluster where id = '${id}'") {
                val data_core = ClusterCore(it.getString("id"), it.getString("points"), mutableListOf<String>().toList())
                ClusterData(data_core, it.getFloat("st_y"), it.getFloat("st_x"))
            }

        assertEquals(expected_data.core.points, data!!.core.points)
        assertEquals(id, data.core.id)
        assertEquals(expected_data.latitude, 37.3676F)
        assertEquals(expected_data.longitude, -122.02515F)
    }

    @Test
    fun testRelations() {
        val expected_data = CollisionData("12344321", 37.36760F, -122.02515F, "2024")
        val e0 = collectorGateway.save(expected_data)
        val expected_data1 = CollisionData("12344322", 37.36861F, -123.02515F, "2023")
        val e1 = collectorGateway.save(expected_data1)

        val core = ClusterCore("12344321", "MULTIPOINT((-122.02515 37.36760),(-122.02515 37.36861))", mutableListOf<String>(e0, e1).toList())
        val expected_cluster = ClusterData(core, 37.368107F, -122.02515F)
        val id = gateway.save(expected_cluster.core)
        gateway.updateCollision(e0, id)
        gateway.updateCollision(e1, id)

        val data = gateway.get(id)

        assertEquals(expected_cluster.latitude, data!!.latitude)
        assertEquals(expected_cluster.longitude, data.longitude)
        assertEquals(expected_cluster.core.data_points, data.core.data_points)
    }

    @Test
    fun testGetAll() {
        val expected_data = CollisionData("12344321", 37.36760F, -122.02515F, "2024")
        val e0 = collectorGateway.save(expected_data)
        val expected_data1 = CollisionData("12344322", 37.36861F, -123.02515F, "2023")
        val e1 = collectorGateway.save(expected_data1)

        val core = ClusterCore("12344321", "MULTIPOINT((-122.02515 37.36760),(-122.02515 37.36861))", mutableListOf<String>(e0, e1).toList())
        val expected_cluster = ClusterData(core, 37.368107F, -122.02515F)
        val id = gateway.save(expected_cluster.core)
        gateway.updateCollision(e0, id)
        gateway.updateCollision(e1, id)

        val data = gateway.getAll()

        assertEquals(expected_cluster.latitude, data[0]!!.latitude)
        assertEquals(expected_cluster.longitude, data[0].longitude)
        assertEquals(expected_cluster.core.data_points, data[0].core.data_points)
    }
}
