package com.gitlab.mvysny.owmcityfinder.server

import org.slf4j.LoggerFactory

object Main {
    internal val log = LoggerFactory.getLogger(Main::class.java)
}
private val log = Main.log

fun main() {
    log.info("OpenWeatherMap City Finder Server starting")
    if (!CityDatabase.exists()) {
        log.info("City database doesn't exist, rebuilding")
        CityListJsonCache.initCache()
        CityDatabase.index()
    }
}
