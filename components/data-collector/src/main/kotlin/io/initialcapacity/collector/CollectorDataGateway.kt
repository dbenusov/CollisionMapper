package io.initialcapacity.collector
import java.sql.ResultSet

class CollectorDataGateway(private val dbTemplate: DatabaseTemplate) {
    fun getAll() : List<CollisionData> {
        var list = mutableListOf<CollisionData>()
        dbTemplate.queryOne("select case_number, ST_X(location::geometry), ST_Y(location::geometry), date_year from data") {
            do {
                val col = CollisionData(it.getString("case_number"), it.getFloat("st_y"), it.getFloat("st_x"), it.getString("date_year"))
                list.add(col)
            } while(it.next())
        }

        return list.toList()
    }

    fun save(data: CollisionData): Unit = dbTemplate.execute(
        //language=SQL
        "insert into data (case_number, date_year, location) values (?, ?, ST_MakePoint(?, ?))",
        data.case_number, data.year, data.longitude, data.latitude
    )
}
