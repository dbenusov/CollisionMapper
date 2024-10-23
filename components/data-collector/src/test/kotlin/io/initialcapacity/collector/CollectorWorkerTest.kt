package io.initialcapacity.collector

import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class CollectorWorkerTest {
    private val dbName = "collisions"
    private val tableName = "data"
    private val dbTemplate = testDatabaseTemplate(dbName)
    private val gateway = CollectorDataGateway(dbTemplate)

    @Before
    fun setUp() {
        dbTemplate.execute("delete from $tableName")
    }

    @Test
    fun testExecute() {
        val worker = CollectorWorker(gateway)
        val task = CollectorTask("/crashes/GetCrashesByLocation?fromCaseYear=2014&toCaseYear=2015&state=1&county=1&format=json")
        worker.execute(task)

        val expected_data = listOf(CollisionData("10018", 32.52434444F, -86.672119440F, "2015"), CollisionData("10124", 32.58379167F, -86.464288890F, "2014"), CollisionData("10318", 32.64268056F, -86.756822220F, "2014"))

        for (expected_entry in expected_data) {
            val data = dbTemplate
                .queryOne("select * from $tableName where case_number = '${expected_entry.case_number}'") {
                    CollisionData(it.getString("case_number"), it.getFloat("latitude"), it.getFloat("longitude"), it.getString("date_year"))
                }
            assertEquals(expected_entry, data)
        }
    }
}
