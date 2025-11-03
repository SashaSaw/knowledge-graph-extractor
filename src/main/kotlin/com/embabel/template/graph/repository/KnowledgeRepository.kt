
package com.embabel.template.graph.repository

import com.embabel.template.graph.model.Knowledge
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.stereotype.Repository

@Repository
interface KnowledgeRepository : Neo4jRepository<Knowledge, Long> {}
