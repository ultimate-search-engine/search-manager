package cz.sspuopava.searchengine.searchmanager.search.searchers

import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.SearchResponse
import cz.sspuopava.searchengine.searchmanager.types.PageType
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
    ): SearchResponse<PageType> = coroutineScope {
        async {
            elastic.client.search({ s: SearchRequest.Builder ->
                s.index(index).query { query ->
                    query.multiMatch {
                        it.query(requestedQuery)
                        it.fuzziness("3")
                        it.fields(
                            "inferredData.backLinks.text^2",
                            "address.urlAsText^5",
                            "metadata.title^4",
                            "body.headings.h1^3",
                            "body.headings.h2",
                            "body.links.internal.text",
                        )
                    }
                }.from(startFrom).size(length)
            }, PageType::class.java)
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
                            put("url", it?.address?.url)
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
