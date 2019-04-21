package com.gitlab.mvysny.owmcityfinder.server

import com.gitlab.mvysny.owmcityfinder.client.City
import io.javalin.Javalin
import io.javalin.NotFoundResponse
import java.io.Closeable

/**
 * Starts an embedded web server and provides REST webservices:
 * * `/city/:id` - retrieves a JSON definition of a city with given ID
 * * `/city?query=Helsinki` - finds a city by its name; supports partial name matching
 *
 * Closing this object will stop the webserver.
 * @param port the port to listen on
 * @param db the database containing cities. Not closed.
 */
class RestServer(val port: Int, val db: CityDatabaseConnection): Closeable {
    private val javalin = Javalin.create().apply {
        get("/city/:id") { ctx ->
            val city: City = db.findById(ctx.pathParam<Long>("id").get()) ?: throw NotFoundResponse()
            ctx.result(city.toJson())
        }
        get("/city") { ctx ->
            val query = ctx.queryParam<String>("query").get()
            val cities: List<City> = db.findByName(query, 100)
            ctx.result(City.gson.toJson(cities))
        }
        start(port)
    }

    override fun close() {
        javalin.stop()
    }
}
