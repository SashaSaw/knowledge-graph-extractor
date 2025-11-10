package com.embabel.template.agent

import com.embabel.agent.api.annotation.AchievesGoal
import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.annotation.Export
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.api.common.create
import com.embabel.agent.domain.io.UserInput
import com.embabel.agent.prompt.persona.Persona
import com.embabel.agent.prompt.persona.RoleGoalBackstory
import com.embabel.common.ai.model.LlmOptions
import com.embabel.template.service.KnowledgeGraphService
import org.springframework.context.annotation.Profile
import org.slf4j.LoggerFactory

val DataAnalyst = RoleGoalBackstory(
    role = "Data analyst for graph databases",
    goal = "Generate accurate Cypher queries and analyze graph data",
    backstory = "Expert in Neo4j and Cypher query generation"
)

val InsightSpecialist = Persona(
    name = "Graph Analyst",
    persona = "Data analyst",
    voice = "Professional and clear",
    objective = "Analyze graph data and provide insights"
)



@Agent(description = "Read a knowledge graph to find information and answer a query")
@Profile("!test")
class GraphReadAgent(
    private val neo4jService: KnowledgeGraphService,
) {
    private val logger = LoggerFactory.getLogger(GraphReadAgent::class.java)
    @Action
    fun generateCypher(userInput: UserInput, context: OperationContext): CypherQuery {
        
        val prompt = """
            Generate a Cypher query for: "${userInput.content}"
            
            Schema:
            
            Graph schema includes:
            Nodes:
            Article(id: Long, title: String, url: String?, content: String, language: String?, summary: String?, publishDateTime: LocalDateTime?, scrapeDateTime: LocalDateTime?, agentProcessId: String?, sentiment: String?)
            Person(id: Long, firstName: String, lastName: String, nicknames: [String]?, dob: LocalDate?, nationalities: [String]?, height: Int? (cm), weight: Int? (kg), gender: String?, occupations: [String]?)
            Organisation(id: Long, name: String, dateFounded: LocalDate?, description: String?)
            Location(id: Long, name: String, number: String?, street: String?, city: String?, country: String?, latitude: Double?, longitude: Double?)
            Event(id: Long, description: String, startDate: LocalDate?, startTime: LocalTime?, endDate: LocalDate?, endTime: LocalTime?, category: String?, status: String?, outcome: String?, impact: String?)
            Knowledge(id: Long, fact: String, category: String?, dateOfFact: LocalDate?)

            Relationships:
            Article -[:MENTIONS]-> Person
            Article -[:MENTIONS]-> Organisation
            Article -[:MENTIONS]-> Event
            Article -[:MENTIONS]-> Location
            Knowledge -[:ABOUT]-> Person
            Knowledge -[:ABOUT]-> Organisation
            Person -[:INVOLVED_IN]-> Event
            Organisation -[:INVOLVED_IN]-> Event
            Event -[:OCCURRED_IN]-> Location
            Knowledge -[:SOURCED_FROM]-> Organisation

            Inverse relationships (for reasoning):
            Person <-[:MENTIONS]- Article
            Organisation <-[:MENTIONS]- Article
            Event <-[:MENTIONS]- Article
            Location <-[:MENTIONS]- Article
            Person <-[:ABOUT]- Knowledge
            Organisation <-[:ABOUT]- Knowledge
            Event <-[:INVOLVED_IN]- Person
            Event <-[:INVOLVED_IN]- Organisation
            Location <-[:OCCURRED_IN]- Event
            Organisation <-[:SOURCED_FROM]- Knowledge
            
            Rules:
            - Generate only READ queries (MATCH, RETURN, WHERE, ORDER BY, LIMIT).
            - Use LIMIT 10 for multiple results.
            - Return just the raw Cypher query without any markdown formatting.
            - The query MUST return all the properties of the retrieved nodes using {.*}
            - Try and return as many relevant nodes as possible.
        """.trimIndent()

        logger.info(prompt)

        val rawResponse = context.ai()
            .withLlm(LlmOptions.withAutoLlm().withTemperature(0.1))
            .withPromptContributor(DataAnalyst)
            .create<String>(prompt)

        // Strip markdown code block formatting if present
        val cypher = rawResponse
            .replace("```cypher\n", "")
            .replace("```\n", "")
            .replace("```", "")
            .trim()

        return CypherQuery(
            cypher = cypher,
            parameters = emptyMap(),
            explanation = "Generated query for: ${userInput.content}"
        )
    }

    @AchievesGoal(
        description = "Graph analysis completed",
        export = Export(remote = true, name = "analyzeGraph")
    )
    @Action  
    fun analyzeGraph(
        userInput: UserInput, 
        cypherQuery: CypherQuery, 
        context: OperationContext
    ): GraphInsight {
        val results = neo4jService.runRead(cypherQuery.cypher, cypherQuery.parameters)

        logger.info(results.toString())

        val analysisPrompt = """
            Analyze this data to answer: ${userInput.content}
            
            Data: ${results}
            
            Provide a summary and key findings.
        """.trimIndent()

        logger.info(analysisPrompt)

        val analysis = context.ai()
            .withLlm(LlmOptions.withAutoLlm().withTemperature(0.3))
            .withPromptContributor(InsightSpecialist)
            .create<String>(analysisPrompt)

        return GraphInsight(
            query = GraphQuery(userInput.content),
            cypherQuery = cypherQuery,
            rawResults = results,
            analysis = analysis,
            summary = "Found ${results.size} records",
            keyFindings = listOf("Data retrieved successfully")
        )
    }

}
