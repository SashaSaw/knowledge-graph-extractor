
package com.embabel.template.service

import com.embabel.template.agent.ExtractedNodes
import com.embabel.template.agent.ExtractedRelationships
import com.embabel.template.agent.KnowledgeGraphExtractorAgent
import com.embabel.template.graph.model.*
import com.embabel.template.graph.repository.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class KnowledgeGraphService(
    private val personRepository: PersonRepository,
    private val organisationRepository: OrganisationRepository,
    private val locationRepository: LocationRepository,
    private val eventRepository: EventRepository,
    private val knowledgeRepository: KnowledgeRepository,
    private val articleRepository: ArticleRepository
) {
    private val logger = LoggerFactory.getLogger(KnowledgeGraphExtractorAgent::class.java)

    @Transactional
    fun saveExtractedData(extractedData: ExtractedNodes, extractedRelationships: ExtractedRelationships) {
        val idMap = mutableMapOf<String, BaseNode>()

        val article = articleRepository.save(
            Article(
                title = extractedData.article.title,
                url = extractedData.article.url,
                content = extractedData.article.content,
                language = extractedData.article.language,
                summary = extractedData.article.summary,
                publishDateTime = extractedData.article.publishDateTime,
                scrapeDateTime = extractedData.article.scrapeDateTime,
                agentProcessId = null, // This can be added later if needed
                sentiment = extractedData.article.sentiment
            )
        )
        idMap[extractedData.article.id as String] = article

        extractedData.people?.forEach { p ->
            val person = personRepository.findByFirstNameAndLastName(p.first_name, p.last_name) ?: Person(firstName = p.first_name, lastName = p.last_name)
            person.nicknames = p.nicknames?.filterNotNull()
            person.nationalities = p.nationalities?.filterNotNull()
            person.height = p.height
            person.weight = p.weight
            person.occupations = p.occupations?.filterNotNull()
            idMap[p.id as String] = personRepository.save(person)
        }

        extractedData.organisations?.forEach { o ->
            val org = organisationRepository.findByName(o.name) ?: Organisation(name = o.name)
            org.description = o.description
            idMap[o.id as String] = organisationRepository.save(org)
        }

        extractedData.locations?.forEach { l ->
            val loc = locationRepository.findByName(l.name) ?: Location(name = l.name)
            loc.number = l.number
            loc.street = l.street
            loc.city = l.city
            loc.country = l.country
            idMap[l.id as String] = locationRepository.save(loc)
        }

        extractedData.events?.forEach { e ->
            val event = Event(
                description = e.description,
                startDate = e.startDate,
                startTime = e.startTime,
                endDate = e.endDate,
                endTime = e.endTime,
                category = e.category,
                status = e.status,
                outcome = e.outcome,
                impact = e.impact
            )
            idMap[e.id as String] = eventRepository.save(event)
        }

        extractedData.knowledge?.forEach { k ->
            val knowledge = Knowledge(
                fact = k.fact,
                category = k.category,
                dateOfFact = k.dateOfFact
            )
            idMap[k.id as String] = knowledgeRepository.save(knowledge)
        }

        extractedRelationships.mentionsPersonReltionships?.forEach { rel ->
            val sourceNode = idMap[rel.start_node_id] as? Article
            logger.info("source node ${sourceNode?.id}, ${sourceNode?.title}")
            val targetNode = idMap[rel.end_node_id] as? Person
            logger.info("target node ${targetNode?.id}, ${targetNode?.firstName} ${targetNode?.lastName}")
            if (sourceNode != null && targetNode != null) {
                val evidence = rel.evidence
                val createdAt = Instant.now()

                val mentionRel = MentionsPerson(
                    person = targetNode,
                    evidence = evidence,
                    createdAt = createdAt,
                )

                // Safely append to the list (since Kotlin data classes are immutable)
                sourceNode.mentionsPeople = sourceNode.mentionsPeople + mentionRel
            }
        }

        extractedRelationships.aboutPersonRelationships?.forEach { rel ->
            val sourceNode = idMap[rel.start_node_id] as? Knowledge
            logger.info("source node ${sourceNode?.id}, ${sourceNode?.fact}")
            val targetNode = idMap[rel.end_node_id] as? Person
            logger.info("target node ${targetNode?.id}, ${targetNode?.firstName} ${targetNode?.lastName}")
            if (sourceNode != null && targetNode != null) {
                val createdAt = Instant.now()

                val aboutPersonRel = AboutPerson(
                    person = targetNode,
                    createdAt = createdAt,
                )

                // Safely append to the list (since Kotlin data classes are immutable)
                sourceNode.aboutPeople = sourceNode.aboutPeople + aboutPersonRel
            }
        }

        extractedRelationships.mentionsOrganisationRelationships?.forEach { rel ->
            val sourceNode = idMap[rel.start_node_id] as? Article
            val targetNode = idMap[rel.end_node_id] as? Organisation
            if (sourceNode != null && targetNode != null) {
                val mentionRel = MentionsOrganisation(
                    organisation = targetNode,
                    evidence = rel.evidence,
                    createdAt = Instant.now()
                )
                sourceNode.mentionsOrganisations = sourceNode.mentionsOrganisations + mentionRel
            }
        }

        extractedRelationships.aboutOrganisationRelationships?.forEach { rel ->
            val sourceNode = idMap[rel.start_node_id] as? Knowledge
            val targetNode = idMap[rel.end_node_id] as? Organisation
            if (sourceNode != null && targetNode != null) {
                val aboutRel = AboutOrganisation(
                    organisation = targetNode,
                    createdAt = Instant.now()
                )
                sourceNode.aboutOrganisations = sourceNode.aboutOrganisations + aboutRel
            }
        }

        extractedRelationships.sourcedFromRelationships?.forEach { rel ->
            val sourceNode = idMap[rel.start_node_id] as? Knowledge
            val targetNode = idMap[rel.end_node_id] as? Article
            if (sourceNode != null && targetNode != null) {
                val sourcedFromRel = SourcedFrom(
                    article = targetNode,
                    createdAt = Instant.now()
                )
                sourceNode.sourcedFrom = sourceNode.sourcedFrom + sourcedFromRel
            }
        }

        extractedRelationships.mentionsEventRelationships?.forEach { rel ->
            val sourceNode = idMap[rel.start_node_id] as? Article
            val targetNode = idMap[rel.end_node_id] as? Event
            if (sourceNode != null && targetNode != null) {
                val mentionsEventRel = MentionsEvent(
                    event = targetNode,
                    createdAt = Instant.now()
                )
                sourceNode.mentionsEvents = sourceNode.mentionsEvents + mentionsEventRel
            }
        }

        extractedRelationships.occuredInRelationships?.forEach { rel ->
            val sourceNode = idMap[rel.start_node_id] as? Event
            val targetNode = idMap[rel.end_node_id] as? Location
            if (sourceNode != null && targetNode != null) {
                val occuredInRel = OccuredIn(
                    location = targetNode,
                    createdAt = Instant.now()
                )
                sourceNode.occurredIn = sourceNode.occurredIn + occuredInRel
            }
        }

        extractedRelationships.mentionsLocationRelationships?.forEach { rel ->
            val sourceNode = idMap[rel.start_node_id] as? Article
            val targetNode = idMap[rel.end_node_id] as? Location
            if (sourceNode != null && targetNode != null) {
                val mentionsLocationRel = MentionsLocation(
                    location = targetNode,
                    createdAt = Instant.now()
                )
                sourceNode.mentionsLocations = sourceNode.mentionsLocations + mentionsLocationRel
            }
        }

        extractedRelationships.involvedPersonRelationships?.forEach { rel ->
            val sourceNode = idMap[rel.start_node_id] as? Event
            val targetNode = idMap[rel.end_node_id] as? Person
            if (sourceNode != null && targetNode != null) {
                val involvedPersonRel = InvolvedPerson(
                    person = targetNode,
                    createdAt = Instant.now()
                )
                sourceNode.involvedPeople = sourceNode.involvedPeople + involvedPersonRel
            }
        }

        extractedRelationships.involvedOrganisationRelationships?.forEach { rel ->
            val sourceNode = idMap[rel.start_node_id] as? Event
            val targetNode = idMap[rel.end_node_id] as? Organisation
            if (sourceNode != null && targetNode != null) {
                val involvedOrganisationRel = InvolvedOrganisation(
                    organisation = targetNode,
                    createdAt = Instant.now()
                )
                sourceNode.involvedOrganisations = sourceNode.involvedOrganisations + involvedOrganisationRel
            }
        }

        idMap.values.forEach { node ->
            when (node) {
                is Person -> personRepository.save(node)
                is Organisation -> organisationRepository.save(node)
                is Location -> locationRepository.save(node)
                is Article -> articleRepository.save(node)
                is Knowledge -> knowledgeRepository.save(node)
                is Event -> eventRepository.save(node)
            }
        }
    }
}
