package com.gitlab.mvysny.owmcityfinder.server

import com.gitlab.mvysny.owmcityfinder.client.City
import com.gitlab.mvysny.owmcityfinder.client.OkHttp
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.File

object CityDatabase {
    private val log = LoggerFactory.getLogger(CityDatabase::class.java)
    private val luceneDir = File(cacheDir, "lucene")

    fun exists() = luceneDir.exists()

    fun index() {
        log.info("Rebuilding city database at $luceneDir")
        luceneDir.rmrf()
        luceneDir.mkdirs2()
        withLuceneWriter { indexWriter ->
            CityListJsonCache.forEachCity { city ->
                val doc = Document()
                doc.add(Field("name", city.name, Field.Store.NO, Field.Index.ANALYZED))
                doc.add(Field("id", city.id.toString(), Field.Store.NO, Field.Index.ANALYZED))
                doc.add(Field("json", city.toJson(), Field.Store.YES, Field.Index.NOT_ANALYZED))
                indexWriter.addDocument(doc)
            }
        }
        log.info("City database ready, uses ${luceneDir.size()} bytes")
    }

    fun open(): CityDatabaseConnection = FSDirectory.open(luceneDir).andTry { directory ->
        IndexReader.open(directory, true).andTry { reader ->
            IndexSearcher(reader).andTry { searcher ->
                CityDatabaseConnection(directory, reader, searcher)
            }
        }
    }

    private fun withLuceneWriter(block: (luceneWriter: IndexWriter) -> Unit) {
        FSDirectory.open(luceneDir).use { directory ->
            StandardAnalyzer(Version.LUCENE_30).use { analyzer ->
                IndexWriter(directory, IndexWriterConfig(Version.LUCENE_30, analyzer).setOpenMode(IndexWriterConfig.OpenMode.CREATE)).use { luceneWriter ->
                    luceneWriter.maxFieldLength = IndexWriter.MaxFieldLength.UNLIMITED.limit
                    block(luceneWriter)
                    log.info("Optimizing Lucene index")
                    luceneWriter.forceMerge(1, true)
                }
            }
        }
    }
}

class CityDatabaseConnection(private val directory: FSDirectory, private val indexReader: IndexReader, private val searcher: IndexSearcher) : Closeable {
    override fun close() {
        searcher.closeQuietly()
        indexReader.closeQuietly()
        directory.closeQuietly()
    }

    private fun Document.toCity() = OkHttp.gson.fromJson(get("json"), City::class.java)

    fun findById(id: Long): City? {
        val parser = QueryParser(Version.LUCENE_30, "id", StandardAnalyzer(Version.LUCENE_30))
        val docs: List<Document> = searcher.search(parser.parse(id.toString()), 1).scoreDocs.map { searcher.doc(it.doc) }
        val doc = docs.firstOrNull() ?: return null
        return doc.toCity()
    }

    fun findByName(query: String, maxResults: Int): List<City> {
        require(query.isNotBlank()) { "query is blank" }
        val modifiedQuery = query.replaceNonAlphanumericCharsWithSpace()
                .splitByWhitespaces()
                .map { "$it*" }
                .joinToString(" AND ")
        if (modifiedQuery.isBlank()) return listOf()
        val parser = QueryParser(Version.LUCENE_30, "name", StandardAnalyzer(Version.LUCENE_30))
        val docs: List<Document> = searcher.search(parser.parse(modifiedQuery), maxResults).scoreDocs.map { searcher.doc(it.doc) }
        return docs.map { it.toCity() }
    }
}

/**
 * Determines if the specified character (Unicode code point) is an alphabet.
 * <p>
 * A character is considered to be alphabetic if its general category type,
 * provided by {@link Character#getType(int) getType(codePoint)}, is any of
 * the following:
 * <ul>
 * <li> <code>UPPERCASE_LETTER</code>
 * <li> <code>LOWERCASE_LETTER</code>
 * <li> <code>TITLECASE_LETTER</code>
 * <li> <code>MODIFIER_LETTER</code>
 * <li> <code>OTHER_LETTER</code>
 * <li> <code>LETTER_NUMBER</code>
 * </ul>
 * or it has contributory property Other_Alphabetic as defined by the
 * Unicode Standard.
 *
 * @param   codePoint the character (Unicode code point) to be tested.
 * @return  <code>true</code> if the character is a Unicode alphabet
 *          character, <code>false</code> otherwise.
 * @since   1.7
 */
fun Int.isAlphabetic(): Boolean {
    // do not use Character.isAlphabetic(c) - only present since Android API 19
    val type = Character.getType(this)
    // triedy su zobrate z javadocu k Character.isAlphabetic
    return type == Character.UPPERCASE_LETTER.toInt() ||
            type == Character.LOWERCASE_LETTER.toInt() ||
            type == Character.TITLECASE_LETTER.toInt() ||
            type == Character.MODIFIER_LETTER.toInt() ||
            type == Character.OTHER_LETTER.toInt() ||
            type == Character.LETTER_NUMBER.toInt()
}

fun String.replaceNonAlphanumericCharsWithSpace(): String = replace { if (it.toInt().isAlphabetic() || Character.isDigit(it)) it else ' ' }

fun String.replace(block: (Char)->Char): String = buildString(length) {
    this@replace.forEach { append(block(it)) }
}

/**
 * https://youtrack.jetbrains.com/issue/KT-11669
 */
private val REGEX_WHITESPACES = "[\\p{javaWhitespace}\\p{javaSpaceChar}\u2000-\u200f]+".toRegex()

/**
 * Splits words by whitespaces. Removes all blank words. Splits also by the NBSP 160 char.
 */
fun CharSequence.splitByWhitespaces() = REGEX_WHITESPACES.split(this).filterNotBlank()

fun Iterable<String>.filterNotBlank(): List<String> = filter { it.isNotBlank() }
