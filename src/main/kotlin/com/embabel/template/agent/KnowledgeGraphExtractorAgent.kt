
package com.embabel.template.agent

import com.embabel.agent.api.annotation.AchievesGoal
import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.domain.io.UserInput
import com.embabel.common.ai.model.LlmOptions
import com.embabel.template.service.KnowledgeGraphService
import org.springframework.context.annotation.Profile
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

data class FormattedExtraction(
    val nodes: ExtractedNodes,
    val relationships: ExtractedRelationships
) {
    override fun toString(): String {
        return """
        # Extracted Knowledge Graph

        ## Nodes
        ### Article
        - ${nodes.article.title}

        ### People
        ${nodes.people?.joinToString("\n") { "- ${it.first_name} ${it.last_name}" }}

        ### Organisations
        ${nodes.organisations?.joinToString("\n") { "- ${it.name}" }}

        ### Locations
        ${nodes.locations?.joinToString("\n") { "- ${it.name}" }}

        ### Events
        ${nodes.events?.joinToString("\n") { "- ${it.description}" }}

        ### Knowledge
        ${nodes.knowledge?.joinToString("\n") { "- ${it.fact}" }}
        """.trimIndent()
    }
}

val article1 = ArticleNode(
    id = UUID.randomUUID().toString(),
    title = "Villa signing Rashford loses three sponsorship deals",
    url = "https://www.tribalfootball.com/article/soccer-premier-league-villa-signing-rashford-loses-three-sponsorship-deals-6d925766-53e5-42b5-a1e2-357f9159d385",
    content = """
        Forward Marcus Rashford has lost sponsorship deals with three major brands in recent years as his struggles at Manchester United continued.
        His dip in form saw him miss out on England’s Euro 2024 squad and led to a January loan move to Aston Villa.
        
        During this period, he reportedly parted ways with Burberry, Levi’s, and Beats, which had backed him at the peak of his popularity.
        
        Per The Mail, his switch to Villa could further impact his sponsorship earnings, particularly with Nike.
        
        United were in Nike’s top-tier category, but Villa are in a lower band, meaning a potential 50% reduction in his earnings.
        
        With fewer goals and appearances already affecting his income, Rashford’s move may prove costly beyond the pitch.
    """.trimIndent(),
    language = "en",
    summary = "Marcus Rashford has reportedly lost sponsorships with Burberry, Levi’s, and Beats amid poor form at Manchester United and a loan move to Aston Villa, which could also reduce his Nike earnings.",
    publishDateTime = LocalDateTime.of(2025, 2, 5, 17, 30),
    scrapeDateTime = LocalDateTime.now(),
    agentProcessId = null,
    sentiment = "negative"
)

val article2 = ArticleNode(
    id = UUID.randomUUID().toString(),
    title = "Manchester United’s Marcus Rashford Faces Disciplinary Action for Night Out Amidst Illness Controversy",
    url = "https://www.tribalfootball.com/article/manchester-uniteds-marcus-rashford-faces-disciplinary-action-for-night-out-amidst-illness-controversy", // assumed source URL format
    content = """
        Manchester United has addressed a disciplinary matter involving forward Marcus Rashford after he was reportedly spotted in a Belfast nightclub last week, subsequently reporting himself as ill. The club confirmed the disciplinary action on Monday, revealing that Rashford had taken responsibility for his actions.

        As a consequence, Rashford was left out of the squad for Manchester United’s 4-2 victory against Newport in the FA Cup on Sunday, with the club citing his illness as the reason for his absence. Manager Erik ten Hag, addressing the situation after the match, stated that he would handle the matter regarding Rashford’s reported activities in the week leading up to the game.

        “Marcus has taken responsibility for his actions. This has been dealt with as an internal disciplinary matter, which is now closed,” Manchester United stated on Monday. The club did not specify whether Rashford had faced a financial penalty for his actions.

        Despite missing the FA Cup fixture, Rashford is expected to be available for selection when Manchester United faces Wolves in the English Premier League on Thursday.

        This isn’t the first time Rashford’s off-field activities have come under scrutiny. Earlier in the season, Ten Hag expressed dissatisfaction when Rashford participated in birthday celebrations after United’s defeat to Manchester City. In November, the manager addressed the issue, stating, “It is unacceptable. I told him. He apologized and that is it.”

        The incident adds another layer of scrutiny to Rashford‘s conduct off the pitch and raises questions about the player’s responsibilities as a high-profile figure representing Manchester United. The club’s decision to address the matter internally reflects their commitment to upholding disciplinary standards within the team.
    """.trimIndent(),
    language = "en",
    summary = "Marcus Rashford faced disciplinary action from Manchester United after being seen at a nightclub in Belfast before calling in sick. The club confirmed the matter was handled internally, with Rashford taking responsibility. Despite the incident, he is expected to be available for selection against Wolves.",
    publishDateTime = LocalDateTime.of(2023, 1, 29, 12, 0), // estimated from “2 years ago” relative to 2025
    scrapeDateTime = LocalDateTime.now(),
    agentProcessId = null,
    sentiment = "negative"
)

