package com.gitlab.mvysny.owmcityfinder.server

import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.nio.file.Files

object CityListJsonCache {
    private val log = LoggerFactory.getLogger(CityListJsonCache::class.java)

    private val cacheDir = File(System.getProperty("user.home"), ".temp/own-city-finder")

    init {
        Files.createDirectories(cacheDir.toPath())
    }

    val cityListJsonGz = File(cacheDir, "citylist.json.gz")
    private val cityListJsonUrl = "https://bulk.openweathermap.org/sample/city.list.json.gz"

    fun initCache() {
        if (!cityListJsonGz.exists()) {
            log.info("$cityListJsonGz does not exist, downloading from $cityListJsonUrl")
            URL(cityListJsonUrl).downloadTo(cityListJsonGz)
            log.info("$cityListJsonGz downloaded, ${cityListJsonGz.length()} bytes")
        }
    }
}

class Main {
    companion object {
        private val log = LoggerFactory.getLogger(Main::class.java)
        fun main() {
            log.info("OpenWeatherMap City Finder Server starting")
            CityListJsonCache.initCache()
        }
    }
}

fun main() {
    Main.main()
}
