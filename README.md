![Build](https://github.com/embabel/embabel-agent/actions/workflows/maven.yml/badge.svg)

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Apache Tomcat](https://img.shields.io/badge/apache%20tomcat-%23F8DC75.svg?style=for-the-badge&logo=apache-tomcat&logoColor=black)
![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
![ChatGPT](https://img.shields.io/badge/chatGPT-74aa9c?style=for-the-badge&logo=openai&logoColor=white)
![JSON](https://img.shields.io/badge/JSON-000?logo=json&logoColor=fff)
![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)

<img align="left" src="https://github.com/embabel/embabel-agent/blob/main/embabel-agent-api/images/315px-Meister_der_Weltenchronik_001.jpg?raw=true" width="180">

&nbsp;&nbsp;&nbsp;&nbsp;

&nbsp;&nbsp;&nbsp;&nbsp;

# Knowledge-Graph RAG example

Built off of the Kotlin agent example repo from embabel.

This project consist of: 
- **The data harvester agent** That uses a modified version of the data harvester mcp (modified to be a FastAPI implementation) and harvests data using a user input query.
- **The Knowledege graph query agent** That injects the KnowledgeGraphTool class to answer a user query with the capability to check its knowledge graph db.
- **The KnowledgeGraphService class** That provides functions for querying the graph.
- **The KnowledgeGraphTool class** That injects the service class and allows the service functions to be used as tools in a standard embabel LLM call through context.ai().withToolObject.


# To run

Make sure to have a local model (ollama) specified as default llm in application.properties:

```bash
embabel.models.defaultLlm=qwen3:14b         # I use qwen3:14b but you can use whichever model you prefer
```

Install the neo4j desktop app and create an instance (just use the most recent version of neo4j),
Set your own username, password and make sure they are configured in the application.properties of this project:

```bash
spring.neo4j.uri=<your_uri>                 # You can find this in the neo4j desktop app in your created instance
spring.neo4j.authentication.username=<your_username>
spring.neo4j.authentication.password=<your_password>
```

***This project uses a mock data response modelled to match the data_harvester's result - no need to run anything else but an instance of a neo4j database.***


Build your maven project while in the same directory as the pom.xml:

```bash
mvn clean install
```

Run the spring-boot app:

```bash
mvn spring-boot:run
```

When the Embabel shell comes up, first use the data harvester agent like this:

```
x "harvester some data" #since the data is mock data the prompt only needs to relate to data harvesting so that it invokes the correct agent
```
This should populate your knowledge graph with some mock data.


Then use the knowledge-graph query agent like this:

```
x "tell me about cole palmers recent performance" 
```

You can also ask about related people and query how they relate to each other - have fun testing it out!
