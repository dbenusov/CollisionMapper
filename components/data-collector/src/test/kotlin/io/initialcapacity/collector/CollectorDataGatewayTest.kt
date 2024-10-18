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
        val expected_data = CollisionData("12344321", 1234.4321F, 5678.8765F)
        gateway.save(expected_data)

        val data = dbTemplate
            .queryOne("select * from $tableName where case_number = '${expected_data.case_number}'") {
                CollisionData(it.getString("case_number"), it.getFloat("latitude"), it.getFloat("longitude"))
            }
        assertEquals(expected_data, data)
    }
}
