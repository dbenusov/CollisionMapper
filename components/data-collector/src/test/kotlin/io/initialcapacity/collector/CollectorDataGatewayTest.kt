package io.initialcapacity.collector

import org.junit.Assert.assertTrue
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CollectorDataGatewayTest {
    private val dbName = "collisions"
    private val tableName = "data"
    private val dbTemplate = testDatabaseTemplate(dbName)
    private val gateway = CollectorDataGateway(dbTemplate)

    @Before
    fun setUp() {
        dbTemplate.execute("delete from $tableName")
    }

    // This is a unit test
    // We test only the database connection here
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

    // This is a unit test
    // We test only the database connection here
    @Test
    fun testExists() {
        gateway.saveProcessed("1234", "4321")

        assertTrue(gateway.isProcessed("1234", "4321"))
        assertFalse(gateway.isProcessed("4321", "9876"))
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
    }
}
