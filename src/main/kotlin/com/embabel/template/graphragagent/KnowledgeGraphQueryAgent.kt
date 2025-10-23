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
package com.embabel.template.graphragagent

import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.annotation.AchievesGoal
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.domain.io.UserInput
import com.embabel.template.tools.KnowledgeGraphTool

@Agent(description = "Answers questions about football players by querying the Neo4j knowledge graph")
class KnowledgeGraphQueryAgent(private val knowledgeGraphTool: KnowledgeGraphTool) {

    @AchievesGoal(description = "The user's question about the knowledge graph has been answered")
    @Action
    fun queryKnowledgeGraph(userInput: UserInput, context: OperationContext): String {
        val prompt = """
            You are a helpful assistant who can answer questions by using tools to query a knowledge graph.
            Use the available tools to answer the user's question.
            
            User Question: "${userInput.content}"
        """.trimIndent()

        return context.ai()
            .withDefaultLlm()
            .withToolObject(knowledgeGraphTool)
            .generateText(prompt)
    }
}