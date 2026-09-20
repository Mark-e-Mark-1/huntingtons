package com.markemcallister.huntingtons.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Reads Huntington disease studies from the public ClinicalTrials.gov Data API v2.
 * Docs: https://clinicaltrials.gov/data-api/api
 *
 * A study record is a registration, not evidence that a treatment works or is
 * available outside a trial. This client never invents results or approvals.
 */
object ClinicalTrialsClient {
    const val BASE_URL = "https://clinicaltrials.gov/api/v2/studies"
    const val EXPLORE_URL = "https://clinicaltrials.gov/search?cond=Huntington%20Disease"
    private const val USER_AGENT = "HuntingtonsEducationalApp/1.0 (educational Android companion)"
    private const val PAGE_SIZE = 25
    private const val MAX_ITEMS = 12

    fun fetchTrialItems(): List<ResearchItem> {
        val query = listOf(
            "query.cond" to "Huntington Disease",
            "filter.overallStatus" to "RECRUITING,NOT_YET_RECRUITING,ENROLLING_BY_INVITATION,ACTIVE_NOT_RECRUITING",
            "sort" to "LastUpdatePostDate:desc",
            "pageSize" to PAGE_SIZE.toString(),
            "countTotal" to "true",
            "fields" to listOf(
                "NCTId",
                "BriefTitle",
                "OfficialTitle",
                "OverallStatus",
                "Phase",
                "StartDate",
                "LastUpdatePostDate",
                "BriefSummary",
                "LeadSponsorName",
                "InterventionName",
                "Condition",
            ).joinToString(","),
        ).joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value)}"
        }
        val body = httpGet("$BASE_URL?$query")
        return parseStudies(body)
    }

    fun parseStudies(json: String): List<ResearchItem> {
        val root = JSONObject(json)
        val studies = root.optJSONArray("studies") ?: JSONArray()
        val items = (0 until studies.length()).mapNotNull { index ->
            toResearchItem(studies.getJSONObject(index))
        }
        return items
            .sortedByDescending { it.date }
            .take(MAX_ITEMS)
    }

    internal fun toResearchItem(study: JSONObject): ResearchItem? {
        val protocol = study.optJSONObject("protocolSection") ?: return null
        val ident = protocol.optJSONObject("identificationModule") ?: JSONObject()
        val status = protocol.optJSONObject("statusModule") ?: JSONObject()
        val description = protocol.optJSONObject("descriptionModule") ?: JSONObject()
        val design = protocol.optJSONObject("designModule") ?: JSONObject()
        val sponsorMod = protocol.optJSONObject("sponsorCollaboratorsModule") ?: JSONObject()
        val conditionsMod = protocol.optJSONObject("conditionsModule") ?: JSONObject()
        val arms = protocol.optJSONObject("armsInterventionsModule") ?: JSONObject()

        val nctId = ident.optString("nctId", "").trim()
        val title = ident.optString("briefTitle", "").trim()
        if (nctId.isEmpty() || title.isEmpty()) return null

        val conditions = conditionsMod.optJSONArray("conditions").stringList()
        val haystack = (conditions + title).joinToString(" ").lowercase()
        if (!haystack.contains("huntington")) return null
        if (isOffTopic(title, conditions)) return null

        val date = status.optJSONObject("lastUpdatePostDateStruct")
            ?.optString("date", "")
            ?.trim()
            .orEmpty()
            .ifEmpty { Instant.now().atOffset(ZoneOffset.UTC).toLocalDate().toString() }
        val overallStatus = humanStatus(status.optString("overallStatus", "").trim())
        val phases = design.optJSONArray("phases").stringList()
            .joinToString(", ") { humanPhase(it) }
        val sponsor = sponsorMod.optJSONObject("leadSponsor")
            ?.optString("name", "")
            ?.trim()
            .orEmpty()
        val interventions = arms.optJSONArray("interventions")
            ?.let { array ->
                (0 until array.length()).mapNotNull { i ->
                    array.optJSONObject(i)?.optString("name", "")?.trim()?.takeIf { it.isNotEmpty() }
                }
            }
            .orEmpty()
            .filterNot { it.equals("placebo", ignoreCase = true) || it.contains("placebo", ignoreCase = true) }
        val brief = description.optString("briefSummary", "").trim()

        val summary = buildSummary(
            brief = brief,
            overallStatus = overallStatus,
            phases = phases,
            sponsor = sponsor,
            interventions = interventions,
        )
        return ResearchItem(
            id = nctId,
            title = title,
            summary = summary,
            source = "ClinicalTrials.gov",
            sourceUrl = "https://clinicaltrials.gov/study/$nctId",
            date = date,
            kind = "clinical_trial",
        )
    }

    fun nowIso(): String {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS))
    }

    private fun isOffTopic(title: String, conditions: List<String>): Boolean {
        val t = title.lowercase()
        if (t.contains("chatbot") || t.contains("llm-based")) return true
        if (t.contains("scattered rare disease")) return true
        if (t.contains("criminal")) return true
        if (t.contains("non-invasive prenatal") && !t.contains("huntington")) return true
        val hdNamed = conditions.any { it.lowercase().contains("huntington") }
        return !hdNamed && !t.contains("huntington")
    }

    private fun buildSummary(
        brief: String,
        overallStatus: String,
        phases: String,
        sponsor: String,
        interventions: List<String>,
    ): String {
        val sentences = splitSentences(brief)
            .filter { it.isNotBlank() }
            .take(3)
        val parts = sentences.toMutableList()
        val meta = buildString {
            append("ClinicalTrials.gov lists this study as ")
            append(overallStatus.ifEmpty { "a registered record" })
            if (phases.isNotEmpty()) {
                append(" (")
                append(phases)
                append(")")
            }
            append(".")
            if (sponsor.isNotEmpty()) {
                append(" Lead sponsor: ")
                append(sponsor)
                append(".")
            }
            if (interventions.isNotEmpty()) {
                append(" Interventions named in the record: ")
                append(interventions.take(4).joinToString(", "))
                append(".")
            }
        }
        parts.add(meta)
        parts.add("A listing here is a public registration, not proof that the approach works, is safe for you, or is available outside a trial.")
        return parts.joinToString(" ").trim()
    }

    private fun splitSentences(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        val cleaned = text.replace("\n", " ").replace(Regex("\\s+"), " ").trim()
        val pieces = cleaned.split(Regex("(?<=[.!?])\\s+"))
        return if (pieces.isEmpty()) listOf(cleaned.take(400)) else pieces
    }

    private fun humanStatus(raw: String): String {
        return when (raw.uppercase()) {
            "RECRUITING" -> "Recruiting"
            "NOT_YET_RECRUITING" -> "Not yet recruiting"
            "ENROLLING_BY_INVITATION" -> "Enrolling by invitation"
            "ACTIVE_NOT_RECRUITING" -> "Active, not recruiting"
            "COMPLETED" -> "Completed"
            else -> raw.replace('_', ' ').lowercase().replaceFirstChar { it.titlecase() }
        }
    }

    private fun humanPhase(raw: String): String {
        return when (raw.uppercase()) {
            "NA" -> "Not applicable"
            "EARLY_PHASE1" -> "Early Phase 1"
            "PHASE1" -> "Phase 1"
            "PHASE2" -> "Phase 2"
            "PHASE3" -> "Phase 3"
            "PHASE4" -> "Phase 4"
            else -> raw.replace('_', ' ')
        }
    }

    private fun JSONArray?.stringList(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { index ->
            optString(index, "").trim().takeIf { it.isNotEmpty() }
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())

    private fun httpGet(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 20_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", USER_AGENT)
        }
        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
            if (code !in 200..299) {
                throw IllegalStateException("ClinicalTrials.gov returned HTTP $code")
            }
            body
        } finally {
            connection.disconnect()
        }
    }
}
