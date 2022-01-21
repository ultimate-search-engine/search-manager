package cz.sspuopava.searchengine.searchmanager.search.searchers

import org.json.JSONArray

data class SearcherResult(val results: JSONArray?) {

    val hasResults
        get() = results != null

}
