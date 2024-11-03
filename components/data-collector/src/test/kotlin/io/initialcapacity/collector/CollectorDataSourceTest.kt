package io.initialcapacity.collector

import org.junit.Before
import kotlin.math.exp
import kotlin.test.Test
import kotlin.test.assertEquals


class CollectorDataSourceTest {
    // This is a unit test
    // It tests the logic in processing the data from the real data API but not the hooks in the application.
    @Test
    fun getCollisionData() {
        // I expect the data to have these elements due to manual inspection.
        val expected_data = listOf(
            CollisionData("10018", 32.52434444F, -86.672119440F, "2015"),
            CollisionData("10124", 32.58379167F, -86.464288890F, "2014"),
            CollisionData("10318", 32.64268056F, -86.756822220F, "2014")
        )

        val all_data = CollectorDataSource().getCollisionData("/crashes/GetCrashesByLocation?fromCaseYear=2014&toCaseYear=2015&state=1&county=1&format=json")
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