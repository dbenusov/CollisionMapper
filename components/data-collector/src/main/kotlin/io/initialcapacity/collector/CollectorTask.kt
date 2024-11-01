package io.initialcapacity.collector

data class CollectorTask(val queryUrl: String, var complete: Boolean = false, var metrics: CollectorMetrics)