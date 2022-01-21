package cz.sspuopava.searchengine.searchmanager.search.searchers

interface Searcher {
    fun search(query: String): SearcherResult
}
