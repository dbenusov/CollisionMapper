package io.initialcapacity.collector

import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class CollectorDataGatewayTest {
    private val dbName = "collisions"
    private val tableName = "data"
    private val dbTemplate = testDatabaseTemplate(dbName)
    private val gateway = CollectorDataGateway(dbTemplate)

    @Before
    fun setUp() {
        dbTemplate.execute("delete from $tableName")
    }

    @Test
    fun testSave() {
        val expected_data = CollisionData("12344321", 37.36760F, -122.02515F, "2024")
        expected_data.id = gateway.save(expected_data)

        val data = dbTemplate
            .queryOne("select data.*, ST_X(location::geometry), ST_Y(location::geometry) from data where case_number = '${expected_data.case_number}'") {
                CollisionData(it.getString("case_number"), it.getFloat("st_y"), it.getFloat("st_x"), it.getString("date_year"), it.getString("id"))
            }
        assertEquals(expected_data, data)
    }

    @Test
    fun testGetAll() {
        val expected_data = CollisionData("12344321", 37.36760F, -122.02515F, "2024")
        expected_data.id = gateway.save(expected_data)
        val expected_data1 = CollisionData("12344322", 37.36861F, -123.02515F, "2023")
        expected_data1.id = gateway.save(expected_data1)
        gateway.save(CollisionData("12344323", 37.36862F, -123.02515F, "2023"))

        val list = gateway.getAll()
        assertEquals(expected_data, list[0])
        assertEquals(expected_data1, list[1])

        val data2 = dbTemplate
            .queryOne(
                    "SELECT " +
                            "case_number, " +
                            "ST_ClusterDBSCAN(location::geometry, eps => 0.1, minpoints => 1) over () AS cluster " +
                        "FROM data")
            {
                do {
                    println(it.getString("case_number") + " " + it.getString("cluster"))
                } while (it.next())
            }

        val data3 = dbTemplate
            .queryOne(
                "SELECT " +
                    "data.*, " +
                    "ST_ClusterWithinWin(location::geometry, 0.1) over () AS cluster " +
                "FROM data")
            {
                do {
                    println(it.getString("case_number") + " " + it.getString("cluster"))
                } while (it.next())
            }

        val data4 = dbTemplate
            .queryOne(
                "WITH clustered_data AS (\n" +
                        "    SELECT \n" +
                        "        data.*,\n" +
                        "        ST_ClusterWithinWin(location::geometry, 0.1) OVER () AS cluster\n" +
                        "    FROM data\n" +
                        ")\n" +
                        "SELECT\n" +
                        "    cluster,\n" +
                        "    ST_AsText(ST_Collect(location::geometry)) AS geom_collection,\n" +
                        "    array_agg(clustered_data.*) AS data_items\n" +
                        "FROM clustered_data\n" +
                        "GROUP BY cluster;")
            {
                do {
                    println(it.getString("cluster") + " " + it.getString("geom_collection") + it.getString("data_items"))
                } while (it.next())
            }

        val data5 = dbTemplate
            .queryOne(
                "WITH clustered_data AS (\n" +
                        "    SELECT \n" +
                        "        data.*,\n" +
                        "        ST_ClusterWithinWin(location::geometry, 0.1) OVER () AS cluster_num\n" +
                        "    FROM data\n" +
                        ")\n" +
                        "SELECT\n" +
                        "    cluster_num,\n" +
                        "    ST_AsText(ST_Collect(location::geometry)) AS geom_collection,\n" +
                        "    array_agg(id) AS data_keys\n" +
                        "FROM clustered_data\n" +
                        "GROUP BY cluster_num;")
            {
                do {
                    println(it.getString("cluster_num") + " " + it.getString("geom_collection") + it.getString("data_keys"))
                } while (it.next())
            }
    }
}
