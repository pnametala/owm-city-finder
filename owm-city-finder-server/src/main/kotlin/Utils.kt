package com.gitlab.mvysny.owmcityfinder.server

import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Files

fun InputStream.downloadTo(file: File) {
    try {
        file.outputStream().use { fileStream -> copyTo(fileStream) }
    } catch (t: Throwable) {
        try {
            Files.deleteIfExists(file.toPath())
        } catch (t2: Throwable) {
            t.addSuppressed(t2)
        }
    }
}

fun URL.downloadTo(file: File) {
    openStream().use { urlStream -> urlStream.downloadTo(file) }
}

/**
 * Closes [this] quietly - if [Closeable.close] fails, an INFO message is logged. The exception is not
 * rethrown.
 */
fun Closeable.closeQuietly() {
    try {
        close()
    } catch (e: Exception) {
        LoggerFactory.getLogger(javaClass).info("Failed to close $this", e)
    }
}
