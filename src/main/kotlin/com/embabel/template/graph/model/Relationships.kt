
package com.embabel.template.graph.model

import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.RelationshipProperties
import org.springframework.data.neo4j.core.schema.TargetNode

@RelationshipProperties
data class RelatedTo(
    @Id @GeneratedValue val id: Long? = null,
    val evidence: String?,
    @TargetNode
    val target: BaseNode
)

@RelationshipProperties
data class Mentions(
    @Id @GeneratedValue val id: Long? = null,
    val evidence: String?,
    @TargetNode
    val target: BaseNode
)
