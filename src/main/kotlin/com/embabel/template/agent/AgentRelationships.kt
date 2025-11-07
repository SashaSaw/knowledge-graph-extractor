package com.embabel.template.agent
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