package com.gitlab.mvysny.owmcityfinder.server

import org.slf4j.LoggerFactory

class Main {
    companion object {
        private val log = LoggerFactory.getLogger(Main::class.java)
        fun main() {
            log.info("OpenWeatherMap City Finder Server starting")
            if (!CityDatabase.exists()) {
                log.info("City database doesn't exist, rebuilding")
                CityListJsonCache.initCache()
                CityDatabase.index()
            }
        }
    }
}

fun main() {
    Main.main()
}
