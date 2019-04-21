package com.gitlab.mvysny.owmcityfinder.client

import com.github.mvysny.dynatest.DynaTest
import com.google.gson.Gson
import kotlin.test.expect

class CityTest : DynaTest({
    test("json deserialization") {
        val json = """{
              "id": 707860,
              "name": "Hurzuf",
              "country": "UA",
              "coord": {
                "lon": 34.283333,
                "lat": 44.549999
              }
            }
        """.trimIndent()
        val city: City = Gson().fromJson(json, City::class.java)
        expect(City(707860L, "Hurzuf", "UA", Coord(34.283333, 44.549999))) { city }
    }

    test("to json") {
        expect("""{"id":707860,"name":"Hurzuf","country":"UA","coord":{"lon":34.283333,"lat":44.549999}}""") {
            City(707860L, "Hurzuf", "UA", Coord(34.283333, 44.549999)).toJson()
        }
    }
})