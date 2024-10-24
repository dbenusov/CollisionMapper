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
        gateway.save(expected_data)

        val data = dbTemplate
            .queryOne("select case_number, ST_X(location::geometry), ST_Y(location::geometry), date_year from data where case_number = '${expected_data.case_number}'") {
                CollisionData(it.getString("case_number"), it.getFloat("st_y"), it.getFloat("st_x"), it.getString("date_year"))
            }
        assertEquals(expected_data, data)
    }

    @Test
    fun testGetAll() {
        val expected_data = CollisionData("12344321", 37.36760F, -122.02515F, "2024")
        gateway.save(expected_data)
        val expected_data1 = CollisionData("12344322", 37.36761F, -122.02515F, "2023")
        gateway.save(expected_data1)

        val list = gateway.getAll()
        assertEquals(expected_data, list[0])
        assertEquals(expected_data1, list[1])
    }
}
