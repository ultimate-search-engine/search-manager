package cz.sspuopava.searchengine.searchmanager.search.searchers

import org.json.JSONArray
import org.json.JSONObject

class Fulltext : Searcher {

    override fun search(query: String) = SearcherResult(
        JSONArray().apply {
            val seznam = JSONObject()
                .put("title", "Seznam.cz")
                .put("url", "https://www.seznam.cz")
                .put("desc", "Placeholder")
            repeat(10) {
                put(seznam)
            }
        }
    )

}