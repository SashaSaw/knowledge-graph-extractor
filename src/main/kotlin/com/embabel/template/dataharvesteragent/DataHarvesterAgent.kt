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
import com.embabel.agent.api.annotation.Export
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.domain.io.UserInput
import com.embabel.agent.domain.library.HasContent
import com.embabel.common.core.types.Timestamped
import com.embabel.template.services.KnowledgeGraphService
import org.springframework.context.annotation.Profile
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.web.client.RestTemplate

data class HarvestRequest(
    val query: String,
    @JsonProperty("result_size") val resultSize: Int = 10,
    @JsonProperty("scrape_content") val scrapeContent: Boolean = true,
    @JsonProperty("max_concurrent") val maxConcurrent: Int = 20,
    @JsonProperty("output_dir") val outputDir: String = "output",
    @JsonProperty("extraction_strategy") val extractionStrategy: String? = "article",
    @JsonProperty("extraction_prompt") val extractionPrompt: String? = null,
    @JsonProperty("search_filter") val searchFilter: String? = null
)

data class HarvestResponse(
    val success: Boolean,
    val query: String,
    @JsonProperty("urls_found") val urlsFound: Int,
    @JsonProperty("urls_scraped") val urlsScraped: Int,
    val error: String?,
    val results: Results?
)

data class Results(
    @JsonProperty("search_results") val searchResults: SearchResults,
    @JsonProperty("scraped_content") val scrapedContent: List<ScrapedContent>
)

data class SearchResults(
    val query: String,
    val service: String,
    val urls: List<UrlResult>,
    @JsonProperty("related_searches") val relatedSearches: List<RelatedSearch>
)

data class UrlResult(
    val url: String,
    val title: String,
    val snippet: String,
    val position: Int,
    val source: String
)

data class RelatedSearch(
    val query: String,
    val source: String
)

data class ScrapedContent(
    val title: String,
    val snippet: String,
    @JsonProperty("extracted_content") val extractedContent: JsonNode
)

data class ExtractedContent(
    val title: String? = null,
    val summary: String? = null,
    val url: String? = null,
    val error: Boolean? = null,
    val index: Int? = null,
    val tags: List<String>? = null,
    val content: String? = null
)

data class Person(
    val name: String,
    val description: String? = null,
)

data class Article(
    val title: String,
    val summary: String,
    val url: String,
    val snippet: String,
    val index: Int? = null,
    val tags: List<String>? = null,
    val content: String? = null,
    val keyPerson: Person? = null,
    val mentionedPeople: List<Person>? = null,
)

data class PeopleInArticle(
    val subjectPerson: Person?,
    val otherPeople: List<Person>?
)

data class HarvestedData(
    val data: HarvestResponse,
) : HasContent, Timestamped {

    override val timestamp: Instant
        get() = Instant.now()

    override val content: String
        get() = """
            ${data}
            
        ${timestamp.atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"))}
        """.trimIndent()
}


@Agent(
    description = "Only harvests data and does not answer user questions",
)
@Profile("!test")
class DataHarvesterAgent(private val knowledgeGraphService: KnowledgeGraphService) {
    private val logger = LoggerFactory.getLogger(DataHarvesterAgent::class.java)

    // Helper function to create the JSON structure for scraped content
    fun createScrapedContent(
        articleTitle: String,
        articleSnippet: String,
        extractedContent: ExtractedContent
    ): ScrapedContent {
        val objectMapper = jacksonObjectMapper()

        return ScrapedContent(
            title = articleTitle,
            snippet = articleSnippet,
            extractedContent = objectMapper.valueToTree(listOf(extractedContent))
        )
    }

    // --- Article 1: Hat-trick performance ---
    val article1 = ExtractedContent(
        title = "Palmer's Stunning Hat-Trick Sinks Rivals",
        summary = "Cole Palmer delivered a masterclass in finishing with a stunning hat-trick, securing a dramatic late victory for Chelsea.",
        url = "https://mock-sports-news.com/palmer-hat-trick-chelsea-win",
        error = false,
        index = 0,
        tags = listOf("chelsea", "hat-trick", "clutch"),
        content = "In a performance for the ages, Cole Palmer demonstrated his immense talent and composure. After scoring twice earlier in the match, he converted a last-minute penalty to complete his hat-trick. Manager Mauricio Pochettino praised his mentality, highlighting his seamless integration into the team since arriving from Manchester City."
    )

