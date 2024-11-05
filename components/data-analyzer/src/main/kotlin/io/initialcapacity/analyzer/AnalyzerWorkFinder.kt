package io.initialcapacity.analyzer

import io.initialcapacity.workflow.WorkFinder
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class AnalyzerWorkFinder(val collector_url: String) : WorkFinder<AnalyzerTask> {
    val client = HttpClient(CIO)
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val work_map = mapOf(
        "data-analyzer" to createWorkList()
    )

    fun checkStatus(): Boolean {
        var is_ready = true
        work_map.forEach { entry ->
            for (work in entry.value) {
                if (!work.complete)
                    is_ready = false
            }
        }
        return is_ready
    }

    fun getMetrics(): List<AnalyzerMetrics> {
        val list = mutableListOf<AnalyzerMetrics>()
        work_map.forEach { entry ->
            for (work in entry.value) {
                list.add(work.metrics)
            }
        }
        return list
    }

    fun createWorkList(): List<AnalyzerTask> {
        var list = mutableListOf<AnalyzerTask>()
        list.add(AnalyzerTask("0.0001"))
        return list
    }

    override fun findRequested(name: String): List<AnalyzerTask> {
        logger.info("finding work")

        var status = false
        // Send GET request
        runBlocking {
            try {
                // Send GET request
                val response: HttpResponse = client.get("$collector_url/health-check")
                // Check if response contains the text
                val body: String = response.bodyAsText()
                status = body.contains("Ready")
            } catch (e: Exception) {
                println("Failed to retrieve the page: ${e.message}")
            }
        }


        var list = mutableListOf<AnalyzerTask>()

        if (!status) {
            logger.info("Data is not ready")
            return list
        }

        val work = work_map[name]
        if (work != null)
            for (item in work) {
                if (!item.complete && !item.in_process)
                    list.add(item)
            }
        else
            logger.info("$name work is already done.")

        return list
    }

    override fun markCompleted(info: AnalyzerTask) {
        logger.info("marking work complete.")
        info.complete = true
    }
}