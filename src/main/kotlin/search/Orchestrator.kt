package cz.sspuopava.searchengine.searchmanager.search

import cz.sspuopava.searchengine.searchmanager.search.searchers.Fulltext
import cz.sspuopava.searchengine.searchmanager.search.searchers.Hints
import org.apache.log4j.Logger
import org.json.JSONArray
import org.json.JSONObject

class Orchestrator {
    private val fulltext = Fulltext()
    private val hints = Hints()
    private val logger = Logger.getLogger(this.javaClass)

    private fun performSearch(query: String) = JSONObject()
        .put("fulltext", fulltext.search(query).results ?: JSONArray())

    fun search(query: String): JSONObject {
        logger.debug("Received query: $query")
        return performSearch(query)
    }
}