package com.markemcallister.huntingtons.data

data class Catalog(
    val contentVersion: String,
    val generatedAt: String,
    val educationLastReviewed: String,
    val overview: EducationPage,
    val treatments: EducationPage,
    val research: ResearchPage,
    val resources: ResourcePage,
    val glossary: List<GlossaryTerm>,
    val disclaimer: Disclaimer,
    val about: About,
)

data class EducationPage(
    val title: String,
    val intro: String,
    val callout: String,
    val sections: List<EducationSection>,
    val sources: List<NamedLink>,
)

data class EducationSection(
    val id: String,
    val title: String,
    val body: String,
    val glossaryTerms: List<String>,
)

data class NamedLink(
    val label: String,
    val url: String,
)

data class ResearchPage(
    val title: String,
    val howToRead: String,
    val askSpecialist: String,
    val pipelineThemes: List<EducationSection>,
    val clinicalTrialsPrimer: String,
    val exploreTrialsLabel: String,
    val exploreTrialsUrl: String,
    val notableDevelopments: List<ResearchItem>,
    val trialItems: List<ResearchItem>,
)

data class ResearchItem(
    val id: String,
    val title: String,
    val summary: String,
    val source: String,
    val sourceUrl: String,
    val date: String,
    val kind: String,
)

data class ResourcePage(
    val intro: String,
    val items: List<ResourceItem>,
)

data class ResourceItem(
    val title: String,
    val description: String,
    val url: String,
    val category: String,
)

data class GlossaryTerm(
    val term: String,
    val definition: String,
)

data class Disclaimer(
    val shortText: String,
    val fullText: String,
)

data class About(
    val purpose: String,
    val sourcing: String,
    val refresh: String,
    val version: String,
)