val article3 = ArticleNode(
    id = UUID.randomUUID().toString(),
    title = "Another huge scandal, Marcus Rashford will pay for this after his indiscipline",
    url = "https://www.elfutbolero.com/soccer/another-huge-scandal-marcus-rashford-will-pay-for-this-after-his-indiscipline-20240129", // inferred plausible source URL
    content = """
        Marcus Rashford earns a lot of money at Manchester United (382,000 euros a week), but the striker is not happy. He has been feuding with Erik Ten Hag for months and his role on the team has become residual. Annoyed by the desperate months he is going through, the footballer went out partying on Thursday and Friday. And on Sunday he was left out of the call.
        
        Manchester United just leads in the FA Cup at halftime against Newport County.
        
        In addition to the sporting punishment, Rashford faces, according to 'The Sun', a financial fine of 750,000 euros (which is equivalent to two weeks' salary), an amount that, although it is not money for him, for most mortals it is a huge figure. Last November, Ten Hag already criticized Rashford for partying after losing 3-0 to his archrival, Manchester City.
        
        Manchester United striker Marcus Rashford once again made headlines when he was spotted at a Belfast nightclub on Thursday, hours before missing team training, citing illness. This episode marks the second time he was seen there before reporting his inability to attend training on Friday, despite previously being seen in Northern Ireland on Wednesday.
        
        Images spread on social media suggest that Rashford entered Thompsons Garage nightclub on Wednesday night. Sources close to the footballer indicate that he was having dinner, providing details to the chronology of events. Although he returned to Manchester on Friday morning, he was absent from training due to his health condition, as reported by Mail Sport.
        
        Contradicting the initial version, The Athletic presents footage showing Rashford at a club in the country's capital hours before his supposed meeting in Carrington. Erik ten Hag, Manchester United coach, confirmed the English international's absence due to illness in a press conference prior to the FA Cup match against Newport County. During his trip, Rashford visited the Larne team's training ground, where his former teammate Roshaun Williams now plays. Although Thursday was scheduled as a day off for the players, reports indicate that Rashford was nowhere near United's training complex the night before, attending Lavery's Bar on Wednesday and Thompsons Garage nightclub on Thursday.
    """.trimIndent(),
    language = "en",
    summary = "Marcus Rashford faces disciplinary and financial repercussions after being spotted partying in Belfast before missing Manchester United training due to alleged illness. Reports suggest repeated incidents, straining his relationship with manager Erik ten Hag and drawing criticism over his professionalism.",
    publishDateTime = LocalDateTime.of(2024, 1, 29, 14, 52), // From '29/01/2024, 02:52 PM +00:00'
    scrapeDateTime = LocalDateTime.now(),
    agentProcessId = null,
    sentiment = "negative"
)

val articles = listOf(article1, article2, article3)

data class ArticleNum(
    val number: Int,
)

@Agent(
    description = "Extracts knowledge graph nodes and relationships from an article.",
)
@Profile("!test")
class KnowledgeGraphExtractorAgent(private val knowledgeGraphService: KnowledgeGraphService) {
    private val logger = LoggerFactory.getLogger(KnowledgeGraphExtractorAgent::class.java)

