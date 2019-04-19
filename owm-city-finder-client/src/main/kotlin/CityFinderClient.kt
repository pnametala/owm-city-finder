package com.gitlab.mvysny.owmcityfinder.client

import okhttp3.OkHttpClient

data class Coord(var lon: Double, var lat: Double)
data class City(var id: Long, var name: String, var country: String, var coord: Coord)

/**
 * A convenient client to access the City Finder server.
 * @param baseUrl the base URL where the server is running, e.g. `http://localhost:25314`
 */
class CityFinderClient(val baseUrl: String, val client: OkHttpClient = OkHttp.client) {
    init {
        require(!baseUrl.endsWith("/")) { "$baseUrl must not end with a slash" }
    }

    /**
     * Retrieves a city by its ID.
     * @throws FileNotFoundException if no such city exists.
     */
    fun getById(id: Long): City {
        val request = "$baseUrl/city/$id".buildUrl().buildRequest()
        return client.exec(request) { response -> response.json(City::class.java) }
    }
}
