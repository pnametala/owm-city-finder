package com.gitlab.mvysny.owmcityfinder.client

import com.google.gson.Gson
import okhttp3.OkHttpClient

data class Coord(var lon: Double, var lat: Double)
data class City(var id: Long, var name: String, var country: String, var coord: Coord) {
    fun toJson(): String = gson.toJson(this)
    companion object {
        /**
         * Use this [Gson] instance to properly read/write [City] from/to json.
         */
        val gson = Gson()
    }
}

/**
 * A convenient client to access the City Finder server.
 * @param baseUrl the base URL where the server is running, e.g. `http://localhost:25314`
 */
class CityFinderClient(val baseUrl: String, val client: OkHttpClient = OkHttp.client) {
    private val gson = City.gson
    init {
        require(!baseUrl.endsWith("/")) { "$baseUrl must not end with a slash" }
    }

    /**
     * Retrieves a city by its ID.
     * @throws FileNotFoundException if no such city exists.
     */
    fun getById(id: Long): City {
        val request = "$baseUrl/city/$id".buildUrl().buildRequest()
        return client.exec(request) { response -> response.json(City::class.java, gson) }
    }

    /**
     * Finds a list of cities by name. Performs partial starts-with matches; handles diacritics and national characters
     * properly.
     * @param query anything that the user inputted into the search field. Returns empty list if blank.
     */
    fun findByName(query: String): List<City> {
        if (query.isBlank()) return listOf()
        val request = "$baseUrl/city".buildUrl {
            setEncodedQueryParameter("query", query)
        }.buildRequest()
        return client.exec(request) { response -> response.jsonArray(City::class.java, gson) }
    }
}
