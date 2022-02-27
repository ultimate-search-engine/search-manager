package cz.sspuopava.searchengine.searchmanager.search

import cz.sspuopava.searchengine.searchmanager.search.searchers.Fulltext
import cz.sspuopava.searchengine.searchmanager.search.searchers.SearcherResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.apache.log4j.Logger
import org.json.JSONArray
import org.json.JSONObject
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class Orchestrator {
    private val fulltext = Fulltext()
//    private val hints = Hints()
    private val logger = Logger.getLogger(this.javaClass)

    @OptIn(ExperimentalTime::class)
    private suspend fun performSearch(query: String, pagination: Int, length: Int): JSONObject =
        coroutineScope {
            val fulltextResultsAsync = async { fulltext.search(query, pagination, length) }
//        val hintsResultsAsync = async { hints... }

            val (fulltextResults: SearcherResult, duration: Duration) = measureTimedValue {
                fulltextResultsAsync.await()
            }

            return@coroutineScope JSONObject().apply {
                put("fulltext", JSONObject().apply {
                    put("results", JSONArray(fulltextResults.results))
                    put("duration", duration.inWholeMilliseconds)
                })
            }
        }

    suspend fun search(query: String, pagination: Int, length: Int): JSONObject {
        logger.debug("Received query: $query")
        return performSearch(query, pagination, length)
    }
}
