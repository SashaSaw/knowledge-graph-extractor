
package com.embabel.template.graph.model

import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.RelationshipProperties
import org.springframework.data.neo4j.core.schema.TargetNode
import java.time.Instant

@RelationshipProperties
data class MentionsPerson(
    @Id @GeneratedValue val id: Long? = null,
    @TargetNode val person: Person,
    val evidence: String?,
    val createdAt: Instant,
)

@RelationshipProperties
data class AboutPerson(
    @Id @GeneratedValue val id: Long? = null,
    @TargetNode val person: Person,
    val createdAt: Instant = Instant.now(),
)

@RelationshipProperties
data class InvolvedPerson(
    @Id @GeneratedValue val id: Long? = null,
    @TargetNode val person: Person,
    val createdAt: Instant = Instant.now(),
)

@RelationshipProperties
data class MentionsOrganisation(
    @Id @GeneratedValue val id: Long? = null,
    @TargetNode val organisation: Organisation,
    val evidence: String?,
    val createdAt: Instant = Instant.now(),
)

@RelationshipProperties
data class AboutOrganisation(
    @Id @GeneratedValue val id: Long? = null,
    @TargetNode val organisation: Organisation,
    val createdAt: Instant = Instant.now(),
)

@RelationshipProperties
data class InvolvedOrganisation(
    @Id @GeneratedValue val id: Long? = null,
    @TargetNode val organisation: Organisation,
    val createdAt: Instant = Instant.now(),
)

@RelationshipProperties
data class SourcedFrom(
    @Id @GeneratedValue val id: Long? = null,
    @TargetNode val article: Article,
    val createdAt: Instant = Instant.now(),
)

@RelationshipProperties
data class MentionsEvent(
    @Id @GeneratedValue val id: Long? = null,
    @TargetNode val event: Event,
    val createdAt: Instant = Instant.now(),
)

@RelationshipProperties
data class OccuredIn(
    @Id @GeneratedValue val id: Long? = null,
    @TargetNode val location: Location,
    val createdAt: Instant = Instant.now(),
)

@RelationshipProperties
data class MentionsLocation(
    @Id @GeneratedValue val id: Long? = null,
    @TargetNode val location: Location,
    val createdAt: Instant = Instant.now(),
)
