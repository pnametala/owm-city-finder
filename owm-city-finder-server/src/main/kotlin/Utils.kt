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

/**
 * Downloads the stream into given [file]. Overwrites the file silently; fails if the file exists and is a directory.
 * Deletes the file on unsuccessful download. Does not close the input stream.
 */
fun InputStream.downloadTo(file: File) {
    withCleanupOnError(file) {
        file.outputStream().use { fileStream -> copyTo(fileStream) }
    }
}

/**
 * Runs given [block]; deletes given [file] (may be a directory) if the block throws an exception.
 *
 * Intended to be used with downloaders or indexers or anything producing files as it computes - this function will
 * clean up the partially written file if the block fails.
 */
fun <R> withCleanupOnError(file: File, block: ()->R): R {
    try {
        return block()
    } catch (t: Throwable) {
        try {
            file.rmrf()
        } catch (t2: Throwable) {
            t.addSuppressed(t2)
        }
        throw t
    }
}

/**
 * Downloads the contents of given URL into given [file]. Overwrites the file silently; fails if the file exists and is a directory.
 * Deletes the file on unsuccessful download.
 */
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

/**
 * Computes the size of the file; traverses the directory recursively and sums sizes of all files.
 */
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

/**
 * Creates a stream which gunzips this stream ([GZIPInputStream]).
 */
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
