package io.initialcapacity.collector

import io.initialcapacity.workflow.WorkFinder
import org.slf4j.LoggerFactory

class CollectorWorkFinder : WorkFinder<CollectorTask> {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val work_map = mapOf(
        "data-collector" to createWorkList())

    fun createWorkList() : List<CollectorTask> {
        var list = mutableListOf<CollectorTask>()
        // Replace 2 with 51 to see all states. Be warned it takes a while. Or just go here!
        // https://basic-server-320300059816.us-central1.run.app
        for (state in 1..2) {
            for (start_year in 2010..2022 step 2) {
                val end_year = start_year + 1
                list.add(CollectorTask(
                    "/FARSData/GetFARSData?dataset=Accident&FromYear=$start_year&ToYear=$end_year&state=$state&format=json",
                    false,
                    CollectorMetrics(start_year.toString(), end_year.toString(), state.toString()))
                )
            }
        }
        return list
    }

    fun checkStatus() : Boolean {
        var is_ready = true
        work_map.forEach { entry ->
            for (work in entry.value) {
                if (!work.complete)
                    is_ready = false
            }
        }
        return is_ready
    }

    fun getMetrics(): List<CollectorMetrics> {
        val list = mutableListOf<CollectorMetrics>()
        work_map.forEach { entry ->
            for (work in entry.value) {
                list.add(work.metrics)
            }
        }
        return list
    }

    override fun findRequested(name: String): List<CollectorTask> {
        logger.info("finding work.")

        var list = mutableListOf<CollectorTask>()
        val work = work_map[name]
        if (work != null)
            for (item in work) {
                if (!item.complete)
                    list.add(item)
            }
        else
            logger.info("$name work is already done.")

        return list
    }

    override fun markCompleted(info: CollectorTask) {
        logger.info("marking work complete.")
        info.complete = true
    }
}