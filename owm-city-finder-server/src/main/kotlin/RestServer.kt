package com.gitlab.mvysny.owmcityfinder.server

import io.javalin.Javalin
import io.javalin.NotFoundResponse
import java.io.Closeable

class RestServer(val port: Int, val db: CityDatabaseConnection): Closeable {
    private val javalin = Javalin.create().apply {
        get("/city/:id") { ctx ->
            val city = db.findById(ctx.pathParam<Long>("id").get()) ?: throw NotFoundResponse()
            ctx.result(city.toJson())
        }
        start(port)
    }

    override fun close() {
        javalin.stop()
    }
}
