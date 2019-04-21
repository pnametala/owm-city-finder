package com.gitlab.mvysny.owmcityfinder.client

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.Closeable
import java.io.FileNotFoundException
import java.io.IOException
import java.io.Reader

/**
 * Destroys the [OkHttpClient] including the dispatcher, connection pool, everything. WARNING: THIS MAY AFFECT
 * OTHER http clients if they share e.g. dispatcher executor service.
 */
fun OkHttpClient.destroy() {
    dispatcher().executorService().shutdown()
    connectionPool().evictAll()
    cache()?.close()
}

/**
 * Fails if the response is not in 200..299 range; otherwise returns [this].
 * @throws IOException if the response is not in 200..299 ([Response.isSuccessful] returns false). Uses [FileNotFoundException] for 404.
 */
fun Response.checkOk(): Response {
    if (!isSuccessful) {
        val msg = "${code()}: ${body()!!.string()} (${request().url()})"
        if (code() == 404) throw FileNotFoundException(msg)
        throw IOException(msg)
    }
    return this
}

object OkHttp : Closeable {
    override fun close() {
        client.destroy()
    }

    /**
     * All REST client calls will reuse this client. Automatically destroyed in [close].
     */
    var client: OkHttpClient = OkHttpClient()
}

/**
 * Parses the response as a JSON and converts it to a Java object with given [clazz] using [OkHttpClientVokPlugin.gson].
 */
fun <T> ResponseBody.json(clazz: Class<T>, gson: Gson): T = gson.fromJson(charStream(), clazz)

/**
 * Parses the response as a JSON array and converts it into a list of Java object with given [clazz] using [OkHttpClientVokPlugin.gson].
 */
fun <T> ResponseBody.jsonArray(clazz: Class<T>, gson: Gson): List<T> = gson.fromJsonArray(charStream(), clazz)

/**
 * Parses [json] as a list of items with class [itemClass] and returns that.
 */
fun <T> Gson.fromJsonArray(json: String, itemClass: Class<T>): List<T> {
    val type = TypeToken.getParameterized(List::class.java, itemClass).type
    return fromJson<List<T>>(json, type)
}

/**
 * Parses JSON from a [reader] as a list of items with class [itemClass] and returns that.
 */
fun <T> Gson.fromJsonArray(reader: Reader, itemClass: Class<T>): List<T> {
    val type = TypeToken.getParameterized(List::class.java, itemClass).type
    return fromJson<List<T>>(reader, type)
}

/**
 * Runs given [request] synchronously and then runs [responseBlock] with the response body. The [Response] is properly closed afterwards.
 * Only calls the block on success; uses [checkOk] to check for failure prior calling the block.
 * @param responseBlock run on success.
 */
fun <T> OkHttpClient.exec(request: Request, responseBlock: (ResponseBody) -> T): T =
        newCall(request).execute().use {
            responseBlock(it.checkOk().body()!!)
        }

/**
 * Parses the response as a JSON map and converts it into a map of objects with given [valueClass] using [OkHttpClientVokPlugin.gson].
 */
fun <V> ResponseBody.jsonMap(valueClass: Class<V>, gson: Gson): Map<String, V> = gson.fromJsonMap(charStream(), valueClass)

/**
 * Parses [json] as a map of items with class [valueClass] and returns that.
 */
fun <T> Gson.fromJsonMap(reader: Reader, valueClass: Class<T>): Map<String, T> {
    val type = TypeToken.getParameterized(Map::class.java, String::class.java, valueClass).type
    return fromJson<Map<String, T>>(reader, type)
}

/**
 * Parses this string as a `http://` or `https://` URL. You can configure the URL (e.g. add further query parameters) in [block].
 * @throws IllegalArgumentException if the URL is unparseable
 */
inline fun String.buildUrl(block: HttpUrl.Builder.()->Unit = {}): HttpUrl = HttpUrl.get(this).newBuilder().apply {
    block()
}.build()

/**
 * Builds a new OkHttp [Request] using given URL. You can optionally configure the request in [block]. Use [exec] to
 * execute the request with given OkHttp client and obtain a response. By default the `GET` request gets built.
 */
inline fun HttpUrl.buildRequest(block: Request.Builder.()->Unit = {}): Request = Request.Builder().url(this).apply {
    block()
}.build()
