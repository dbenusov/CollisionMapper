package io.initialcapacity.analyzer

import io.initialcapacity.DatabaseTemplate

class AnalyzerDataGateway(private val dbTemplate: DatabaseTemplate) {
    fun clusterCollisions(range: String) : List<ClusterCore> {
        var list = mutableListOf<ClusterCore>()
        dbTemplate
            .queryOne(
                "WITH clustered_data AS (\n" +
                        "    SELECT \n" +
                        "        data.*,\n" +
                        "        ST_ClusterWithinWin(location::geometry, $range) OVER () AS cluster_num\n" +
                        "    FROM data\n" +
                        ")\n" +
                        "SELECT\n" +
                        "    cluster_num,\n" +
                        "    ST_AsText(ST_Collect(location::geometry)) AS geom_collection,\n" +
                        "    array_agg(id) AS data_keys\n" +
                        "FROM clustered_data\n" +
                        "GROUP BY cluster_num;")
            {
                do {
                    val idsArray = it.getArray("data_keys")
                    val ids = idsArray.array as Array<Int>
                    val stringArray = ids.map { it.toString() }.toTypedArray()
                    list.add(ClusterCore(it.getString("cluster_num"), it.getString("geom_collection"), stringArray.toList()))
                } while (it.next())
            }
        return list
    }

    fun getAll() : List<ClusterData> {
        var list = mutableListOf<ClusterData>()
        dbTemplate.queryOne("select id, ST_AsText(points) as points, ST_Y(ST_Centroid(points)), ST_X(ST_Centroid(points)) from cluster") { it ->
            do {
                var cluster = get(it.getString("id"))
                if (cluster != null)
                    list.add(cluster)
            } while (it.next())
            }

        return list.toList()
    }

    fun get(id: String) : ClusterData? {
        return dbTemplate
            .queryOne("select id, ST_AsText(points) as points, ST_Y(ST_Centroid(points)), ST_X(ST_Centroid(points)) from cluster where id = '${id}'") { it ->
                var list = mutableListOf<String>()
                dbTemplate.queryOne("select id from data where cluster_id = $id") { dit ->
                    do {
                        list.add(dit.getString("id"))
                    } while(dit.next())
                }
                val core = ClusterCore(it.getString("id"), it.getString("points"), list)
                ClusterData(core, it.getFloat("st_y"), it.getFloat("st_x"))
            }
    }

    fun updateCollision(id: String, cluster_id: String) : Unit {
        dbTemplate.execute(
            "UPDATE data SET cluster_id = $cluster_id WHERE id = $id"
        )
    }

    fun save(data: ClusterCore): String {
        return dbTemplate.queryOne(
            //language=SQL
            "insert into cluster (points) values (ST_GeomFromText(?)) RETURNING id;",
            data.points
        ) {
            it.getString("id")
        }?: "NOT_FOUND"
    }
}
