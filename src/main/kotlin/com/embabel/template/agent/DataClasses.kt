
package com.embabel.template.agent

import com.embabel.agent.domain.library.HasContent
import java.time.LocalDateTime

data class Person(
    val id: String,
    val first_name: String? = null,
    val surname: String? = null,
    val other_names: List<String>? = null,
//    val dob: LocalDateTime,
    val nationalities: List<String>? = null,
    val height: Int? = null, // in cm
    val weight: Int? = null, // in kg
    val sex: String? = null,
    val occupations: List<String>? = null,
    )

data class Organisation(
    val id: String,
    val node_type: String = "organisation",
    val name: String? = null,
    val description: String? = null,
    val date_founded: LocalDateTime? = null,
    )

data class Article(
    val id: String,
    val node_type: String = "article",
    val title: String? = null,
    val URL: String? = null,
    val content: String? = null,
    val language: String? = null,
    val summary: String? = null,
    val publish_datetime: LocalDateTime? = null,
    val scrape_datetime: LocalDateTime? = null,
    val sentiment: String? = null,
    )

data class Knowledge(
    val id: String,
    val node_type: String = "knowledge",
    val topic: String? = null,
    val fact: String? = null,
    )

data class Location(
    val id: String,
    val node_type: String = "location",
    val name: String? = null,
    val description: String? = null,
    val number: Int? = null,
    val street: String? = null,
    val city: String? = null,
    val country: String? = null,
    )

data class Event(
    val id: String,
    val node_type: String = "event",
    val description: String? = null, // short 2-3 sentence description of the event
    val started_at: LocalDateTime? = null,
    val finished_at: LocalDateTime? = null,
    val category: String? = null,
    )

data class Relationship(
    val name: String? = null,
    val start_node_id: String? = null,
    val end_node_id: String? = null,
    val properties: Map<String, String>? = null,
    val created_at: LocalDateTime? = null,
    val source_id: String? = null,
)

data class ExtractedNodes(
    val article: Article,
    val people: List<Person>,
    val organisations: List<Organisation>,
    val knowledge_points: List<Knowledge>,
    val locations: List<Location>,
    val events: List<Event>,
    val relationships: List<Relationship>,
)

data class ExtractedResult(
    val extracted_nodes: ExtractedNodes
) : HasContent {

    override val content: String
        get() = """
            # Article
            
            ${extracted_nodes.article.title}
            ${extracted_nodes.article.content}
            
            # People
            
            ${extracted_nodes.people.joinToString("\n")}
            
            # Organisations
            
            ${extracted_nodes.organisations.joinToString("\n")}
            
            # Knowledge
            
            ${extracted_nodes.knowledge_points.joinToString("\n")}
            
            # Locations
            
            ${extracted_nodes.locations.joinToString("\n")}
            
            # Events
            
            ${extracted_nodes.events.joinToString("\n")}
            
            # Relationships
            
            ${extracted_nodes.relationships.joinToString("\n")}
        """.trimIndent()
}
