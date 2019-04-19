package com.gitlab.mvysny.owmcityfinder.server

import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.io.IOException
import java.nio.file.Path
import java.nio.file.FileVisitResult
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.SimpleFileVisitor
import java.util.zip.GZIPInputStream

fun InputStream.downloadTo(file: File) {
    try {
        file.outputStream().use { fileStream -> copyTo(fileStream) }
    } catch (t: Throwable) {
        try {
            Files.deleteIfExists(file.toPath())
        } catch (t2: Throwable) {
            t.addSuppressed(t2)
        }
        throw t
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

val userHome: File = File(System.getProperty("user.home"))
fun File.mkdirs2() {
    Files.createDirectories(toPath())
}

fun File.rmrf() {
    if (!exists()) return
    Files.walkFileTree(toPath(), object : SimpleFileVisitor<Path>() {

        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            Files.delete(file)
            return FileVisitResult.CONTINUE
        }

        override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
            if (exc == null) {
                Files.delete(dir)
                return FileVisitResult.CONTINUE
            } else {
                throw exc
            }
        }
    })
}

fun File.size(): Long {
    var size = 0L
    Files.walkFileTree(toPath(), object : SimpleFileVisitor<Path>() {

        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            size += Files.size(file)
            return FileVisitResult.CONTINUE
        }

        override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
            if (exc == null) {
                size += Files.size(dir)
                return FileVisitResult.CONTINUE
            } else {
                throw exc
            }
        }
    })
    return size
}

fun InputStream.gunzip(): InputStream = GZIPInputStream(this)

val cacheDir: File = File(userHome, ".temp/owm-city-finder").apply { mkdirs2() }

fun <T: Closeable, R> T.andTry(block: (T)->R): R {
    try {
        return block(this)
    } catch (t: Throwable) {
        try {
            close()
        } catch (t2: Throwable) {
            t.addSuppressed(t2)
        }
        throw t
    }
}
