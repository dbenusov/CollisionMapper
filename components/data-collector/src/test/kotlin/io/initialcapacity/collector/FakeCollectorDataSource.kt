package io.initialcapacity.collector

class FakeCollectorDataSource(private val collisions: List<CollisionData>) : CollectorDataSourceInterface {
    override fun getCollisionData(url: String): List<CollisionData> {
        return collisions
    }
}