package io.initialcapacity.analyzer

import io.initialcapacity.workflow.Worker
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class AnalyzerWorker(val gateway: AnalyzerDataGateway, override val name: String = "data-analyzer") : Worker<AnalyzerTask> {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun execute(task: AnalyzerTask) {
        runBlocking {
            logger.info("starting data analysis.")

            // todo - data analysis happens here
            val clusters = gateway.clusterCollisions();
            for (cluster in clusters) {
                val id = gateway.save(cluster)
                for (collision in cluster.data_points) {
                    gateway.updateCollision(collision, id);
                }
            }

            logger.info("completed data analysis.")
        }
    }
}