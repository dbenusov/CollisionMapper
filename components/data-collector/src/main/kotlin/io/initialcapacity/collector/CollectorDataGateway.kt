package io.initialcapacity.collector
import java.sql.ResultSet

class CollectorDataGateway(private val dbTemplate: DatabaseTemplate) {
    fun getAll() : List<CollisionData> {
        var list = mutableListOf<CollisionData>()
        dbTemplate.queryOne("select * from data") {
            do {
                val col = CollisionData(it.getString("case_number"), it.getFloat("latitude"), it.getFloat("longitude"), it.getString("date_year"))
                list.add(col)
            } while(it.next())
        }

        return list.toList()
    }

    fun save(data: CollisionData): Unit = dbTemplate.execute(
        //language=SQL
        "insert into data (case_number, latitude, longitude, date_year) values (?, ?, ?, ?)",
        data.case_number, data.latitude, data.longitude, data.year
    )
}
