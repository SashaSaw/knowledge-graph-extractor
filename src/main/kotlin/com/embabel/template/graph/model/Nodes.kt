
package com.embabel.template.graph.model

import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// Base interface to allow generic relationships
interface BaseNode {
    val id: Long?
}

@Node("Person")
data class Person(
    @Id @GeneratedValue override val id: Long? = null,
    val name: String,
    var nicknames: List<String>? = null,
    var dob: LocalDate? = null,
    var nationalities: List<String>? = null,
    var height: Int? = null, // in cm
    var weight: Int? = null, // in kg
    var gender: String? = null,
    var occupations: List<String>? = null,

    @Relationship(type = "MENTIONS", direction = Relationship.Direction.INCOMING)
    var mentioned: List<Mentions> = listOf()
) : BaseNode

@Node("Organisation")
data class Organisation(
    @Id @GeneratedValue override val id: Long? = null,
    val name: String,
    var dateFounded: LocalDate? = null,
    var description: String? = null
) : BaseNode

@Node("Location")
data class Location(
    @Id @GeneratedValue override val id: Long? = null,
    val name: String,
    var number: String? = null,
    var street: String? = null,
    var city: String? = null,
    var country: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null
) : BaseNode

@Node("Event")
data class Event(
    @Id @GeneratedValue override val id: Long? = null,
    val description: String,
    var startDate: LocalDate? = null,
    var startTime: LocalTime? = null,
    var endDate: LocalDate? = null,
    var endTime: LocalTime? = null,
    var category: String? = null,
    var status: String? = null,
    var outcome: String? = null,
    var impact: String? = null
) : BaseNode

@Node("Knowledge")
data class Knowledge(
    @Id @GeneratedValue override val id: Long? = null,
    val fact: String,
    var category: String? = null,
    var dateOfFact: LocalDate? = null
) : BaseNode

@Node("Article")
data class Article(
    @Id @GeneratedValue override val id: Long? = null,
    val title: String,
    val url: String?,
    val content: String,
    val language: String?,
    val summary: String?,
    val publishDateTime: LocalDateTime?,
    val scrapeDateTime: LocalDateTime?,
    val agentProcessId: String?,
    val sentiment: String?,

    @Relationship(type = "MENTIONS", direction = Relationship.Direction.OUTGOING)
    var mentions: List<Mentions> = listOf()
) : BaseNode
