package com.gitlab.mvysny.owmcityfinder.server

import org.slf4j.LoggerFactory

object Main {
    internal val log = LoggerFactory.getLogger(Main::class.java)
}
private val log = Main.log

fun main() {
    val port = 25314
    log.info("OpenWeatherMap City Finder Server starting")
    if (!CityDatabase.exists()) {
        log.info("City database doesn't exist, rebuilding")
        CityListJsonCache.initCache()
        CityDatabase.index()
    }
    CityDatabase.open().use { db ->
        log.info("Got database connection")
        RestServer(port, db).use { restServer ->
            log.info("City Finder running on port $port, press CTRL+C to stop")
            Runtime.getRuntime().addShutdownHook(Thread {
                try {
                    log.info("Shutting down")
                    restServer.closeQuietly()
                    db.closeQuietly()
                    log.info("Shut down cleanly")
                } catch (t: Throwable) {
                    log.error("Failed to shut down cleanly", t)
                }
            })
            while(true) {
                Thread.sleep(Long.MAX_VALUE)
            }
        }
    }
}
