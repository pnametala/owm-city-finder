package com.gitlab.mvysny.owmcityfinder.server

import com.gitlab.mvysny.owmcityfinder.client.City
import com.gitlab.mvysny.owmcityfinder.client.OkHttp
import io.javalin.Javalin
import io.javalin.NotFoundResponse
import java.io.Closeable

class RestServer(val port: Int, val db: CityDatabaseConnection): Closeable {
    private val javalin = Javalin.create().apply {
        get("/city/:id") { ctx ->
            val city: City = db.findById(ctx.pathParam<Long>("id").get()) ?: throw NotFoundResponse()
            ctx.result(city.toJson())
        }
        get("/city") { ctx ->
            val query = ctx.queryParam<String>("query").get()
            val cities: List<City> = db.findByName(query, 100)
            ctx.result(OkHttp.gson.toJson(cities))
        }
        start(port)
    }

    override fun close() {
        javalin.stop()
    }
}
