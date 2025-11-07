package com.embabel.template.agent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class PersonNode(
    val id: String? = "",
    val first_name: String,
    val last_name: String,
    val nicknames: List<String>? = null,
    val dob: LocalDate? = null,
    val nationalities: List<String>? = null,
    val height: Int? = null, // in cm
    val weight: Int? = null, // in kg
    val gender: String? = null,
    val occupations: List<String>? = null,
)

data class OrganisationNode(
    val id: String? = "",
    val name: String,
    val dateFounded: LocalDate? = null,
    val description: String? = null,
)

data class ArticleNode(
    val id: String? = "",
    val title: String,
    val url: String?,
    val content: String,
    val language: String?,
    val summary: String?,
    val publishDateTime: LocalDateTime?,
    val scrapeDateTime: LocalDateTime?,
    val agentProcessId: String?,
    val sentiment: String?,
)

data class KnowledgeNode(
    val id: String? = "",
    val fact: String,
    val category: String? = null,
    val dateOfFact: LocalDate? = null
)

data class LocationNode(
    val id: String? = "",
    val name: String,
    val number: String? = null,
    val street: String? = null,
    val city: String? = null,
    val country: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

data class EventNode(
    val id: String? = "",
    val description: String,
    val startDate: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endDate: LocalDate? = null,
    val endTime: LocalTime? = null,
    val category: String? = null,
    val status: String? = null,
    val outcome: String? = null,
    val impact: String? = null
)


data class ExtractedNodes(
    val article: ArticleNode,
    val people: List<PersonNode>? = null,
    val organisations: List<OrganisationNode>? = null,
    val knowledge: List<KnowledgeNode>? = null,
    val locations: List<LocationNode>? = null,
    val events: List<EventNode>? = null
)

