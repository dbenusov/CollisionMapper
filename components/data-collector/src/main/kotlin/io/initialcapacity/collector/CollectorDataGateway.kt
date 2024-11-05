package io.initialcapacity.collector
import io.initialcapacity.DatabaseTemplate

class CollectorDataGateway(private val dbTemplate: DatabaseTemplate) {
    fun getAll() : List<CollisionData> {
        var list = mutableListOf<CollisionData>()
        dbTemplate.queryOne("select id, data.*, ST_X(location::geometry), ST_Y(location::geometry) from data") {
            do {
                val col = CollisionData(it.getString("case_number"), it.getFloat("st_y"), it.getFloat("st_x"), it.getString("date_year"), it.getString("id"))
                list.add(col)
            } while(it.next())
        }

        return list.toList()
    }

    fun saveProcessed(case_year: String, state: String) {
        dbTemplate.execute(
            //language=SQL
            "insert into processed_data (case_year, state) values (?, ?);", case_year, state
        )
    }

    fun isProcessed(case_year: String, state: String): Boolean {
        val id = dbTemplate.queryOne(
            //language=SQL
            "select id from processed_data where case_year = '${case_year}' AND state = '${state}'"
        ) {
            it.getString("id")
        }
        return id != null
    }

    fun get(id: String) : CollisionData? {
        return dbTemplate.queryOne(
            //language=SQL
            "select data* from data where id = $id"
        ) {
            CollisionData(it.getString("case_number"), it.getFloat("st_y"), it.getFloat("st_x"), it.getString("date_year"), it.getString("id"))
        }
    }

    fun save(data: CollisionData): String {
        return dbTemplate.queryOne(
            //language=SQL
            "insert into data (case_number, date_year, location) values (?, ?, ST_MakePoint(?, ?)) RETURNING id;",
            data.case_number, data.year, data.longitude, data.latitude
        ) {
            it.getString("id")
        }?: "NOT_FOUND"
    }
}
