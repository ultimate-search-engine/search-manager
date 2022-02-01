package cz.sspuopava.searchengine.searchmanager.search.searchers

class Hints : Searcher {
    suspend fun search(query: String) = SearcherResult(null)
}
