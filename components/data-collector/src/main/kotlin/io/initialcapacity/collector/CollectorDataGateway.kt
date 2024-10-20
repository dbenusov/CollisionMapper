package io.initialcapacity.collector
import java.sql.ResultSet

class CollectorDataGateway(private val dbTemplate: DatabaseTemplate) {
    fun getAll() : List<CollisionData> {
        var list = mutableListOf<CollisionData>()
        val result = dbTemplate.queryOne("select * from data") {it}
        if (result == null)
            return list.toList()

        do {
            val col = CollisionData(result.getString("case_number"), result.getFloat("latitude"), result.getFloat("longitude"))
            list.add(col)
        } while (result.next())
        return list.toList()
    }

    fun save(data: CollisionData): Unit = dbTemplate.execute(
        //language=SQL
        "insert into data (case_number, latitude, longitude) values (?, ?, ?)",
        data.case_number, data.latitude, data.longitude
    )
}
