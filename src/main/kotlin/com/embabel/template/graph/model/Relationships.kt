
package com.embabel.template.graph.model

import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.RelationshipProperties
import org.springframework.data.neo4j.core.schema.TargetNode
import java.time.Instant

// Generic relationship for simplicity, can be extended
@RelationshipProperties
data class Mentions(
    @Id @GeneratedValue val id: Long? = null,

    @TargetNode
    val target: Person,

    val evidence: String?,
    val createdAt: Instant,
)
