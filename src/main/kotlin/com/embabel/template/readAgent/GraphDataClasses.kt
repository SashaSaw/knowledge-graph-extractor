package com.embabel.template.agent

import com.embabel.agent.domain.library.HasContent
import com.embabel.common.core.types.Timestamped
import java.time.Instant

data class GraphQuery(
    val question: String
)

data class CypherQuery(
    val cypher: String,
    val parameters: Map<String, Any>,
    val explanation: String
)

data class GraphInsight(
    val query: GraphQuery,
    val cypherQuery: CypherQuery,
    val rawResults: List<Map<String, Any>>,
    val analysis: String,
    val summary: String,
    val keyFindings: List<String>
) : HasContent, Timestamped {

    override val timestamp: Instant = Instant.now()

    override val content: String = """
# Data Analysis Results

**Query:** ${query.question}

## Summary
$summary

## Analysis
$analysis

## Key Findings
${keyFindings.joinToString("\n") { "• $it" }}

## Technical Details
**Records Found:** ${rawResults.size}
**Query:** `${cypherQuery.cypher}`
        """.trimIndent()
}


