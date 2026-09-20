package com.markemcallister.huntingtons.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.FileNotFoundException
import java.io.InputStream

object ContentLoader {
    /** Plain JSON only. Do not ship a `.gz` asset: aapt2 decompresses `*.gz`
     *  and strips the suffix, which crashed Spelling Helper when the code
     *  opened the original `.gz` name. */
    const val ASSET_NAME = "baseline.json"

    fun openAsset(opener: (String) -> InputStream): InputStream {
        return try {
            opener(ASSET_NAME)
        } catch (error: Exception) {
            throw FileNotFoundException(
                "Missing catalog asset $ASSET_NAME (${error.message ?: error.javaClass.simpleName})",
            )
        }
    }

    fun load(input: InputStream): Catalog {
        val text = input.bufferedReader(Charsets.UTF_8).use { it.readText() }
        return parse(text)
    }

    fun parse(json: String): Catalog {
        val root = JSONObject(json)
        val catalog = Catalog(
            contentVersion = root.getString("contentVersion").trim(),
            generatedAt = root.getString("generatedAt").trim(),
            educationLastReviewed = root.getString("educationLastReviewed").trim(),
            overview = root.getJSONObject("overview").educationPage(),
            treatments = root.getJSONObject("treatments").educationPage(),
            research = root.getJSONObject("research").researchPage(),
            resources = root.getJSONObject("resources").resourcePage(),
            glossary = root.getJSONArray("glossary").glossary(),
            disclaimer = root.getJSONObject("disclaimer").disclaimer(),
            about = root.getJSONObject("about").about(),
        )
        require(catalog.contentVersion.isNotEmpty()) { "contentVersion is required" }
        require(catalog.generatedAt.isNotEmpty()) { "generatedAt is required" }
        require(catalog.overview.sections.isNotEmpty()) { "Overview needs sections" }
        require(catalog.treatments.sections.isNotEmpty()) { "Treatments needs sections" }
        require(catalog.treatments.callout.isNotEmpty()) { "Treatments needs the clinician callout" }
        require(catalog.research.howToRead.isNotEmpty()) { "Research needs how-to-read text" }
        require(catalog.research.pipelineThemes.isNotEmpty()) { "Research needs pipeline themes" }
        require(catalog.research.trialItems.isNotEmpty()) { "Research needs a starter trial snapshot" }
        require(catalog.resources.items.isNotEmpty()) { "Resources list is empty" }
        require(catalog.glossary.size >= 10) { "Glossary is too small" }
        require(catalog.disclaimer.fullText.isNotEmpty()) { "Disclaimer is required" }
        catalog.glossary.forEach { term ->
            require(term.term.isNotEmpty()) { "Glossary term is missing a name" }
            require(term.definition.isNotEmpty()) { "Glossary ${term.term} is missing a definition" }
        }
        return catalog
    }

    fun parseResearchCache(json: String): Pair<String, List<ResearchItem>> {
        val root = JSONObject(json)
        val fetchedAt = root.getString("fetchedAt").trim()
        val items = root.getJSONArray("trialItems").researchItems()
        require(fetchedAt.isNotEmpty()) { "Research cache is missing fetchedAt" }
        return fetchedAt to items
    }

    fun writeResearchCache(fetchedAt: String, items: List<ResearchItem>): String {
        val root = JSONObject()
        root.put("fetchedAt", fetchedAt)
        root.put("source", "ClinicalTrials.gov API v2")
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("title", item.title)
                    .put("summary", item.summary)
                    .put("source", item.source)
                    .put("sourceUrl", item.sourceUrl)
                    .put("date", item.date)
                    .put("kind", item.kind),
            )
        }
        root.put("trialItems", array)
        return root.toString()
    }

    private fun JSONObject.educationPage(): EducationPage {
        return EducationPage(
            title = getString("title").trim(),
            intro = optionalString("intro"),
            callout = optionalString("callout"),
            sections = getJSONArray("sections").mapObjects { obj ->
                EducationSection(
                    id = obj.getString("id").trim(),
                    title = obj.getString("title").trim(),
                    body = obj.getString("body").trim(),
                    glossaryTerms = obj.optJSONArray("glossaryTerms")?.stringList().orEmpty(),
                )
            },
            sources = optJSONArray("sources")?.mapObjects { obj ->
                NamedLink(
                    label = obj.getString("label").trim(),
                    url = obj.getString("url").trim(),
                )
            }.orEmpty(),
        )
    }

    private fun JSONObject.researchPage(): ResearchPage {
        return ResearchPage(
            title = getString("title").trim(),
            howToRead = getString("howToRead").trim(),
            askSpecialist = getString("askSpecialist").trim(),
            pipelineThemes = getJSONArray("pipelineThemes").mapObjects { obj ->
                EducationSection(
                    id = obj.optionalString("id").ifEmpty { obj.getString("title").trim() },
                    title = obj.getString("title").trim(),
                    body = obj.getString("body").trim(),
                    glossaryTerms = obj.optJSONArray("glossaryTerms")?.stringList().orEmpty(),
                )
            },
            clinicalTrialsPrimer = getString("clinicalTrialsPrimer").trim(),
            exploreTrialsLabel = getString("exploreTrialsLabel").trim(),
            exploreTrialsUrl = getString("exploreTrialsUrl").trim(),
            notableDevelopments = getJSONArray("notableDevelopments").researchItems(),
            trialItems = getJSONArray("trialItems").researchItems(),
        )
    }

    private fun JSONObject.resourcePage(): ResourcePage {
        return ResourcePage(
            intro = getString("intro").trim(),
            items = getJSONArray("items").mapObjects { obj ->
                ResourceItem(
                    title = obj.getString("title").trim(),
                    description = obj.getString("description").trim(),
                    url = obj.getString("url").trim(),
                    category = obj.optionalString("category"),
                )
            },
        )
    }

    private fun JSONArray.glossary(): List<GlossaryTerm> {
        return mapObjects { obj ->
            GlossaryTerm(
                term = obj.getString("term").trim(),
                definition = obj.getString("definition").trim(),
            )
        }.sortedBy { it.term.lowercase() }
    }

    private fun JSONObject.disclaimer(): Disclaimer {
        return Disclaimer(
            shortText = getString("shortText").trim(),
            fullText = getString("fullText").trim(),
        )
    }

    private fun JSONObject.about(): About {
        return About(
            purpose = getString("purpose").trim(),
            sourcing = getString("sourcing").trim(),
            refresh = getString("refresh").trim(),
            version = getString("version").trim(),
        )
    }

    private fun JSONArray.researchItems(): List<ResearchItem> {
        return mapObjects { obj ->
            ResearchItem(
                id = obj.getString("id").trim(),
                title = obj.getString("title").trim(),
                summary = obj.getString("summary").trim(),
                source = obj.getString("source").trim(),
                sourceUrl = obj.getString("sourceUrl").trim(),
                date = obj.getString("date").trim(),
                kind = obj.optionalString("kind").ifEmpty { "clinical_trial" },
            )
        }
    }

    private fun JSONObject.optionalString(key: String): String {
        if (!has(key) || isNull(key)) return ""
        return optString(key, "").trim()
    }

    private fun JSONArray.stringList(): List<String> {
        return (0 until length()).map { index ->
            getString(index).trim().also { value ->
                require(value.isNotEmpty()) { "Empty string in list at index $index" }
            }
        }
    }

    private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> {
        return (0 until length()).map { index -> transform(getJSONObject(index)) }
    }
}
