package io.initialcapacity.collector

import io.initialcapacity.workflow.WorkFinder
import org.slf4j.LoggerFactory

class CollectorWorkFinder : WorkFinder<CollectorTask> {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun findRequested(name: String): List<CollectorTask> {
        logger.info("finding work.")

        val work = CollectorTask("some info")

        return mutableListOf(work)
    }

    override fun markCompleted(info: CollectorTask) {
        logger.info("marking work complete.")
    }
}