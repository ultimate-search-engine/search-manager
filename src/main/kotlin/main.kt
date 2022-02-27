package cz.sspuopava.searchengine.searchmanager

import cz.sspuopava.searchengine.searchmanager.server.Server

fun main() = try {
    val port = 8080
    val server = Server(port)
    println("listening on port $port")
    server.run()
    Unit
} catch (e: Exception) {
    e.printStackTrace()
}
