
package com.embabel.template.agent

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class PersonNode(
    val id: String,
    val name: String,
    val nicknames: List<String>? = null,
    val dob: LocalDate? = null,
    val nationalities: List<String>? = null,
    val height: Int? = null, // in cm
    val weight: Int? = null, // in kg
    val gender: String? = null,
    val occupations: List<String>? = null,
)

data class OrganisationNode(
    val id: String,
    val name: String,
    val dateFounded: LocalDate? = null,
    val description: String? = null,
)

data class ArticleNode(
    val id: String,
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
    val id: String,
    val fact: String,
    val category: String? = null,
    val dateOfFact: LocalDate? = null
)

data class LocationNode(
    val id: String,
    val name: String,
    val number: String? = null,
    val street: String? = null,
    val city: String? = null,
    val country: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

data class EventNode(
    val id: String,
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

/**
 * ===========================================
 * Knowledge Graph Relationships Data Classes
 * ===========================================
 * Generated for: Person, Organisation, Event, Article, Knowledge, etc.
 * Each relationship represents an edge between two nodes in the graph.
 * All relationships include: created_at and source_id/source_ids
 */

data class MentionsPersonRelationship(// Article -> Person
    val start_node_id: String,
    val end_node_id: String,
    val evidence: String? = null,
)

data class AboutPersonRelationship(// Knowledge -> Person
    val start_node_id: String,
    val end_node_id: String,
)

data class InvolvedPersonRelationship(// Event -> Person
    val start_node_id: String,
    val end_node_id: String,
)

data class MentionsOrganisationRelationship(// Article -> Organisation
    val start_node_id: String,
    val end_node_id: String,
    val evidence: String? = null,
)

data class AboutOrganisationRelationship(// Knowledge -> Organisation
    val start_node_id: String,
    val end_node_id: String,
)

data class InvolvedOrganisationRelationship(// Event -> Organisation
    val start_node_id: String,
    val end_node_id: String,
)

data class SourcedFromRelationship(// Knowledge -> Article
    val start_node_id: String,
    val end_node_id: String,
)

data class MentionsEventRelationship(// Article -> Event
    val start_node_id: String,
    val end_node_id: String,
)

data class OccuredInRelationship(// Event -> Location
    val start_node_id: String,
    val end_node_id: String,
)

data class MentionsLocationRelationship(// Article -> Location
    val start_node_id: String,
    val end_node_id: String,
)


data class ExtractedRelationships(
    val mentionsPersonReltionships: List<MentionsPersonRelationship>? = null,
    val aboutPersonRelationships: List<AboutPersonRelationship>? = null,
    val involvedPersonRelationships: List<InvolvedPersonRelationship>? = null,
    val mentionsOrganisationRelationships: List<MentionsOrganisationRelationship>? = null,
    val aboutOrganisationRelationships: List<AboutOrganisationRelationship>? = null,
    val involvedOrganisationRelationships: List<InvolvedOrganisationRelationship>? = null,
    val sourcedFromRelationships: List<SourcedFromRelationship>? = null,
    val mentionsEventRelationships: List<MentionsEventRelationship>? = null,
    val occuredInRelationships: List<OccuredInRelationship>? = null,
    val mentionsLocationRelationships: List<MentionsLocationRelationship>? = null,
    val reasoning: String
)

data class FormattedExtraction(
    val nodes: ExtractedNodes,
    val relationships: ExtractedRelationships
) {
    override fun toString(): String {
        return """
        # Extracted Knowledge Graph

        ## Nodes
        ### Article
        - ${nodes.article.title}

        ### People
        ${nodes.people?.joinToString("\n") { "- ${it.name}" }}

        ### Organisations
        ${nodes.organisations?.joinToString("\n") { "- ${it.name}" }}

        ### Locations
        ${nodes.locations?.joinToString("\n") { "- ${it.name}" }}

        ### Events
        ${nodes.events?.joinToString("\n") { "- ${it.description}" }}

        ### Knowledge
        ${nodes.knowledge?.joinToString("\n") { "- ${it.fact}" }}
        """.trimIndent()
    }
}
