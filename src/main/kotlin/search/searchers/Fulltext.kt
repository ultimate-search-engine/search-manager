package cz.sspuopava.searchengine.searchmanager.search.searchers

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.SearchResponse
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import libraries.*
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import kotlin.math.pow

val esHost: String = (System.getenv("ES_HOST") ?: "").ifEmpty { "localhost" }
val esUsername: String = (System.getenv("ES_USERNAME") ?: "").ifEmpty { "elastic" }
val esPassword: String = (System.getenv("ES_PASSWORD") ?: "").ifEmpty { "changeme" }
val esPort = (System.getenv("ES_PORT") ?: "").ifEmpty { "9200" }.toInt()
val esIndex: String = (System.getenv("ES_INDEX") ?: "").ifEmpty { "ency" }
val dbHost: String = (System.getenv("DB_HOST") ?: "").ifEmpty { "localhost" }
val dbPort: String = (System.getenv("DB_PORT") ?: "").ifEmpty { "27017" }
val dbUsername = (System.getenv("DB_USERNAME") ?: "").ifEmpty { "" }
val dbPassword = (System.getenv("DB_PASSWORD") ?: "").ifEmpty { "" }

class Fulltext : Searcher {
    private val elastic = Elastic(Credentials(esUsername, esPassword), Address(esHost, esPort), esIndex)

    private suspend fun performFulltextSearch(
        requestedQuery: String,
        length: Int = 20,
        startFrom: Int = 0
    ): SearchResponse<Page.Page> {

        val builder = SearchRequest.of { s ->
            s.index(esIndex)
            s.size(length)
            s.from(startFrom)
            s.query { query ->
                query.bool { bool ->
                    bool.must(listOf(
                        Query.of { query ->
                            query.multiMatch { mm ->
                                mm.query(requestedQuery)
                                mm.fields(
                                    "content.anchors^8",
                                    "content.boldText",
                                    "content.description",
                                    "content.headings.h1^3",
                                    "content.headings.h2",
                                    "content.headings.h3",
                                    "content.headings.h4",
                                    "content.headings.h5",
                                    "content.headings.h6",
                                    "content.text",
                                    "content.title^4"
                                )
                                mm.fuzziness("1")
                            }
                        }
                        ,
                        Query.of { query ->
                            query.rankFeature { rf ->
                                rf.field("ranks.smartRank.rankFeature")
                                rf.log { it.scalingFactor(2.0F) }
//                            rf.linear { it }
                                rf.boost(3.0F)
                            }
                        },
                        Query.of { query ->
                            query.rankFeature { rf ->
                                rf.field("ranks.urlSegmentsCount.negative")
//                            rf.linear { it }
                                rf.boost(2.0F)
                            }
                        },
                        Query.of { query ->
                            query.rankFeature { rf ->
                                rf.field("ranks.totalUrlDocs.positive")
//                            rf.linear { it }
                                rf.boost(1.0F)
                            }
                        }
                    )
                    )
                }
            }
        }
        return elastic.search(builder)
    }

//    private fun searchResponseBody(hits: List<Page.PageType>) {
//        return hits.map {
//
//        }
//    }

//    private fun orderByRank(hits: List<Page.PageType?>): List<Page.PageType> {
//
//    }

    suspend fun search(query: String, pagination: Int = 0, length: Int = 16): SearcherResult =
        coroutineScope {
            val result =
                withContext(Dispatchers.IO) { performFulltextSearch(query, length * 10, pagination * length) }
            val hits = result.hits()?.hits()?.map { it.source() }
                ?: emptyList()
            println(hits)

            return@coroutineScope SearcherResult(JSONArray().apply {
                hits.distinctBy { it?.url }.take(length).forEach { hit ->
                    if (hit != null) put(customProcessor(hit))
                }
            })
        }
}

val location =
    if (dbUsername.isNotEmpty()) "mongodb://$dbUsername:$dbPassword@$dbHost:$dbPort" else "mongodb://$dbHost:$dbPort"

suspend fun customProcessor(hit: Page.Page): Any {
    val hitHostUrl = Url(hit.url.url).host
    val collection = listOf(
        "britannica",
        "deletionpedia",
        "goodreads",
        "infoplease",
        "ncatlab",
        "scholarpedia",
        "wikipedia"
    ).first { hitHostUrl.contains(it) }

    val dbClient = PageRepository.MongoClient("ency", collectionName = collection, location = location)
    val repoPage = dbClient.find(hit.url.url)

    return JSONObject().apply {
        put("url", hit.url.url)
        if (repoPage != null) {
            try {
                println("here1")
                val jPage = Jsoup.parse(repoPage.content)
                println("here2")

                when (collection) {
                    "wikipedia" -> {
                        println("here3")
                        put("title", jPage.select("h1").first()?.text())
                        put("description", jPage.select("p")[1]?.text())
                        //put("image", jPage.)
                        println("here4")
                        put("subsections", jPage.select("h2").map { it?.text() })
                        put("type", "wikipedia")
                    }

                    "goodreads" -> {
                        put("title", jPage.select("h1").firstOrNull()?.text())
                        put("rating",
                            jPage.select("span").mapNotNull { if (it.attr("itemprop") == "ratingValue") it else null }
                                .firstOrNull()?.text()
                        )
                        put("description", jPage.select("#description").firstOrNull()?.text())
                        put("genres", jPage.getElementsByClass("actionLinkLite bookPageGenreLink").map { it.text() })
                        put("type", "goodreads")
                    }

                    else -> {
                        // other sites will have to do with this basic matching
                        put("title", hit.content.title.let { title ->
                            title.ifEmpty {
                                var longest = ""
                                hit.content.anchors.forEach { if (it.length > longest.length) longest = it }
                                longest
                            }
                        })
                        put("description", hit.content.description)
                        put("type", "default")
                    }
                }
            } catch (e: Exception) {
                put("title", hit.content.title.let { title ->
                    title.ifEmpty {
                        var longest = ""
                        hit.content.anchors.forEach { if (it.length > longest.length) longest = it }
                        longest
                    }
                })
                put("description", hit.content.description)
                put("type", "default")
            }
        }
    }
}
