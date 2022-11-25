package cz.sspuopava.searchengine.searchmanager

import cz.sspuopava.searchengine.searchmanager.server.Server

fun main() = try {
    val port = (System.getenv("SM_PORT") ?: "").ifEmpty { "8080" }.toInt()
    val server = Server(port)
    println("listening on port $port")
    server.run()
    Unit
} catch (e: Exception) {
    e.printStackTrace()
    Unit
}
