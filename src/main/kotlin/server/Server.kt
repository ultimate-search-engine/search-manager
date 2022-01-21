package cz.sspuopava.searchengine.searchmanager.server

import cz.sspuopava.searchengine.searchmanager.search.Orchestrator
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*
import org.json.JSONObject


class Server(port: Int) {

    private val orchestrator = Orchestrator()

    private suspend fun PipelineContext<Unit, ApplicationCall>.jsonResponse(json: JSONObject) =
        jsonResponse(json.toString())

    private suspend fun PipelineContext<Unit, ApplicationCall>.jsonResponse(json: String) =
        call.respondText(json, ContentType.Application.Json)

    private val server = embeddedServer(Netty, port) {
        routing {
            get("/search") {
                val query = context.parameters["q"] ?: ""
                jsonResponse(orchestrator.search(query))
            }
        }
    }

    fun run() = server.start(wait=true)

}
