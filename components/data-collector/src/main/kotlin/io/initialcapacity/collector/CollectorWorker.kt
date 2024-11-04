package io.initialcapacity.collector

import io.initialcapacity.workflow.Worker
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import kotlin.time.TimeSource

class CollectorWorker(val gateway: CollectorDataGateway, override val name: String = "data-collector", val source: CollectorDataSourceInterface = CollectorDataSource()) :
    Worker<CollectorTask> {
    private val time_source = TimeSource.Monotonic
    private var start = time_source.markNow()
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun execute(task: CollectorTask) {
        start = time_source.markNow()
        runBlocking {
            logger.info("starting data collection.")

            val collisions = source.getCollisionData(task.queryUrl)
            task.metrics.collisions = collisions.size
            for (collision in collisions)
                gateway.save(collision)

            task.metrics.time = time_source.markNow() - start
            logger.info("completed data collection.")
        }
    }
}