package cz.sspuopava.searchengine.searchmanager.search.searchers

import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.SearchResponse
import cz.sspuopava.searchengine.searchmanager.types.Page
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.json.JSONArray
import org.json.JSONObject

class Fulltext : Searcher {
    private val elastic = Elastic()

    private suspend fun performFulltextSearch(
        requestedQuery: String,
        index: String,
        startFrom: Int = 0,
        length: Int = 20
    ): SearchResponse<Page> = coroutineScope {
        async {
            elastic.client.search({ s: SearchRequest.Builder ->
                s.index(index).query { query ->
                    query.multiMatch {
                        it.query(requestedQuery)
                        it.fuzziness("2")
                        it.fields(
                            "metadata.title^10",
                            "body.headings.h1^10",
                            "metadata.description^8",
                            "body.headings.h2^8",
                            "body.headings.h3^6",
                            "body.headings.h4^4",
                            "body.article^2",
                            "body.plaintext"
                        )
                    }
                }.from(startFrom).size(length)
            }, Page::class.java)
        }
    }.await()

    suspend fun search(query: String, index: String, pagination: Int = 0, length: Int = 16): SearcherResult =
        coroutineScope {
            val result = performFulltextSearch(query, index, pagination * length, length)
            val hits = result.hits()?.hits()?.map { it.source() } ?: emptyList()

            return@coroutineScope SearcherResult(
                JSONArray().apply {
                    hits.forEach {
                        put(JSONObject().apply {
                            put("url", it?.url)
                            put("title", it?.metadata?.title)
                            put("description", it?.metadata?.description)
                            // these might not be used
                            put("type", it?.metadata?.type)
                            put("openGraphImgURL", it?.metadata?.openGraphImgURL)
                        })
                    }
                }
            )
        }
}
