
package com.embabel.template.services
import com.embabel.template.dataharvesteragent.Article
import com.embabel.template.dataharvesteragent.DataHarvesterAgent
import org.neo4j.driver.Driver
import org.neo4j.driver.SessionConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class KnowledgeGraphService(private val driver: Driver) {
    private val logger = LoggerFactory.getLogger(DataHarvesterAgent::class.java)

    /**
     * Adds a completed data harvest to the knowledge graph.
     * Creates a Query node (if it doesn't exist), a Harvest node,
     * and Source nodes for each URL found.
     * Connects them with the appropriate relationships.
     */
    fun addToKnowledgeGraph(
        query: String,
        articles: List<Article>
    ) {
        driver.session(
            SessionConfig.builder()
                .withDatabase("neo4j")
                .build()
        ).use { session ->
            session.executeWrite { tx ->
                // Ensure the Query node exists for this operation
                tx.run("MERGE (q:Query {text: ${'$'}query}) ON CREATE SET q.createdAt = datetime()", mapOf("query" to query)).consume()

                articles.forEach { article ->
                    val params = mapOf(
                        "query" to query,
                        "url" to article.url,
                        "title" to article.title,
                        "summary" to article.summary,
                        "snippet" to article.snippet,
                        "index" to article.index,
                        "tags" to article.tags,
                        "content" to article.content,
                        "keyPersonName" to article.keyPerson?.name?.lowercase(),
                        "mentionedPeopleNames" to (article.mentionedPeople?.map { it.name.lowercase() } ?: emptyList())
                    )

                    tx.run(
                        """
                        // Find the query node
                        MATCH (q:Query {text: ${'$'}query})
                        
                        // Create or merge the article node
                        MERGE (a:Article {url: ${'$'}url})
                        ON CREATE SET
                            a.title = ${'$'}title,
                            a.summary = ${'$'}summary,
                            a.snippet = ${'$'}snippet,
                            a.index = ${'$'}index,
                            a.tags = ${'$'}tags,
                            a.content = ${'$'}content,
                            a.createdAt = datetime()
                        
                        // Link the query to the article
                        MERGE (q)-[:HARVESTED]->(a)
                        
                        // Carry forward the article and the person names
                        WITH a, ${'$'}keyPersonName AS keyPersonName, ${'$'}mentionedPeopleNames AS mentionedPeopleNames

                        // Conditionally create the key person and relationship
                        FOREACH (name IN CASE WHEN keyPersonName IS NOT NULL THEN [keyPersonName] ELSE [] END |
                            MERGE (p:Person {name: name})
                            MERGE (a)-[:ABOUT]->(p)
                        )

                        // Re-introduce 'a' and the list of mentioned people into the scope
                        WITH a, mentionedPeopleNames

                        // Create mentioned people and relationships
                        UNWIND mentionedPeopleNames AS mentionedName
                        MERGE (p:Person {name: mentionedName})
                        MERGE (a)-[:MENTIONS]->(p)
                        """.trimIndent(),
                        params
                    ).consume()
                }
            }
        }
    }

    // Find all articles about a person
    fun getArticlesAboutPerson(personName: String): List<Article> {
        val lowerPersonName = personName.lowercase()
        driver.session(
            SessionConfig.builder()
                .withDatabase("neo4j")
                .build()
        ).use { session ->
            return session.executeRead { tx ->
                val result = tx.run(
                    """
                MATCH (a:Article)-[:ABOUT]->(p:Person {name: ${'$'}personName})
                RETURN a.title AS title, a.summary AS summary, 
                       a.url AS url, a.snippet AS snippet,
                       a.index AS index, a.tags AS tags, a.content AS content
                """.trimIndent(),
                    mapOf("personName" to lowerPersonName)
                )
                result.list { record ->
                    val indexValue = record["index"]
                    val tagsValue = record["tags"]
                    val contentValue = record["content"]
                    Article(
                        title = record["title"].asString(),
                        summary = record["summary"].asString(),
                        url = record["url"].asString(),
                        snippet = record["snippet"].asString(),
                        index = if (indexValue.isNull) null else indexValue.asInt(),
                        tags = if (tagsValue.isNull) null else tagsValue.asList(Any::toString),
                        content = if (contentValue.isNull) null else contentValue.asString()
                    )
                }
            }
        }
    }

    // Find all queries about a person
    fun getQueriesAboutPerson(personName: String): List<String> {
        val lowerPersonName = personName.lowercase()
        driver.session(
            SessionConfig.builder()
                .withDatabase("neo4j")
                .build()
        ).use { session ->
            return session.executeRead { tx ->
                val result = tx.run(
                    """
                MATCH (q:Query)-[:ABOUT]->(p:Person {name: ${'$'}personName})
                RETURN q.text AS query
                ORDER BY q.createdAt DESC
                """.trimIndent(),
                    mapOf("personName" to lowerPersonName)
                )
                result.list { it["query"].asString() }
            }
        }
    }

    fun getRelatedPeopleAndCommonArticle(personName: String): List<String> {
        val lowerPersonName = personName.lowercase()
        driver.session(
            SessionConfig.builder()
                .withDatabase("neo4j")
                .build()
        ).use { session ->
            return session.executeRead { tx ->
                val result = tx.run(
                    """
                MATCH (start:Person {name: ${'$'}personName})-[r1]-(mid)-[r2]-(other:Person)
                WHERE start <> other
                RETURN DISTINCT other.name AS otherName, properties(mid) AS mid,
                               labels(mid) AS midLabels,
                               type(r1) AS rel1Type,
                               type(r2) AS rel2Type
                """.trimIndent(),
                    mapOf("personName" to lowerPersonName)
                )
                result.list { record ->
                    val otherName = record["otherName"].asString()
                    val midNode = record["mid"].asMap()
                    val midLabels = record["midLabels"].asList(Any::toString)
                    val rel1Type = record["rel1Type"].asString()
                    val rel2Type = record["rel2Type"].asString()

                    logger.info("THE MIDLABELS THAT THE FUCKING MID THINH HAS ARE${midNode}")
                    "Is connected to '$otherName' through $midNode via relationships '$rel1Type' and '$rel2Type'"
                }
            }
        }
    }

    // Find all articles about a person
    fun getArticlesThatMentionPerson(personName: String): List<Article> {
        val lowerPersonName = personName.lowercase()
        driver.session(
            SessionConfig.builder()
                .withDatabase("neo4j")
                .build()
        ).use { session ->
            return session.executeRead { tx ->
                val result = tx.run(
                    """
                MATCH (a:Article)-[:MENTIONS]->(p:Person {name: ${'$'}personName})
                RETURN a.title AS title, a.summary AS summary, 
                       a.url AS url, a.snippet AS snippet,
                       a.index AS index, a.tags AS tags, a.content AS content
                """.trimIndent(),
                    mapOf("personName" to lowerPersonName)
                )
                result.list { record ->
                    val indexValue = record["index"]
                    val tagsValue = record["tags"]
                    val contentValue = record["content"]
                    Article(
                        title = record["title"].asString(),
                        summary = record["summary"].asString(),
                        url = record["url"].asString(),
                        snippet = record["snippet"].asString(),
                        index = if (indexValue.isNull) null else indexValue.asInt(),
                        tags = if (tagsValue.isNull) null else tagsValue.asList(Any::toString),
                        content = if (contentValue.isNull) null else contentValue.asString()
                    )
                }
            }
        }
    }

    fun clearDatabase() {
        driver.session(
            SessionConfig.builder()
                .withDatabase("neo4j")
                .build()
        ).use { session ->
            session.executeWrite { tx ->
                tx.run("MATCH (n) DETACH DELETE n").list()
            }
        }
        println("Database cleared successfully")
    }
}