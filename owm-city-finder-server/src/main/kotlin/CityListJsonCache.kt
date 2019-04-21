package com.gitlab.mvysny.owmcityfinder.server

import com.gitlab.mvysny.owmcityfinder.client.City
import com.google.gson.stream.JsonReader
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL

/**
 * Caches city list downloaded from the OpenWeatherMap site. Not thread-safe.
 */
object CityListJsonCache {
    private val log = LoggerFactory.getLogger(CityListJsonCache::class.java)

    private val dir = File(cacheDir, "citylist").apply { mkdirs2() }
    private val cityListJsonGz = File(dir, "citylist.json.gz")
    private val cityListJsonUrl = "https://bulk.openweathermap.org/sample/city.list.json.gz"

    /**
     * Initializes cache - downloads the `citylist.json.gz` if it's not cached on the filesystem. Does nothing
     * if the file is already cached.
     */
    fun initCache() {
        if (!cityListJsonGz.exists()) {
            log.info("$cityListJsonGz does not exist, downloading from $cityListJsonUrl")
            URL(cityListJsonUrl).downloadTo(cityListJsonGz)
            log.info("$cityListJsonGz downloaded, ${cityListJsonGz.length()} bytes")
        }
    }

    /**
     * Reads the `citylist.json.gz` and parses out all cities. Invokes [block] with every city encountered.
     */
    fun forEachCity(block: (city: City)->Unit) {
        cityListJsonGz.inputStream().use { fileStream ->
            val reader = JsonReader(fileStream.buffered().gunzip().reader().buffered())
            reader.beginArray()
            while (reader.hasNext()) {
                val city: City = City.gson.fromJson<City>(reader, City::class.java)
                block(city)
            }
            reader.endArray()
        }
    }
}