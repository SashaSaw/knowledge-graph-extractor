package com.embabel.template.tools

import com.embabel.template.services.KnowledgeGraphService
import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Component

/**
 * Exposes knowledge graph querying capabilities to an LLM.
 * Acts as a safe facade over the KnowledgeGraphService.
 */
@Component
class KnowledgeGraphTool(private val knowledgeGraphService: KnowledgeGraphService) {

    @Tool(description = "Finds all articles that have been harvested about a specific person. Returns a list of article titles and their full content.")
    fun findArticlesAbout(personName: String): List<String> {
        val articles = knowledgeGraphService.getArticlesAboutPerson(personName)
        if (articles.isEmpty()) {
            return listOf("No articles found about $personName.")
        }
        return articles.map { "- ${it.title}: ${it.content}" }
    }

    @Tool(description = "Finds all the original search queries that have been run about a specific person. Returns a list of the queries.")
    fun findQueriesAbout(personName: String): List<String> {
        val queries = knowledgeGraphService.getQueriesAboutPerson(personName)
        if (queries.isEmpty()) {
            return listOf("No previous queries found about $personName.")
        }
        return queries
    }

    @Tool(description = "Finds people related to a given person within two hops and includes the intermediary node (e.g., common Article) and relationship types connecting them. Returns human-readable connection summaries.")
    fun findRelatedPeopleAndCommonArticle(personName: String): List<String> {
        val connections = knowledgeGraphService.getRelatedPeopleAndCommonArticle(personName)
        if (connections.isEmpty()) {
            return listOf("No related people within two hops found for $personName.")
        }
        return connections
    }

    @Tool(description = "Finds all articles that mention a specific person. Returns a list of article titles and their full content.")
    fun findArticlesThatMention(personName: String): List<String> {
        val articles = knowledgeGraphService.getArticlesThatMentionPerson(personName)
        if (articles.isEmpty()) {
            return listOf("No articles found about $personName.")
        }
        return articles.map { "- ${it.title}: ${it.content}" }
    }
}