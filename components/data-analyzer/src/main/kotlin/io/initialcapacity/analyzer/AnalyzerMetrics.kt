package io.initialcapacity.analyzer

import kotlin.time.Duration

data class AnalyzerMetrics(var available_clusters: Int, var processed_clusters: Int, var time: Duration)
