package cz.sspuopava.searchengine.searchmanager.search.searchers

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.SearchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import libraries.Address
import libraries.Credentials
import libraries.Elastic
import libraries.Page
import org.json.JSONArray
import org.json.JSONObject

class Fulltext : Searcher {
    private val elastic = Elastic(Credentials("elastic", "testerino"), Address("localhost", 9200), "search")

    private suspend fun performFulltextSearch(
        requestedQuery: String,
        length: Int = 20,
        startFrom: Int = 0
    ): SearchResponse<Page.PageType>? {

        val builder = SearchRequest.of { s ->
            s.index(elastic.index)
            s.size(length)
            s.from(startFrom)
            s.query { query ->
                query.bool { bool ->
                    bool.must(listOf(Query.of { query ->
                        query.multiMatch { mm ->
                            mm.query(requestedQuery)
                            mm.fields(
                                "metadata.title^2",
                                "metadata.description",
                                "inferredData.backLinks.text^3",
                                "body.headings.h1",
                            )
                            mm.fuzziness("2")
                        }
                    }, Query.of { query ->
                        query.rankFeature { rf ->
                            rf.field("inferredData.ranks.smartRank")
                            rf.linear { it }
                        }
                    }))
                }
            }
        }
        return elastic.search(builder)
    }


    suspend fun search(query: String, pagination: Int = 0, length: Int = 16): SearcherResult =
        coroutineScope {
            val result = withContext(Dispatchers.IO) { performFulltextSearch(query, length, pagination * length) }
            val hits = result?.hits()?.hits()?.map { it.source() } ?: emptyList()

            return@coroutineScope SearcherResult(JSONArray().apply {
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
            })
        }
}
