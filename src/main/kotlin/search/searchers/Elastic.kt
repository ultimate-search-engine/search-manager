package cz.sspuopava.searchengine.searchmanager.search.searchers

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient

class Elastic {
    val client: ElasticsearchClient

    init {
        val credentialsProvider: CredentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(
            AuthScope.ANY, UsernamePasswordCredentials("elastic", "testerino")
        )

        val restClient = RestClient.builder(
            HttpHost("localhost", 9200)
        ).setHttpClientConfigCallback { httpClientBuilder ->
            httpClientBuilder.disableAuthCaching()
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
        }.build()

        val transport: ElasticsearchTransport = RestClientTransport(
            restClient, JacksonJsonpMapper()
        )
        client = ElasticsearchClient(transport)
    }
}