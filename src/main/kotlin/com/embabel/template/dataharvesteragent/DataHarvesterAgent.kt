/*
 * Copyright 2024-2025 Embabel Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.embabel.template.dataharvesteragent

import com.embabel.agent.api.annotation.AchievesGoal
import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.domain.io.UserInput
import com.embabel.agent.domain.library.HasContent
import com.embabel.common.core.types.Timestamped
import org.springframework.context.annotation.Profile

import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

val article1 = Node.Article(
    title = "Villa signing Rashford loses three sponsorship deals",
    URL = "https://www.tribalfootball.com/article/soccer-premier-league-villa-signing-rashford-loses-three-sponsorship-deals-6d925766-53e5-42b5-a1e2-357f9159d385",
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
    publish_datetime = LocalDateTime.of(2025, 2, 5, 17, 30),
    scrape_datetime = LocalDateTime.now(),
    sentiment = "negative"
)

val article2 = Node.Article(
    title = "Manchester United’s Marcus Rashford Faces Disciplinary Action for Night Out Amidst Illness Controversy",
    URL = "https://www.tribalfootball.com/article/manchester-uniteds-marcus-rashford-faces-disciplinary-action-for-night-out-amidst-illness-controversy", // assumed source URL format
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
    publish_datetime = LocalDateTime.of(2023, 1, 29, 12, 0), // estimated from “2 years ago” relative to 2025
    scrape_datetime = LocalDateTime.now(),
    sentiment = "negative"
)

val article3 = Node.Article(
    title = "Another huge scandal, Marcus Rashford will pay for this after his indiscipline",
    URL = "https://www.elfutbolero.com/soccer/another-huge-scandal-marcus-rashford-will-pay-for-this-after-his-indiscipline-20240129", // inferred plausible source URL
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
    publish_datetime = LocalDateTime.of(2024, 1, 29, 14, 52), // From '29/01/2024, 02:52 PM +00:00'
    scrape_datetime = LocalDateTime.now(),
    sentiment = "negative"
)

sealed interface FormattedVerificationResult : HasContent {
}

data class ExtractedNodes(
    val article: Node.Article,
    val people: List<Node.Person>,
    val organisations: List<Node.Organisation>,
    val knowledge_points: List<Node.Knowledge>,
    val locations: List<Node.Location>,
    val events: List<Node.Event>,
)

data class ExtractedResult(
    val extracted_nodes: ExtractedNodes
) : FormattedVerificationResult {

    override val content: String
        get() = """
            # Article
            
            ${extracted_nodes.article.title}
            ${extracted_nodes.article.content}
            
            # People
            
            ${extracted_nodes.people.joinToString("\n")}
            
            # Organisations
            
            ${extracted_nodes.organisations.joinToString("\n")}
            
            # Knowledge
            
            ${extracted_nodes.knowledge_points.joinToString("\n")}
            
            # Locations
            
            ${extracted_nodes.locations.joinToString("\n")}
            
            # Events
            
            ${extracted_nodes.events.joinToString("\n")}
            
        """.trimIndent()
}

@Agent(
    description = "Extracts knowledge graph nodes and relationships from a ",
)
@Profile("!test")
class DataHarvesterAgent() {
    private val logger = LoggerFactory.getLogger(DataHarvesterAgent::class.java)

    @Action
    fun createKnowledgeGraph(userInput: UserInput, context: OperationContext): ExtractedNodes {
        val prompt = """You are an intelligent information extraction model designed to populate a football knowledge graph.
        
        ### ARTICLE
        Title: ${article1.title}
        Content: ${article1.content}
        
        ### OBJECTIVE
        From the given article text, identify all people mentioned, all facts, all locations and all events and extract any information that you can find about them.
        
        every person json MUST have the property node_type = "person"
        every organisation json MUST have the property node_type = "organisation"
        every knowledge_point json MUST have the property node_type = "knowledge"
        every locations json MUST have the property node_type = "locations"
        every event json MUST have the property node_type = "event"
        
        all people
        """.trimIndent()
        return context.ai()
            .withDefaultLlm()
            .createObject(prompt, ExtractedNodes::class.java)
    }

    @AchievesGoal(description = "Nodes and relationships have been extracted from article")
    @Action(description = "Extracts a knowledge graph")
    fun goal(extracted_nodes: ExtractedNodes): ExtractedResult {
        return ExtractedResult(extracted_nodes)
    }

}
