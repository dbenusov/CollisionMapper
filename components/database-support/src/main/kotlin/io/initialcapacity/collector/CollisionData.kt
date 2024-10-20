package io.initialcapacity.collector

data class CollisionData(val case_number: String, val latitude: Float, val longitude: Float) {
    override fun toString(): String {
        return "Case Number: $case_number - Lat: $latitude, Long: $longitude"
    }
}
