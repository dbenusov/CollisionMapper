package io.initialcapacity.collector

import kotlin.time.Duration

data class CollectorMetrics(val start_year: String, val end_year: String, var collisions: Int = 0, var time: Duration = Duration.ZERO)
