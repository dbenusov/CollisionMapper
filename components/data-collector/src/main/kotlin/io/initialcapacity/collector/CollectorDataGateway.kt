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

    fun exists(case_number: String): Boolean {
        val id = dbTemplate.queryOne(
            //language=SQL
            "select id from data where case_number = '${case_number}'"
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
