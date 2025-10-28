package com.embabel.template.dataharvesteragent

import com.embabel.common.core.types.Timestamped
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDateTime

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "node_type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = Node.Person::class, name = "person"),
    JsonSubTypes.Type(value = Node.FootballPlayer::class, name = "footballplayer"),
    JsonSubTypes.Type(value = Node.Organisation::class, name = "organisation"),
    JsonSubTypes.Type(value = Node.Article::class, name = "article"),
    JsonSubTypes.Type(value = Node.Knowledge::class, name = "knowledge"),
    JsonSubTypes.Type(value = Node.Location::class, name = "location"),
    JsonSubTypes.Type(value = Node.Event::class, name = "event"),
)

sealed class Node {

data class Person(
    val node_type: String = "person",
    val first_name: String? = null,
    val surname: String? = null,
    val other_names: List<String>? = null,
//    val dob: LocalDateTime,
    val nationalities: List<String>? = null,
    val height: Int? = null, // in cm
    val weight: Int? = null, // in kg
    val sex: String? = null,
    val occupations: List<String>? = null,
    ) : Node()

data class FootballPlayer(
    val node_type: String = "footballplayer",
    val first_name: String? = null,
    val surname: String? = null,
    val other_names: List<String>? = null,
//    val dob: LocalDateTime,
    val nationalities: List<String>? = null,
    val height: Int? = null, // in cm
    val weight: Int? = null, // in kg
    val sex: String? = null,
    val occupations: List<String>? = mutableListOf("footballplayer"),
    val positions: List<String>? = null,
    val preferred_foot: String? = null,
    ) : Node()

data class Organisation(
    val node_type: String = "organisation",
    val name: String? = null,
    val description: String? = null,
    ) : Node()

data class Article(
    val node_type: String = "article",
    val title: String? = null,
    val URL: String? = null,
    val content: String? = null,
    val language: String? = null,
    val summary: String? = null,
    val publish_datetime: LocalDateTime? = null,
    val scrape_datetime: LocalDateTime? = null,
    val sentiment: String? = null,
    ) : Node()

data class Knowledge(
    val node_type: String = "knowledge",
    val topic: String? = null,
    val fact: String? = null,
    ) : Node()

data class Location(
    val node_type: String = "location",
    val name: String? = null,
    val description: String? = null,
    val number: Int? = null,
    val street: String? = null,
    val city: String? = null,
    val country: String? = null,
    ) : Node()

data class Event(
    val node_type: String = "event",
    val description: String? = null, // short 2-3 sentence description of the event
    val started_at: LocalDateTime? = null,
    val finished_at: LocalDateTime? = null,
    val category: String? = null,
    ) : Node()
}


