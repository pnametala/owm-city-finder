package com.gitlab.mvysny.owmcityfinder.server

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import org.slf4j.LoggerFactory
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
