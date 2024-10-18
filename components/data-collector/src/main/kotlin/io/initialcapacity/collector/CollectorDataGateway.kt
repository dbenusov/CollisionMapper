package io.initialcapacity.collector

class CollectorDataGateway(private val dbTemplate: DatabaseTemplate) {
    fun save(data: CollisionData): Unit = dbTemplate.execute(
        //language=SQL
        "insert into data (case_number, latitude, longitude) values (?, ?, ?)",
        data.case_number, data.latitude, data.longitude
    )
}
