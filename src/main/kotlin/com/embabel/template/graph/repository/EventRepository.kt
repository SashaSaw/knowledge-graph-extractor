
package com.embabel.template.graph.repository

import com.embabel.template.graph.model.Event
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.stereotype.Repository

@Repository
interface EventRepository : Neo4jRepository<Event, Long> {}