    // --- Article 2: Penalty King ---
    val article2 = ExtractedContent(
        title = "The Iceman: Palmer's Perfect Penalty Record",
        summary = "Once again, Cole Palmer proved to be Chelsea's most reliable player from the penalty spot, maintaining his perfect record for the season.",
        url = "https://mock-football-stats.com/palmer-penalty-record-2024",
        error = false,
        index = 1,
        tags = listOf("penalties", "statistics", "chelsea"),
        content = "When the pressure is on, Cole Palmer is the man for the occasion. He coolly dispatched another penalty, sending the keeper the wrong way. Teammates like Enzo Fernández have noted his calm demeanor in training, stating that they always have confidence when he steps up to the spot. His technique is already being compared to some of the league's best."
    )

    // --- Article 3: Link-up Play ---
    val article3 = ExtractedContent(
        title = "Palmer and Sterling Forge Deadly Partnership",
        summary = "The connection between Cole Palmer and Raheem Sterling is quickly becoming one of Chelsea's most potent attacking weapons.",
        url = "https://mock-tactics-weekly.com/palmer-sterling-partnership",
        error = false,
        index = 2,
        tags = listOf("tactics", "partnership", "sterling"),
        content = "On the pitch, the chemistry between Cole Palmer and veteran forward Raheem Sterling is undeniable. Palmer's clever passes and Sterling's intelligent runs have resulted in numerous chances. This partnership, often involving striker Nicolas Jackson, provides a dynamic threat that has been crucial for breaking down stubborn defenses."
    )

    // --- Article 4: Impact Since Transfer ---
    val article4 = ExtractedContent(
        title = "Signing of the Season? Palmer's Impact at Chelsea",
        summary = "Few expected Cole Palmer to have such a monumental impact after his transfer from Manchester City, but he has quickly become Chelsea's talisman.",
        url = "https://mock-transfer-analysis.com/palmer-signing-of-the-season",
        error = false,
        index = 3,
        tags = listOf("transfer", "analysis", "manchester-city"),
        content = "When Cole Palmer left Manchester City, some questioned the move. However, he has silenced all doubters by becoming the focal point of Chelsea's attack. While Pep Guardiola had a wealth of talent, Palmer has thrived with the consistent playing time offered at his new club, proving to be one of the most effective transfers of the year."
    )

    // --- Article 5: Season in Review ---
    val article5 = ExtractedContent(
        title = "By the Numbers: A Sensational Debut Season for Palmer",
        summary = "A statistical look at Cole Palmer's incredible debut season, where he has exceeded all expectations in goals, assists, and overall contribution.",
        url = "https://mock-data-focus.com/palmer-season-stats-2024",
        error = false,
        index = 4,
        tags = listOf("stats", "performance", "debut-season"),
        content = "Cole Palmer's statistics speak for themselves: over 20 goal contributions in his first season with Chelsea. His performance metrics place him in the elite category, alongside players like Bukayo Saka and Phil Foden. His consistency has been a bright spot, making him a clear candidate for the league's Young Player of the Year award."
    )

