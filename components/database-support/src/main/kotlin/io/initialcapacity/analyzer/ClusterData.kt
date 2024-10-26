package io.initialcapacity.analyzer

data class ClusterData(val core: ClusterCore, val latitude: Float, val longitude: Float) {
    override fun toString(): String {
        return "ID: $core.id - Lat: $latitude, Long: $longitude, Data Points: $core.data_points"
    }
}
