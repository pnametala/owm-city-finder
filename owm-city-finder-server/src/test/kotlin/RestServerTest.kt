package com.gitlab.mvysny.owmcityfinder.server

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectList
import com.github.mvysny.dynatest.expectThrows
import com.gitlab.mvysny.owmcityfinder.client.City
import com.gitlab.mvysny.owmcityfinder.client.CityFinderClient
import com.gitlab.mvysny.owmcityfinder.client.Coord
import java.io.FileNotFoundException
import java.io.IOException
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

    group("GET city/id") {
        test("simple valid test") {
            expect(City(707860L, "Hurzuf", "UA", Coord(34.283333, 44.549999))) {
                client.getById(707860)
            }
        }
    }


    group("GET /city by name") {
        test("simple one-word search") {
            expectList(City(707860L, "Hurzuf", "UA", Coord(34.283333, 44.549999))) {
                client.findByName("Hurzuf")
            }
        }
        test("partial one-word search") {
            expectList(City(707860L, "Hurzuf", "UA", Coord(34.283333, 44.549999))) {
                client.findByName("Hurz")
            }
        }
        test("invalid char search returns no cities") {
            expectList() { client.findByName("*") }
        }
        test("multiple word search") {
            expectList(City(1269750L, "Republic of India", "IN", Coord(77.0, 20.0))) {
                client.findByName("Republic of India")
            }
        }
        test("separator search") {
            expectList("Zavety Il’icha", "Zavety Il’icha") { client.findByName("Zavety Il’icha").map { it.name } }
            expectList("Zavety Il’icha", "Zavety Il’icha") { client.findByName("Zavety Il icha").map { it.name } }
            expectList("Zavety Il’icha", "Zavety Il’icha") { client.findByName("Zavety Il'icha").map { it.name } }
        }
        test("diacritic search") {
            expectList("Bāgmatī Zone") { client.findByName("Bāgmatī Zone").map { it.name } }
            expectList("Bāgmatī Zone") { client.findByName("Bagmati Zone").map { it.name } }
        }
        test("ae") {
            expectList("Kværndrup") { client.findByName("Kværndrup").map { it.name }}
            expectList("Kværndrup") { client.findByName("Kvaerndrup").map { it.name }}
        }
        test("Å") {
            expectList("Arslev", "Årslev") { client.findByName("Årslev").map { it.name }}
            expectList("Arslev", "Årslev") { client.findByName("arslev").map { it.name }}
        }
        test("Consolação") {
            expectList("Consolação") { client.findByName("Consolação").map { it.name }}
            expectList("Consolação") { client.findByName("consolacao").map { it.name }}
        }
        test("country search") {
            expectList("Helsinki", "Helsinki", "Helsinki") { client.findByName("Helsinki,FI").map { it.name }}
            println(client.findByName("Helsinki,FI"))
        }
    }
})
