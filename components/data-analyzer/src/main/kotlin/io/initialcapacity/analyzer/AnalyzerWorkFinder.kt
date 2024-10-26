package io.initialcapacity.analyzer

import io.initialcapacity.workflow.WorkFinder
import org.slf4j.LoggerFactory

class AnalyzerWorkFinder : WorkFinder<AnalyzerTask> {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun findRequested(name: String): List<AnalyzerTask> {
        logger.info("finding work.")

        val work = AnalyzerTask("0.0001")

        return mutableListOf(work)
    }

    override fun markCompleted(info: AnalyzerTask) {
        logger.info("marking work complete.")
    }
}