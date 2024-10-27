package io.initialcapacity.analyzer

import io.initialcapacity.workflow.Worker
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class AnalyzerWorker(val gateway: AnalyzerDataGateway, override val name: String = "data-analyzer") : Worker<AnalyzerTask> {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun execute(task: AnalyzerTask) {
        task.in_process = true
        runBlocking {
            logger.info("starting data analysis.")

            // todo - data analysis happens here
            val clusters = gateway.clusterCollisions(task.range);
            var i = 0
            for (cluster in clusters) {
                logger.info("Processing cluster $i of ${clusters.size}")
                val id = gateway.save(cluster)
                gateway.updateClusterSize(id, cluster.data_points.size)
                for (collision in cluster.data_points) {
                    gateway.updateCollision(collision, id);
                }
                i++
            }

            logger.info("completed data analysis.")
        }
    }
}