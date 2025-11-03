
package com.embabel.template.graph.repository

import com.embabel.template.graph.model.Organisation
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.stereotype.Repository

@Repository
interface OrganisationRepository : Neo4jRepository<Organisation, Long> {
    fun findByName(name: String): Organisation?
}
