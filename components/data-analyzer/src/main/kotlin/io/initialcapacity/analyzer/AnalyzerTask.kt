package io.initialcapacity.analyzer

import kotlin.time.Duration

//decimal  degrees    distance
//places
//-------------------------------
//0        1.0        111 km
//1        0.1        11.1 km
//2        0.01       1.11 km
//3        0.001      111 m
//4        0.0001     11.1 m
//5        0.00001    1.11 m
//6        0.000001   0.111 m
//7        0.0000001  1.11 cm
//8        0.00000001 1.11 mm
data class AnalyzerTask(val range: String, var complete: Boolean = false, var in_process: Boolean = false, var metrics: AnalyzerMetrics = AnalyzerMetrics(0,0,
    Duration.ZERO)
)