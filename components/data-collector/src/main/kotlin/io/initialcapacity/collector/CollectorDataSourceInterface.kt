package io.initialcapacity.collector

import kotlinx.serialization.json.JsonObject

interface CollectorDataSourceInterface {
    fun getCollisionData(url: String) : List<CollisionData>
}