package com.markemcallister.huntingtons.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClinicalTrialsClientTest {
    @Test
    fun parseStudiesKeepsHdTrialsAndDropsOffTopic() {
        val json = """
            {
              "studies": [
                {
                  "protocolSection": {
                    "identificationModule": {
                      "nctId": "NCT00000001",
                      "briefTitle": "Pridopidine study in Huntington's Disease"
                    },
                    "statusModule": {
                      "overallStatus": "RECRUITING",
                      "lastUpdatePostDateStruct": { "date": "2026-09-17" }
                    },
                    "descriptionModule": {
                      "briefSummary": "This trial asks whether an experimental medicine is safe. It compares the medicine with a placebo."
                    },
                    "sponsorCollaboratorsModule": { "leadSponsor": { "name": "Example Sponsor" } },
                    "conditionsModule": { "conditions": ["Huntington Disease"] },
                    "designModule": { "phases": ["PHASE3"] },
                    "armsInterventionsModule": { "interventions": [ { "name": "Pridopidine" }, { "name": "Placebo" } ] }
                  }
                },
                {
                  "protocolSection": {
                    "identificationModule": {
                      "nctId": "NCT00000002",
                      "briefTitle": "LLM-based Chatbot for mixed caregivers"
                    },
                    "statusModule": { "overallStatus": "RECRUITING", "lastUpdatePostDateStruct": { "date": "2026-09-01" } },
                    "descriptionModule": { "briefSummary": "A chatbot study." },
                    "conditionsModule": { "conditions": ["Huntington Disease", "Dementia"] }
                  }
                },
                {
                  "protocolSection": {
                    "identificationModule": {
                      "nctId": "NCT00000003",
                      "briefTitle": "Sociodemographic Factors and Criminal Behaviour Preceding Neurodegeneration"
                    },
                    "statusModule": { "overallStatus": "RECRUITING", "lastUpdatePostDateStruct": { "date": "2026-08-10" } },
                    "descriptionModule": { "briefSummary": "Observational record review." },
                    "conditionsModule": { "conditions": ["Huntington Disease"] }
                  }
                }
              ]
            }
        """.trimIndent()

        val items = ClinicalTrialsClient.parseStudies(json)
        assertEquals(1, items.size)
        assertEquals("NCT00000001", items[0].id)
        assertEquals("2026-09-17", items[0].date)
        assertEquals("https://clinicaltrials.gov/study/NCT00000001", items[0].sourceUrl)
        assertTrue(items[0].summary.contains("Recruiting"))
        assertTrue(items[0].summary.contains("Example Sponsor"))
        assertTrue(items[0].summary.contains("Pridopidine"))
        assertTrue(!items[0].summary.contains("Placebo") || items[0].summary.contains("registration"))
        assertTrue(items[0].summary.contains("not proof"))
    }

    @Test
    fun emptyStudiesIsAnEmptyListNotAnError() {
        val items = ClinicalTrialsClient.parseStudies("""{"studies":[]}""")
        assertTrue(items.isEmpty())
    }
}
