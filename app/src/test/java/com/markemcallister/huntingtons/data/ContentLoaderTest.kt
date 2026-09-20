package com.markemcallister.huntingtons.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException

class ContentLoaderTest {
    private val catalog: Catalog by lazy {
        ContentLoader.parse(bundledJson())
    }

    @Test
    fun bundledCatalogHasRequiredEducationPages() {
        assertTrue(catalog.contentVersion.isNotBlank())
        assertTrue(catalog.generatedAt.isNotBlank())
        assertEquals("What is Huntington’s Disease", catalog.overview.title)
        assertEquals("Current Treatments", catalog.treatments.title)
        assertTrue(catalog.overview.sections.map { it.id }.containsAll(
            listOf("overview", "genetics", "symptoms", "how-common", "living"),
        ))
        assertTrue(catalog.treatments.sections.map { it.id }.containsAll(
            listOf("symptom", "team", "therapies", "counseling", "standard-vs-experimental"),
        ))
        assertTrue(catalog.treatments.callout.contains("ask your clinician", ignoreCase = true))
    }

    @Test
    fun treatmentsPageHasNoDoseTables() {
        val text = (listOf(catalog.treatments.intro, catalog.treatments.callout) +
            catalog.treatments.sections.map { it.body }).joinToString(" ").lowercase()
        assertTrue(!text.contains("mg/"))
        assertTrue(!text.contains(" milligram"))
        assertTrue(text.contains("does not list doses") || catalog.treatments.sections.any { it.body.contains("does not list doses") })
    }

    @Test
    fun researchPageIsConservative() {
        assertTrue(catalog.research.howToRead.contains("not the same as a treatment", ignoreCase = true))
        assertTrue(catalog.research.askSpecialist.contains("Ask your specialist", ignoreCase = true))
        assertTrue(catalog.research.pipelineThemes.size >= 3)
        assertTrue(catalog.research.trialItems.isNotEmpty())
        catalog.research.trialItems.forEach { item ->
            assertTrue(item.sourceUrl.startsWith("https://clinicaltrials.gov/"))
            assertTrue(item.summary.contains("not proof") || item.summary.contains("registration"))
            assertTrue(!item.title.contains("criminal", ignoreCase = true))
        }
    }

    @Test
    fun glossaryIsSearchableAZ() {
        assertTrue(catalog.glossary.size >= 15)
        val terms = catalog.glossary.map { it.term.lowercase() }
        assertEquals(terms, terms.sorted())
        assertTrue(terms.contains("chorea"))
        assertTrue(terms.contains("htt"))
        assertTrue(terms.contains("cag repeat"))
    }

    @Test
    fun resourcesAreExternalHttps() {
        assertTrue(catalog.resources.intro.contains("U.S.", ignoreCase = true))
        assertTrue(catalog.resources.items.size >= 8)
        catalog.resources.items.forEach { item ->
            assertTrue(item.url.startsWith("https://"))
        }
    }

    @Test
    fun disclaimerIsNotAdvice() {
        val text = catalog.disclaimer.fullText.lowercase()
        assertTrue(text.contains("not a clinical tool"))
        assertTrue(text.contains("911"))
        assertTrue(text.contains("does not diagnose"))
    }

    @Test
    fun missingAssetDoesNotCrashTheLoaderApi() {
        val error = try {
            ContentLoader.openAsset { throw FileNotFoundException("gone") }
            null
        } catch (thrown: FileNotFoundException) {
            thrown
        }
        assertTrue(error is FileNotFoundException)
        assertTrue(error!!.message!!.contains(ContentLoader.ASSET_NAME))
    }

    @Test
    fun loadReadsUtf8Stream() {
        val loaded = ContentLoader.load(ByteArrayInputStream(bundledJson().toByteArray(Charsets.UTF_8)))
        assertEquals(catalog.glossary.size, loaded.glossary.size)
    }

    @Test
    fun assetFileIsPlainJsonNotGzip() {
        val file = bundledFile()
        assertTrue(file.name.endsWith(".json"))
        assertTrue(!file.name.endsWith(".gz"))
        val header = file.inputStream().use { stream ->
            val bytes = ByteArray(2)
            val read = stream.read(bytes)
            read to bytes
        }
        val gzip = header.first >= 2 && header.second[0] == 0x1f.toByte() && header.second[1] == 0x8b.toByte()
        assertTrue("do not ship a gzip file as the asset", !gzip)
    }

    @Test
    fun researchCacheRoundTrip() {
        val items = catalog.research.trialItems
        val json = ContentLoader.writeResearchCache("2026-09-20T17:00:00Z", items)
        val parsed = ContentLoader.parseResearchCache(json)
        assertEquals("2026-09-20T17:00:00Z", parsed.first)
        assertEquals(items.size, parsed.second.size)
        assertEquals(items.first().id, parsed.second.first().id)
    }

    private fun bundledJson(): String = bundledFile().readText(Charsets.UTF_8)

    private fun bundledFile(): File {
        val candidates = listOf(
            File("../content/baseline.json"),
            File("content/baseline.json"),
            File("src/main/assets/baseline.json"),
            File("app/src/main/assets/baseline.json"),
        )
        return candidates.first { it.exists() }
    }
}
