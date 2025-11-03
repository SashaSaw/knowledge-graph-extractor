
package com.embabel.template.graph.repository

import com.embabel.template.graph.model.Article
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleRepository : Neo4jRepository<Article, Long> {}
