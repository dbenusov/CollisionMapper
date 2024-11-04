package io.initialcapacity.analyzer

import io.initialcapacity.workflow.Worker
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.TimeSource

class AnalyzerWorker(val gateway: AnalyzerDataGateway, override val name: String = "data-analyzer") : Worker<AnalyzerTask> {
    private val time_source = TimeSource.Monotonic
    private var start = time_source.markNow()
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun execute(task: AnalyzerTask) {
        task.in_process = true
        start = time_source.markNow()
        runBlocking {
            logger.info("starting data analysis.")

            val clusters = gateway.clusterCollisions(task.range);
            task.metrics.available_clusters = clusters.size
            task.metrics.processed_clusters = 0
            for (cluster in clusters) {
                logger.info("Processing cluster ${task.metrics.processed_clusters} of ${task.metrics.available_clusters}")
                val id = gateway.save(cluster)
                gateway.updateClusterSize(id, cluster.data_points.size)
                for (collision in cluster.data_points) {
                    gateway.updateCollision(collision, id);
                }
                task.metrics.processed_clusters++
                task.metrics.time = time_source.markNow() - start
            }
            logger.info("completed data analysis.")
        }
    }
}