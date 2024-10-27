package io.initialcapacity.display

import io.initialcapacity.DatabaseTemplate
import io.initialcapacity.analyzer.ClusterCore
import io.initialcapacity.analyzer.ClusterData

class DisplayDataGateway(private val dbTemplate: DatabaseTemplate) {
    fun getTopTen(min_lat: String, min_long: String, max_lat: String, max_long: String) : List<ClusterData> {
        var list = mutableListOf<ClusterData>()
        dbTemplate.queryOne(
            "SELECT \n" +
                "    id,\n" +
                "    list_size,\n" +
                "    ST_AsText(points) as points,\n" +
                "    ST_Y(ST_Centroid(points)),\n" +
                "    ST_X(ST_Centroid(points))\n" +
                "FROM cluster\n" +
                "WHERE ST_Intersects(\n" +
                "    points,\n" +
                "    ST_MakeEnvelope($min_long, $min_lat, $max_long, $max_lat)\n" +
                ")\n" +
                "ORDER BY list_size DESC\n" +
                "LIMIT 10;"){ it ->
            // We add only the top ten clusters
            var index = 0
            do {
                // We do not need the list of points for display purposes.
                // give an empty list. We trust that the database sorted and from highest to lowest.
                val listOfEmptyStrings = List(it.getInt("list_size")) { "" }
                val core = ClusterCore(it.getString("id"), it.getString("points"), listOfEmptyStrings)
                list.add(ClusterData(core, it.getFloat("st_y"), it.getFloat("st_x")))
                index++
            } while (it.next() && index <= 10)
            }

        return list.toList()
    }
}
