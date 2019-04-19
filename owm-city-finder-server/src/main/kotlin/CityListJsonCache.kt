package com.gitlab.mvysny.owmcityfinder.server

import com.gitlab.mvysny.owmcityfinder.client.City
import com.gitlab.mvysny.owmcityfinder.client.OkHttp
import com.google.gson.stream.JsonReader
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL

object CityListJsonCache {
    private val log = LoggerFactory.getLogger(CityListJsonCache::class.java)

    private val dir = File(cacheDir, "citylist").apply { mkdirs2() }
    val cityListJsonGz = File(dir, "citylist.json.gz")
    private val cityListJsonUrl = "https://bulk.openweathermap.org/sample/city.list.json.gz"

    fun initCache() {
        if (!cityListJsonGz.exists()) {
            log.info("$cityListJsonGz does not exist, downloading from $cityListJsonUrl")
            URL(cityListJsonUrl).downloadTo(cityListJsonGz)
            log.info("$cityListJsonGz downloaded, ${cityListJsonGz.length()} bytes")
        }
    }

    fun forEachCity(block: (city: City)->Unit) {
        cityListJsonGz.inputStream().use { fileStream ->
            val reader = JsonReader(fileStream.buffered().gunzip().reader().buffered())
            reader.beginArray()
            while (reader.hasNext()) {
                val city: City = OkHttp.gson.fromJson<City>(reader, City::class.java)
                block(city)
            }
            reader.endArray()
        }
    }
}