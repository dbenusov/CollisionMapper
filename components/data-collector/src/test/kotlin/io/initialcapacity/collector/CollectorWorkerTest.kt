package io.initialcapacity.collector

import org.junit.Before
import kotlin.math.exp
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

    // This is an integration test
    // It tests the connections between the workers, database, and the mocked data API
    @Test
    fun testExecute() {
        val expected_data = listOf(CollisionData("10018", 32.52434444F, -86.672119440F, "2015"), CollisionData("10124", 32.58379167F, -86.464288890F, "2014"), CollisionData("10318", 32.64268056F, -86.756822220F, "2014"))
        // This runs the worker with a fake data source. The fake returns the list of data passed into it.
        val worker = CollectorWorker(gateway, "test", FakeCollectorDataSource(expected_data))
        val task = CollectorTask("/crashes/GetCrashesByLocation?fromCaseYear=2014&toCaseYear=2015&state=1&county=1&format=json", false, CollectorMetrics("1234", "1234"))
        worker.execute(task)

        val all_data = gateway.getAll()
        assertEquals(all_data.size, expected_data.size)
        for (expected_entry in expected_data) {
            var found = false
            for (data in all_data) {
                if (data.case_number == expected_entry.case_number)
                    found = true
            }
            assert(found) { "Missing entry ${expected_entry.toString()}" }
        }
    }
}
