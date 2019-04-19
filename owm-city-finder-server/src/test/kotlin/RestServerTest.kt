package com.gitlab.mvysny.owmcityfinder.server

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.gitlab.mvysny.owmcityfinder.client.City
import com.gitlab.mvysny.owmcityfinder.client.CityFinderClient
import com.gitlab.mvysny.owmcityfinder.client.Coord
import java.io.FileNotFoundException
import kotlin.test.expect

class RestServerTest : DynaTest({
    usingApp()

    var db: CityDatabaseConnection? = null
    var s: RestServer? = null
    beforeGroup { db = CityDatabase.open(); s = RestServer(44444, db!!) }
    afterGroup { s?.closeQuietly(); db?.closeQuietly() }

    lateinit var client: CityFinderClient
    beforeEach { client = CityFinderClient("http://localhost:44444") }

    test("not found") {
        expectThrows(FileNotFoundException::class, "404: Not found (http://localhost:44444/city/1)") {
            client.getById(1)
        }
    }

    test("city retrieved") {
        expect(City(707860L, "Hurzuf", "UA", Coord(34.283333, 44.549999))) {
            client.getById(707860)
        }
    }
})