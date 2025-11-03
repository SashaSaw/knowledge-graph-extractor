
package com.embabel.template.service

import com.embabel.template.agent.ExtractedNodes
import com.embabel.template.graph.model.*
import com.embabel.template.graph.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class KnowledgeGraphService(
    private val personRepository: PersonRepository,
    private val organisationRepository: OrganisationRepository,
    private val locationRepository: LocationRepository,
    private val eventRepository: EventRepository,
    private val knowledgeRepository: KnowledgeRepository,
    private val articleRepository: ArticleRepository
) {

    @Transactional
    fun saveExtractedData(extractedData: ExtractedNodes) {
        val idMap = mutableMapOf<String, BaseNode>()

        val article = articleRepository.save(
            Article(
                title = extractedData.article.title ?: "Untitled",
                url = extractedData.article.URL,
                content = extractedData.article.content ?: "",
                language = extractedData.article.language,
                summary = extractedData.article.summary,
                publishDateTime = extractedData.article.publish_datetime,
                scrapeDateTime = extractedData.article.scrape_datetime,
                agentProcessId = null, // This can be added later if needed
                sentiment = extractedData.article.sentiment
            )
        )
        idMap[extractedData.article.id] = article

        extractedData.people.forEach { p ->
            val name = "${p.first_name ?: ""} ${p.surname ?: ""}".trim()
            if (name.isNotEmpty()) {
                val person = personRepository.findByName(name) ?: Person(name = name, gender = p.sex)
                person.nicknames = p.other_names
                person.nationalities = p.nationalities
                person.height = p.height
                person.weight = p.weight
                person.occupations = p.occupations
                idMap[p.id] = personRepository.save(person)
            }
        }

        extractedData.organisations.forEach { o ->
            o.name?.let { name ->
                val org = organisationRepository.findByName(name) ?: Organisation(name = name)
                org.description = o.description
                idMap[o.id] = organisationRepository.save(org)
            }
        }

        extractedData.locations.forEach { l ->
            l.name?.let { name ->
                val loc = locationRepository.findByName(name) ?: Location(name = name)
                loc.number = l.number?.toString()
                loc.street = l.street
                loc.city = l.city
                loc.country = l.country
                idMap[l.id] = locationRepository.save(loc)
            }
        }

        extractedData.events.forEach { e ->
            e.description?.let { description ->
                val event = Event(
                    description = description,
                    startDate = e.started_at?.toLocalDate(),
                    startTime = e.started_at?.toLocalTime(),
                    endDate = e.finished_at?.toLocalDate(),
                    endTime = e.finished_at?.toLocalTime(),
                    category = e.category,
                    status = null,
                    outcome = null,
                    impact = null
                )
                idMap[e.id] = eventRepository.save(event)
            }
        }

        extractedData.knowledge_points.forEach { k ->
            k.fact?.let { fact ->
                val knowledge = Knowledge(
                    fact = fact,
                    category = k.topic,
                    dateOfFact = null
                )
                idMap[k.id] = knowledgeRepository.save(knowledge)
            }
        }

        extractedData.relationships.forEach { rel ->
            val sourceNode = idMap[rel.source_id]
            val targetNode = idMap[rel.end_node_id]

            if (sourceNode != null && targetNode != null) {
                when (sourceNode) {
                    is Person -> {
                        sourceNode.relations.add(targetNode)
                        personRepository.save(sourceNode)
                    }
                    is Organisation -> {
                        sourceNode.relations.add(targetNode)
                        organisationRepository.save(sourceNode)
                    }
                    is Location -> {
                        sourceNode.relations.add(targetNode)
                        locationRepository.save(sourceNode)
                    }
                    is Event -> {
                        sourceNode.relations.add(targetNode)
                        eventRepository.save(sourceNode)
                    }
                    is Knowledge -> {
                        sourceNode.relations.add(targetNode)
                        knowledgeRepository.save(sourceNode)
                    }
                    is Article -> {
                        sourceNode.mentions.add(targetNode)
                        articleRepository.save(sourceNode)
                    }
                }
            }
        }
    }
}