    private fun getMockHarvestResponse(): HarvestResponse {

        // --- Assemble the Final HarvestResponse ---
        return HarvestResponse(
            success = true,
            query = "Cole Palmer recent performance",
            urlsFound = 5,
            urlsScraped = 5,
            error = null,
            results = Results(
                searchResults = SearchResults(
                    query = "Cole Palmer recent performance",
                    service = "mock_service",
                    urls = listOf(
                        UrlResult("https://mock-sports-news.com/palmer-hat-trick-chelsea-win", "Palmer's Stunning Hat-Trick", "...", 1, "mock"),
                        UrlResult("https://mock-football-stats.com/palmer-penalty-record-2024", "The Iceman: Palmer's Perfect Penalty Record", "...", 2, "mock"),
                        UrlResult("https://mock-tactics-weekly.com/palmer-sterling-partnership", "Palmer and Sterling Forge Deadly Partnership", "...", 3, "mock"),
                        UrlResult("https://mock-transfer-analysis.com/palmer-signing-of-the-season", "Signing of the Season? Palmer's Impact", "...", 4, "mock"),
                        UrlResult("https://mock-data-focus.com/palmer-season-stats-2024", "By the Numbers: A Sensational Debut Season", "...", 5, "mock")
                    ),
                    relatedSearches = emptyList()
                ),
                scrapedContent = listOf(
                    createScrapedContent("Palmer's Stunning Hat-Trick", "...", article1),
                    createScrapedContent("The Iceman: Palmer's Perfect Penalty Record", "...", article2),
                    createScrapedContent("Palmer and Sterling Forge Deadly Partnership", "...", article3),
                    createScrapedContent("Signing of the Season? Palmer's Impact", "...", article4),
                    createScrapedContent("By the Numbers: A Sensational Debut Season", "...", article5)
                )
            )
        )
    }


    @Action
    fun harvestData(userInput: UserInput, context: OperationContext): HarvestResponse? {
        return getMockHarvestResponse()
    }

    private fun getArticles(response: HarvestResponse, context: OperationContext): List<Article> {
        val objectMapper = jacksonObjectMapper()
        val listType = object : TypeReference<List<ExtractedContent>>() {} //create an anonymous subclass of type List<ExtractedContent>
        return response.results?.scrapedContent?.mapNotNull { scrapedContent ->
            val extractedContentList: List<ExtractedContent> = if (scrapedContent.extractedContent.isTextual) {
                // It's a string. Let's see if it's JSON string.
                try {
                    // If it's a JSON string extract the data as an ExtractedContent object (extractedContentList should be a list of one ExtractedContentObject)
                    logger.info("JSON string content found: ${scrapedContent.extractedContent.asText()}")
                    objectMapper.readValue(scrapedContent.extractedContent.asText(), listType)
                } catch (e: Exception) {
                    // Not a JSON string, treat as plain text content
                    logger.info("Non-JSON string content found: ${scrapedContent.extractedContent.asText()}")
                    listOf(ExtractedContent(content=scrapedContent.extractedContent.asText()))
                }
            } else if (scrapedContent.extractedContent.isArray) {
                logger.info("Array content found: ${scrapedContent.extractedContent}")
                objectMapper.convertValue(scrapedContent.extractedContent, listType)
            } else {
                logger.info("No text found!")
                emptyList()
            }

            // Find the first extracted content without an error
            val validContent = extractedContentList.firstOrNull { it.error != true }

            validContent?.let {
                val peopleInArticle = it.content?.let { content ->
                    context.ai()
                        .withDefaultLlm()
                        .createObject(
                            """
                        Here is an article.
                        Please identify the person who is the subject of the article and generate a description of them
                        Article:
                        $content
                        """.trimIndent(),
                            PeopleInArticle::class.java
                        )
                }

                Article(
                    title = it.title ?: scrapedContent.title,
                    summary = it.summary ?: "No summary available",
                    url = it.url ?: "N/A",
                    snippet = scrapedContent.snippet?: "No snippet available",
                    index = it.index,
                    tags = it.tags,
                    content = it.content ?: "No content available",
                    keyPerson = peopleInArticle?.subjectPerson,
                    mentionedPeople = peopleInArticle?.otherPeople
                )
            }
        } ?: emptyList()
    }

    @AchievesGoal(
        description = "The user data harvest has been completed",
        export = Export(remote = true, name = "harvestAndReport")
    )
    @Action
    fun reportResult(userInput: UserInput, harvestResponse: HarvestResponse, context: OperationContext): HarvestedData {
        val articles = getArticles(harvestResponse, context)
        knowledgeGraphService.addToKnowledgeGraph(userInput.content, articles)
        return HarvestedData(
            data = harvestResponse
        )
    }

}