    @Action
    fun getArticle(userInput: UserInput, context: OperationContext): ArticleNode {
        val articleNum = context.ai()
            .withLlm("granite4:micro-h")
            .createObject("Take this user input:${userInput} and tell me what number article the user wants to test",
                ArticleNum::class.java)
        return if (articleNum.number in (1..3)){
            articles[articleNum.number - 1]
        }
        else{
            article1
        }
    }

    @Action
    fun extractNodes(article: ArticleNode, context: OperationContext): ExtractedNodes {
        val prompt = """
        You are an information extraction system that builds a knowledge graph from text.  
        Your goal is to identify **entities (nodes)** from the article provided.

        Follow the schema and output format exactly as described below.

        ---

        ### NODE TYPES

        #### 1. Person
        Represents an individual human.
        - Fields: name, nicknames, dob, nationalities, height, weight, gender, occupations  
        - Example: Marcus Rashford, Erik ten Hag

        #### 2. Organisation
        Represents any company, sports team, government body, or media outlet.
        - Fields: name, dateFounded, description
        - Example: Manchester United, Aston Villa, Nike, The Mail

        #### 3. Location
        Represents a geographic or physical place.
        - Fields: name, city, country, latitude, longitude  
        - Example: Belfast, Manchester, Carrington Training Ground

        #### 4. Event
        Represents a specific occurrence or incident.
        - Fields: description, startDate, endDate, category, status, outcome, impact  
        - Example: Rashford Disciplinary Incident, FA Cup Match vs Newport County, Transfer to Aston Villa

        #### 5. Knowledge
        Represents a factual statement or claim.
        - Fields: fact, category, dateOfFact  
        - Example: "Rashford earns €382,000 per week", "Rashford fined €750,000"

        #### 6. Article
        Represents the article being processed.
        - Fields: title, url, content, language, summary, publishDateTime, sentiment  
        - Example: "Marcus Rashford Loses Sponsorship Deals After Villa Move"

        ### INSTRUCTIONS

        1. Identify all possible nodes in the article text (People, Organisations, Locations, Events, Knowledge, Article).
        2. Please only fill properties based off information in the article. If information is not in the article to support a property then it must be null.
        3. Do not create duplicate nodes (e.g., "Manchester United" and "Man United" should be treated as the same and try to use the most clear name).  
        3.  **Be Exhaustive:** Analyze the article text carefully. Create as many Nodes as you can possibly find evidence for.
        ### ARTICLE
        
        Title: ${article.title}
        Content: ${article.content}
        """.trimIndent()

//        val mockExtractedNodes = ExtractedNodes(
//            article = ArticleNode(
//                id = "9fb38238-ab30-4489-b75a-2b084d15d9cd",
//                title = "Another huge scandal, Marcus Rashford will pay for this after his indiscipline",
//                url = "[provided_url]",
//                content = "Marcus Rashford earns a lot of money at Manchester United (382,000 euros a week), but the striker is not happy. He has been feuding with Erik Ten Hag for months and his role on the team has become residual. Annoyed by the desperate months he is going through, the footballer went out partying on Thursday and Friday. And on Sunday he was left out of the call.\n\nManchester United just leads in the FA Cup at halftime against Newport County.\nIn addition to the sporting punishment, Rashford faces, according to 'The Sun', a financial fine of 750,000 euros (which is equivalent to two weeks' salary), an amount that, although it is not money for him, for most mortals it is a huge figure. Last November, Ten Hag already criticized Rashford for partying after losing 3-0 to his archrival, Manchester City.\n\nManchester United striker Marcus Rashford once again made headlines when he was spotted at a Belfast nightclub on Thursday, hours before missing team training, citing illness. This episode marks the second time he was seen there before reporting his inability to attend training on Friday, despite previously being seen in Northern Ireland on Wednesday.\nImages spread on social media suggest that Rashford entered Thompsons Garage nightclub on Wednesday night. Sources close to the footballer indicate that he was having dinner, providing details to the chronology of events. Although he returned to Manchester on Friday morning, he was absent from training due to his health condition, as reported by Mail Sport.\nContradicting the initial version, The Athletic presents footage showing Rashford at a club in the country's capital hours before his supposed meeting in Carrington. Erik ten Hag, Manchester United coach, confirmed the English international's absence due to illness in a press conference prior to the FA Cup match against Newport County. During his trip, Rashford visited the Larne team's training ground, where his former teammate Roshaun Williams now plays. Although Thursday was scheduled as a day off for the players, reports indicate that Rashford was nowhere near United's training complex the night before, attending Lavery's Bar on Wednesday and Thompsons Garage nightclub on Thursday.",
//                language = "en",
//                summary = "Marcus Rashford faces financial penalty and missing training due to partying in Belfast nightclub.",
//                publishDateTime = LocalDateTime.parse("2025-11-06T12:34:56"),
//                scrapeDateTime = LocalDateTime.parse("2025-11-06T13:00:00"),
//                agentProcessId = null,
//                sentiment = "negative"
//            ),
//            people = listOf(
//                PersonNode(
//                    id = "4d25c0a9-d8a9-4151-a872-acd4ccfbbb29",
//                    first_name = "Marcus",
//                    last_name = "Rashford",
//                    nicknames = listOf("Rash", "Mars"),
//                    occupations = listOf("footballer")
//                )
//            ),
//            organisations = listOf(
//                OrganisationNode(id = "12ce8487-dfa7-4baf-9c43-327b7b198b8b", name = "Manchester United"),
//                OrganisationNode(id = "e328bd9b-170a-48d6-8263-5b762031b6f8", name = "Larne"),
//                OrganisationNode(id = "e4e3dd9a-5df5-4dd7-90a7-6e7c942788af", name = "Thompsons Garage"),
//                OrganisationNode(id = "7ef30b23-ce49-46e9-bcc2-3c023d4a2230", name = "The Athletic")
//            ),
//            knowledge = listOf(
//                KnowledgeNode(
//                    id = "34ecf13f-faa2-46e0-a7e7-aec1abe27ff0",
//                    fact = "\"Rashford earns €382,000 per week\"",
//                    category = "salary",
//                    dateOfFact = LocalDate.parse("2025-11-06")
//                ),
//                KnowledgeNode(
//                    id = "3000b399-23e1-4df6-ae8e-280f7358beed",
//                    fact = "\"Rashford fined €750,000\"",
//                    category = "financial penalty",
//                    dateOfFact = LocalDate.parse("2025-11-06")
//                )
//            ),
//            locations = listOf(
//                LocationNode(
//                    id = "8fa78622-8a47-4567-8a9a-2d29b6130443",
//                    name = "Belfast",
//                    city = "Manchester",
//                    country = "United Kingdom"
//                )
//            ),
//            events = listOf(
//                EventNode(
//                    id = "4861c163-8aac-4f72-a5e6-53eca0f2f477",
//                    description = "Rashford Disciplinary Incident",
//                    startDate = LocalDate.parse("2025-11-06"),
//                    endDate = LocalDate.parse("2025-11-07"),
//                    category = "disciplinary",
//                    status = "ongoing",
//                    outcome = "fine and training ban"
//                ),
//                EventNode(
//                    id = "3cb5e3c3-1695-4f94-9796-5bf1e3fbc777",
//                    description = "FA Cup Match vs Newport County",
//                    startDate = LocalDate.parse("2025-11-05"),
//                    endDate = LocalDate.parse("2025-11-06"),
//                    category = "football",
//                    status = "completed"
//                )
//            )
//        )
//        return mockExtractedNodes

//        val nodes = context.ai().withDefaultLlm().createObject(prompt, ExtractedNodes::class.java)
        val nodes = context.ai().withLlm(LlmOptions.withModel("gpt-oss:20b").withTemperature(0.3)).createObject(prompt, ExtractedNodes::class.java)
        return nodes.copy(
            article = nodes.article.copy(id = UUID.randomUUID().toString()),
            people = nodes.people?.map { it.copy(id = UUID.randomUUID().toString()) },
            organisations = nodes.organisations?.map { it.copy(id = UUID.randomUUID().toString()) },
            locations = nodes.locations?.map { it.copy(id = UUID.randomUUID().toString()) },
            events = nodes.events?.map { it.copy(id = UUID.randomUUID().toString()) },
            knowledge = nodes.knowledge?.map { it.copy(id = UUID.randomUUID().toString()) }
        )
    }

