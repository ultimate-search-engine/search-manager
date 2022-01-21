package cz.sspuopava.searchengine.searchmanager

import cz.sspuopava.searchengine.searchmanager.server.Server

fun main() = try {
    val server = Server(8080)
    server.run()
    Unit
} catch (e: Exception) {
    e.printStackTrace()
}