    @Action
    fun extractRelationships(nodes: ExtractedNodes, context: OperationContext): ExtractedRelationships {
        val prompt = """
You are an information extraction system that builds a knowledge graph from text.
Your goal is to identify all possible relationships (edges) between the provided nodes, using the node ids exactly as provided.

### STRICT REQUIREMENTS
1. Use only the node ids present in the NODES section below for start_node_id and end_node_id.
2. Return a single JSON object only (no surrounding text).
3. Be exhaustive and add a relationship every time you find any evidence in the article.

---

### ARTICLE
Title: ${nodes.article.title}
Content: ${nodes.article.content}

---

### NODES (use these exact ids)
The Article Node:
  - id: ${nodes.article.id}
  - title: ${nodes.article.title}
  - content: ${nodes.article.content}

PERSON NODES:
${nodes.people?.joinToString("\n") { "  - id: ${it.id}\n    name: ${it.first_name} ${it.last_name}" } ?: "  []"}

ORGANISATION NODES:
${nodes.organisations?.joinToString("\n") { "  - id: ${it.id}\n    name: ${it.name}" } ?: "  []"}

KNOWLEDGE NODES:
${nodes.knowledge?.joinToString("\n") { "  - id: ${it.id}\n    fact: ${it.fact}" } ?: "  []"}

LOCATION NODES:
${nodes.locations?.joinToString("\n") { "  - id: ${it.id}\n    name: ${it.name}" } ?: "  []"}

EVENT NODES:
${nodes.events?.joinToString("\n") { "  - id: ${it.id}\n    description: ${it.description}" } ?: "  []"}

---

### RELATIONSHIP TYPES
MentionsPersonRelationship:
- Start_node_id MUST come from an Article node
- End_node_id MUST come from a Person node


AboutPersonRelationship:
- Start_node_id MUST come from a Knowledge node
- End_node_id MUST come from a Person node


InvolvedPersonRelationship:
- Start_node_id MUST come from an Event node
- End_node_id MUST come from a Person node


MentionsOrganisationRelationship:
- Start_node_id MUST come from an Article node
- End_node_id MUST come from a Organisation node


AboutOrganisationRelationship:
- Start_node_id MUST come from a Knowledge node
- End_node_id MUST come from a Organisation node


InvolvedOrganisationRelationship:
- Start_node_id MUST come from an Event node
- End_node_id MUST come from a Organisation node


SourcedFromRelationship:
- Start_node_id MUST come from an Knowledge node
- End_node_id MUST come from an Article node


MentionsEventRelationship:
- Start_node_id MUST come from an Article node
- End_node_id MUST come from an Event node


OccuredInRelationship:
- Start_node_id MUST come from an Event node
- End_node_id MUST come from a Location node


MentionsLocationRelationship:
- Start_node_id MUST come from an Article node
- End_node_id MUST come from an Location node

---

Please also include clear reasoning for why you have or have not created any relationships.

Now produce the JSON output.
""".trimIndent()
//        val mockExtractedRelationships = ExtractedRelationships(
//            mentionsPersonReltionships = listOf(
//                MentionsPersonRelationship(
//                    start_node_id = "9fb38238-ab30-4489-b75a-2b084d15d9cd",
//                    end_node_id = "4d25c0a9-d8a9-4151-a872-acd4ccfbbb29",
//                    evidence = "because I said so"
//                )
//            ),
//            aboutPersonRelationships = listOf(
//                AboutPersonRelationship(
//                    start_node_id = "34ecf13f-faa2-46e0-a7e7-aec1abe27ff0",
//                    end_node_id = "4d25c0a9-d8a9-4151-a872-acd4ccfbbb29"
//                ),
//                AboutPersonRelationship(
//                    start_node_id = "3000b399-23e1-4df6-ae8e-280f7358beed",
//                    end_node_id = "4d25c0a9-d8a9-4151-a872-acd4ccfbbb29"
//                )
//            ),
//            involvedPersonRelationships = listOf(
//                InvolvedPersonRelationship(
//                    start_node_id = "4861c163-8aac-4f72-a5e6-53eca0f2f477",
//                    end_node_id = "4d25c0a9-d8a9-4151-a872-acd4ccfbbb29"
//                )
//            ),
//            mentionsOrganisationRelationships = listOf(
//                MentionsOrganisationRelationship(
//                    start_node_id = "9fb38238-ab30-4489-b75a-2b084d15d9cd",
//                    end_node_id = "12ce8487-dfa7-4baf-9c43-327b7b198b8b"
//                ),
//                MentionsOrganisationRelationship(
//                    start_node_id = "9fb38238-ab30-4489-b75a-2b084d15d9cd",
//                    end_node_id = "e328bd9b-170a-48d6-8263-5b762031b6f8"
//                ),
//                MentionsOrganisationRelationship(
//                    start_node_id = "9fb38238-ab30-4489-b75a-2b084d15d9cd",
//                    end_node_id = "7ef30b23-ce49-46e9-bcc2-3c023d4a2230"
//                ),
//            ),
//            aboutOrganisationRelationships = listOf(
//                AboutOrganisationRelationship(
//                    start_node_id = "3000b399-23e1-4df6-ae8e-280f7358beed",
//                    end_node_id = "12ce8487-dfa7-4baf-9c43-327b7b198b8b"
//                )
//            ),
//            involvedOrganisationRelationships = listOf(
//                InvolvedOrganisationRelationship(
//                    start_node_id = "3cb5e3c3-1695-4f94-9796-5bf1e3fbc777",
//                    end_node_id = "12ce8487-dfa7-4baf-9c43-327b7b198b8b",
//                )
//            ),
//            sourcedFromRelationships = listOf(
//                SourcedFromRelationship(
//                    start_node_id = "34ecf13f-faa2-46e0-a7e7-aec1abe27ff0",
//                    end_node_id = "9fb38238-ab30-4489-b75a-2b084d15d9cd"
//                ),
//                SourcedFromRelationship(
//                    start_node_id = "3000b399-23e1-4df6-ae8e-280f7358beed",
//                    end_node_id = "9fb38238-ab30-4489-b75a-2b084d15d9cd"
//                )
//            ),
//            mentionsEventRelationships = listOf(
//                MentionsEventRelationship(
//                    start_node_id = "9fb38238-ab30-4489-b75a-2b084d15d9cd",
//                    end_node_id = "4861c163-8aac-4f72-a5e6-53eca0f2f477"
//                ),
//                MentionsEventRelationship(
//                    start_node_id = "9fb38238-ab30-4489-b75a-2b084d15d9cd",
//                    end_node_id = "3cb5e3c3-1695-4f94-9796-5bf1e3fbc777"
//                )
//            ),
//            occuredInRelationships = listOf(
//                OccuredInRelationship(
//                    start_node_id = "4861c163-8aac-4f72-a5e6-53eca0f2f477",
//                    end_node_id = "8fa78622-8a47-4567-8a9a-2d29b6130443"
//                ),
//            ),
//            mentionsLocationRelationships = null,
//            reasoning = "Mock relationships generated from the Rashford article example, connecting article, people, organisations, knowledge, and events per schema direction."
//        )
//        return mockExtractedRelationships

        return context.ai().withLlm(LlmOptions.withModel("gpt-oss:20b").withTemperature(0.3)).createObject(prompt, ExtractedRelationships::class.java)
    }

    @AchievesGoal(description = "Nodes and relationships have been extracted from the article and saved to the knowledge graph")
    @Action(description = "Extracts a knowledge graph and saves it to Neo4j")
    fun goal(extractedNodes: ExtractedNodes, extractedRelationships: ExtractedRelationships): FormattedExtraction {
        knowledgeGraphService.saveExtractedData(extractedNodes, extractedRelationships)
        logger.info("Successfully saved extracted nodes and relationships to Neo4j.")
        return FormattedExtraction(extractedNodes, extractedRelationships)
    }
}
