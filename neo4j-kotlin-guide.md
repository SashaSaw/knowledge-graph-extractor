# Neo4j Knowledge Graph Database Guide for Kotlin

A comprehensive guide to implementing Neo4j graph databases in Kotlin, with simple explanations, analogies, and practical examples.

---

## Table of Contents
1. [Understanding Transactions](#understanding-transactions)
2. [Core Architecture](#core-architecture)
3. [Three Ways to Run Transactions](#three-ways-to-run-transactions)
4. [Sessions Explained](#sessions-explained)
5. [Processing Query Results](#processing-query-results)
6. [Database URI vs Database Name](#database-uri-vs-database-name)
7. [Best Practices](#best-practices)
8. [Common Gotchas](#common-gotchas)
9. [Complete Code Examples](#complete-code-examples)

---

## Understanding Transactions

### What is a Transaction?

**Analogy:** Think of a transaction like a shopping cart checkout:
- You add multiple items to your cart (multiple database operations)
- When you hit "Purchase," either ALL items are bought, or NONE are (if your card declines)
- You can't have half the items purchased and half not - it's all-or-nothing

In Neo4j, a transaction ensures that multiple database changes either **all succeed together** or **all fail together**. No half-finished work!

### Key Properties (ACID)
- **Atomic:** All operations succeed or all fail
- **Consistent:** Database remains in a valid state
- **Isolated:** Transactions don't interfere with each other
- **Durable:** Committed changes are permanent

---

## Core Architecture

### The Connection Flow

```
1. Create Driver with URI + Auth (ONCE)
         ↓
2. Driver connects to server
         ↓
3. Create Sessions from Driver (as needed)
         ↓
4. Run Transactions in Session
         ↓
5. Execute Queries in Transaction
```

### Components Explained

| Component | Purpose | Analogy |
|-----------|---------|---------|
| **Driver** | Connection to Neo4j server | The main entrance to an apartment building |
| **Session** | Query channel to database | A phone line to the database |
| **Transaction** | Unit of work (all-or-nothing) | A shopping cart checkout |
| **Query** | Individual Cypher statement | A single item in your cart |

---

## Three Ways to Run Transactions

### 1. Auto Transactions (The Easy Way) 🚗

**When to use:** Single, simple queries

**Analogy:** Like using a taxi - you just say where you want to go, and the driver handles everything.

```kotlin
val result = driver.executableQuery("MATCH (p:Person) RETURN p")
    .withConfig(QueryConfig.builder().withDatabase("neo4j").build())
    .execute()
```

**Pros:**
- Simplest syntax
- Automatic transaction handling

**Cons:**
- Only one query at a time
- No custom logic between queries

---

### 2. Managed Transactions (The Recommended Way) 🎫

**When to use:** Multiple queries with application logic, automatic retries needed

**Analogy:** Like a train journey where:
- The conductor (Neo4j driver) handles the route and retries if there's a track problem
- You decide what to do at each stop (run queries, process results)
- If something goes wrong, the conductor automatically takes you back to the start

```kotlin
driver.session(
    SessionConfig.builder()
        .withDatabase("neo4j")
        .build()
).use { session ->
    val people = session.executeRead { tx ->
        val result = tx.run(
            """
            MATCH (p:Person) WHERE p.name STARTS WITH ${'$'}filter
            RETURN p.name AS name ORDER BY name
            """,
            mapOf("filter" to "Al")
        )
        result.list()
    }
    
    people.forEach { person ->
        println(person)
    }
}
```

**Key Methods:**
- `executeRead()` - For queries that only read data (like visiting a library)
- `executeWrite()` - For queries that modify data (like editing a document)

**Pros:**
- Automatic retry on transient failures
- Clean error handling
- Best for most use cases

**Cons:**
- Transaction function must be idempotent (safe to run multiple times)

---

### 3. Explicit Transactions (Full Control) 🎮

**When to use:** External API interactions, custom retry logic, distributed logic across functions

**Analogy:** Like driving a manual car - you control the clutch, gears, everything. More power, more responsibility.

```kotlin
driver.session(
    SessionConfig.builder()
        .withDatabase("neo4j")
        .build()
).use { session ->
    session.beginTransaction().use { tx ->
        // Check balance
        val hasEnough = customerBalanceCheck(tx, customerId, amount)
        
        if (!hasEnough) {
            println("Insufficient funds")
            return  // Transaction auto-rolls back
        }
        
        // Call external API (CAN'T UNDO THIS!)
        externalBankTransfer(customerId, amount)
        
        try {
            // Update our database
            decreaseCustomerBalance(tx, customerId, amount)
            tx.commit()  // Explicitly commit
        } catch (e: Exception) {
            // tx.rollback() is automatic if not committed
            logForManualInspection(e)
            throw e
        }
    }
}
```

**Pros:**
- Full control over commit/rollback
- Can interact with external services
- No automatic retries (good when you don't want them)

**Cons:**
- More complex
- Must manually handle commit/rollback
- External API calls can't be rolled back

---

## Sessions Explained

### What is a Session?

**Analogy:** Sessions are like phone lines to the database:
- Each thread needs its own phone line (session)
- You can make multiple calls (transactions) on one line
- Always hang up when done (close the session)

### Creating Sessions

```kotlin
// Always use .use { } to auto-close!
driver.session(
    SessionConfig.builder()
        .withDatabase("neo4j")
        .build()
).use { session ->
    // Use session here
}  // Automatically closed when done
```

### Session Configuration Options

```kotlin
driver.session(
    SessionConfig.builder()
        .withDatabase("products")              // Which database
        .withDefaultAccessMode(AccessMode.READ) // Read or write mode
        .withImpersonatedUser("analyst")        // Run as different user
        .build()
)
```

### Important Session Rules

1. **Always use `.use { }`** - It automatically closes the session
2. **Never share sessions across threads** - Create a new session per thread
3. **Sessions are cheap** - Don't worry about creating many
4. **One active transaction per session** - Can't run concurrent transactions in one session

---

## Processing Query Results

### The Streaming Nature of Results

**Analogy:** Results are like streaming video - you can't rewind!

```kotlin
val result = tx.run("MATCH (p:Person) RETURN p")

// Records come as a stream
// Once consumed, you can't go back!
```

### Common Result Methods

```kotlin
// Get all results as a list
val allPeople = result.list()

// Get exactly one result (throws if 0 or 2+)
val person = result.single()

// Get next result
val nextPerson = result.next()

// Check if more results available
if (result.hasNext()) {
    val person = result.next()
}

// Peek at next without consuming
val preview = result.peek()

// Consume remaining results and get summary
val summary = result.consume()
```

### The `consume()` Method

**Analogy:** Like hitting "End Video" on a stream:
1. Throws away any remaining records
2. Returns a summary of what happened
3. Marks the result as exhausted

```kotlin
val result = tx.run("CREATE (p:Person {name: 'Alice'})")
val summary = result.consume()

// What's in the summary?
println("Query took: ${summary.resultAvailableAfter()} ms")
println("Nodes created: ${summary.counters().nodesCreated()}")
println("Properties set: ${summary.counters().propertiesSet()}")
println("Query type: ${summary.queryType()}")
```

**When to use `consume()`:**
- ✅ You don't need the actual data, just statistics
- ✅ You've finished processing and want performance metrics
- ❌ Never call it before processing the data you need!

### Extracting Values from Records

```kotlin
val result = tx.run("MATCH (p:Person) RETURN p.name AS name, p.age AS age")

result.list().forEach { record ->
    val name = record.get("name").asString()
    val age = record.get("age").asInt()
    println("$name is $age years old")
}
```

**Available type conversions:**
- `.asString()`
- `.asInt()`
- `.asLong()`
- `.asBoolean()`
- `.asDouble()`
- `.asList()`
- `.asMap()`

---

## Database URI vs Database Name

### Understanding the Difference

**Analogy:**
- **URI** = "123 Apartment Building Street" (the building location)
- **Database Name** = "Apartment 4B" (which unit in the building)

### The URI (Where the Server Is)

```kotlin
// Local database
"bolt://localhost:7687"

// Remote database  
"bolt://myserver.example.com:7687"

// Secure connection
"neo4j+s://myserver.example.com:7687"

// Neo4j AuraDB (cloud)
"neo4j+s://xxxxx.databases.neo4j.io"
```

**URI is specified ONCE when creating the driver:**

```kotlin
val driver = GraphDatabase.driver(
    "bolt://localhost:7687",              // ← WHERE the server is
    AuthTokens.basic("neo4j", "password")  // ← WHO you are
)
```

### The Database Name (Which Database on That Server)

One Neo4j server can host multiple databases:

```kotlin
val driver = GraphDatabase.driver(uri, auth)

// Access the "customers" database
driver.session(
    SessionConfig.builder()
        .withDatabase("customers")  // ← Database NAME
        .build()
)

// Access the "products" database
driver.session(
    SessionConfig.builder()
        .withDatabase("products")  // ← Different database
        .build()
)
```

### Why Specify Database in Session?

**Reason 1: Multiple Databases**

```kotlin
// One server, three separate databases
val driver = GraphDatabase.driver("bolt://localhost:7687", auth)

// Session 1: Customer data
driver.session(SessionConfig.builder().withDatabase("customers").build()).use {
    // Alice exists only in "customers" database
}

// Session 2: Product data  
driver.session(SessionConfig.builder().withDatabase("products").build()).use {
    // Widget exists only in "products" database
}

// Session 3: Audit logs
driver.session(SessionConfig.builder().withDatabase("audit_logs").build()).use {
    // Log entries exist only in "audit_logs" database
}
```

**Reason 2: Performance**

```kotlin
// WITHOUT specifying database - SLOWER
driver.session().use { session ->
    // Driver asks server: "What's the default database?"
    // Server responds: "It's 'neo4j'"
    // Then your query runs
}

// WITH specifying database - FASTER
driver.session(
    SessionConfig.builder().withDatabase("neo4j").build()
).use { session ->
    // Driver already knows which database
    // Query runs immediately - saves a network round-trip!
}
```

### Complete Flow Diagram

```
Neo4j Server (bolt://localhost:7687)
├─ AuthTokens verify you can enter
│
├─ Database: "neo4j" (default)
│  ├─ Nodes: (:User), (:Admin)
│  └─ Relationships: [:MANAGES]
│
├─ Database: "customers"
│  ├─ Nodes: (:Customer), (:Order)
│  └─ Relationships: [:PURCHASED]
│
└─ Database: "products"
   ├─ Nodes: (:Product), (:Category)
   └─ Relationships: [:BELONGS_TO]
```

---

## Best Practices

### 1. Always Use `.use { }` for Resource Management

```kotlin
// ✅ GOOD - Auto-closes
driver.use { driver ->
    driver.session(config).use { session ->
        // Work with session
    }
}

// ❌ BAD - Must remember to close manually
val session = driver.session(config)
// ... use session ...
session.close()  // Easy to forget!
```

### 2. Use Query Parameters (NEVER String Concatenation)

```kotlin
// ✅ GOOD - Safe and efficient
tx.run(
    "MATCH (p:Person {name: ${'$'}name}) RETURN p",
    mapOf("name" to userName)
)

// ❌ BAD - SQL injection risk, slower performance
tx.run("MATCH (p:Person {name: '$userName'}) RETURN p")
```

### 3. Always Specify Database Name

```kotlin
// ✅ GOOD - Faster, explicit
driver.session(
    SessionConfig.builder()
        .withDatabase("neo4j")
        .build()
)

// ❌ ACCEPTABLE BUT SLOWER
driver.session()  // Has to ask server for default DB
```

### 4. Process Results Inside Transaction Functions

```kotlin
// ✅ GOOD
session.executeRead { tx ->
    val result = tx.run("MATCH (p:Person) RETURN p")
    result.list()  // Process here
}

// ❌ BAD
session.executeRead { tx ->
    val result = tx.run("MATCH (p:Person) RETURN p")
    result  // Don't return raw Result!
}
```

### 5. Use Managed Transactions by Default

```kotlin
// ✅ GOOD - Use this for most cases
session.executeWrite { tx ->
    tx.run("CREATE (p:Person {name: ${'$'}name})", mapOf("name" to name))
    // Automatic retry on transient failures
}

// ⚠️ USE SPARINGLY - Only when you need full control
session.beginTransaction().use { tx ->
    // Manual control needed
    tx.commit()
}
```

### 6. Make Transaction Functions Idempotent

Since managed transactions can retry, ensure your code is safe to run multiple times:

```kotlin
// ✅ GOOD - Idempotent (MERGE instead of CREATE)
session.executeWrite { tx ->
    tx.run("MERGE (p:Person {id: ${'$'}id})", mapOf("id" to userId))
}

// ⚠️ RISKY - Not idempotent (multiple retries = duplicate nodes)
session.executeWrite { tx ->
    tx.run("CREATE (p:Person {name: ${'$'}name})", mapOf("name" to name))
}
```

### 7. One Driver per Application

```kotlin
// ✅ GOOD - Create once, reuse everywhere
object DatabaseConfig {
    val driver: Driver by lazy {
        GraphDatabase.driver(
            "bolt://localhost:7687",
            AuthTokens.basic("neo4j", "password")
        )
    }
}

// Use across your application
fun queryData() {
    DatabaseConfig.driver.session(config).use { session ->
        // ...
    }
}
```

### 8. Configure Transaction Timeouts

```kotlin
session.executeRead({ tx ->
    tx.run("MATCH (p:Person) RETURN p").list()
}, TransactionConfig.builder()
    .withTimeout(Duration.ofSeconds(30))
    .withMetadata(mapOf("appName" to "myApp"))
    .build()
)
```

---

## Common Gotchas

### ❌ Gotcha #1: Sharing Sessions Across Threads

```kotlin
// ❌ BAD - Session not thread-safe!
val session = driver.session(config)
launch { session.executeRead { /* ... */ } }
launch { session.executeRead { /* ... */ } }

// ✅ GOOD - Each thread gets its own session
launch {
    driver.session(config).use { session ->
        session.executeRead { /* ... */ }
    }
}
launch {
    driver.session(config).use { session ->
        session.executeRead { /* ... */ }
    }
}
```

### ❌ Gotcha #2: Trying to Rewind Results

```kotlin
val result = tx.run("MATCH (p:Person) RETURN p")

val first = result.list()
val second = result.list()  // ❌ ERROR - Result already consumed!

// ✅ SOLUTION - Save results if you need them multiple times
val people = result.list()
val first = people.take(10)
val second = people.drop(10)
```

### ❌ Gotcha #3: External APIs in Managed Transactions

```kotlin
// ❌ BAD - API might be called multiple times on retry!
session.executeWrite { tx ->
    sendEmail(customer)  // Could send duplicate emails!
    tx.run("CREATE (p:Person {name: ${'$'}name})", params)
}

// ✅ GOOD - Use explicit transaction for external APIs
session.beginTransaction().use { tx ->
    val result = tx.run("MATCH (c:Customer) RETURN c")
    if (result.hasNext()) {
        sendEmail(customer)  // Only called once
        tx.commit()
    }
}
```

### ❌ Gotcha #4: Forgetting to Close Resources

```kotlin
// ❌ BAD - Leaks connections!
for (i in 1..1000) {
    val session = driver.session(config)
    session.executeRead { /* ... */ }
    // Forgot to close!
}

// ✅ GOOD - Always use .use { }
for (i in 1..1000) {
    driver.session(config).use { session ->
        session.executeRead { /* ... */ }
    }  // Auto-closed
}
```

### ❌ Gotcha #5: Multiple Active Transactions in One Session

```kotlin
// ❌ BAD - Can't have concurrent transactions in one session
session.beginTransaction().use { tx1 ->
    session.beginTransaction().use { tx2 ->  // ERROR!
        // ...
    }
}

// ✅ GOOD - Use multiple sessions for concurrent transactions
driver.session(config).use { session1 ->
    session1.beginTransaction().use { tx1 -> /* ... */ }
}
driver.session(config).use { session2 ->
    session2.beginTransaction().use { tx2 -> /* ... */ }
}
```

---

## Complete Code Examples

### Example 1: Simple CRUD Operations

```kotlin
import org.neo4j.driver.*

class PersonRepository(private val driver: Driver) {
    
    fun createPerson(name: String, age: Int): String {
        return driver.session(
            SessionConfig.builder()
                .withDatabase("neo4j")
                .build()
        ).use { session ->
            session.executeWrite { tx ->
                val result = tx.run(
                    """
                    CREATE (p:Person {id: randomUUID(), name: ${'$'}name, age: ${'$'}age})
                    RETURN p.id AS id
                    """,
                    mapOf("name" to name, "age" to age)
                )
                result.single().get("id").asString()
            }
        }
    }
    
    fun findPersonByName(name: String): Person? {
        return driver.session(
            SessionConfig.builder()
                .withDatabase("neo4j")
                .build()
        ).use { session ->
            session.executeRead { tx ->
                val result = tx.run(
                    "MATCH (p:Person {name: ${'$'}name}) RETURN p.id AS id, p.name AS name, p.age AS age",
                    mapOf("name" to name)
                )
                
                if (result.hasNext()) {
                    val record = result.single()
                    Person(
                        id = record.get("id").asString(),
                        name = record.get("name").asString(),
                        age = record.get("age").asInt()
                    )
                } else {
                    null
                }
            }
        }
    }
    
    fun updatePersonAge(id: String, newAge: Int) {
        driver.session(
            SessionConfig.builder()
                .withDatabase("neo4j")
                .build()
        ).use { session ->
            session.executeWrite { tx ->
                tx.run(
                    "MATCH (p:Person {id: ${'$'}id}) SET p.age = ${'$'}age",
                    mapOf("id" to id, "age" to newAge)
                )
            }
        }
    }
    
    fun deletePerson(id: String) {
        driver.session(
            SessionConfig.builder()
                .withDatabase("neo4j")
                .build()
        ).use { session ->
            session.executeWrite { tx ->
                tx.run(
                    "MATCH (p:Person {id: ${'$'}id}) DETACH DELETE p",
                    mapOf("id" to id)
                )
            }
        }
    }
    
    fun getAllPeople(): List<Person> {
        return driver.session(
            SessionConfig.builder()
                .withDatabase("neo4j")
                .build()
        ).use { session ->
            session.executeRead { tx ->
                val result = tx.run("MATCH (p:Person) RETURN p.id AS id, p.name AS name, p.age AS age")
                result.list().map { record ->
                    Person(
                        id = record.get("id").asString(),
                        name = record.get("name").asString(),
                        age = record.get("age").asInt()
                    )
                }
            }
        }
    }
}

data class Person(val id: String, val name: String, val age: Int)
```

### Example 2: Complex Transaction with Business Logic

```kotlin
import org.neo4j.driver.*
import org.neo4j.driver.exceptions.NoSuchRecordException

class OrganizationService(private val driver: Driver) {
    
    fun employPerson(name: String): String {
        return driver.session(
            SessionConfig.builder()
                .withDatabase("neo4j")
                .build()
        ).use { session ->
            session.executeWrite { tx ->
                employPersonTransaction(tx, name)
            }
        }
    }
    
    private fun employPersonTransaction(tx: TransactionContext, name: String): String {
        val employeeThreshold = 10
        
        // Step 1: Create person (or find existing)
        tx.run("MERGE (p:Person {name: ${'$'}name})", mapOf("name" to name))
        
        // Step 2: Find the latest organization
        val result = tx.run(
            """
            MATCH (o:Organization)
            RETURN o.id AS id, 
                   COUNT{(p:Person)-[:WORKS_FOR]->(o)} AS employeesN
            ORDER BY o.createdDate DESC
            LIMIT 1
            """
        )
        
        var orgId: String
        val employeesN: Int
        
        try {
            val org = result.single()
            orgId = org.get("id").asString()
            employeesN = org.get("employeesN").asInt()
        } catch (e: NoSuchRecordException) {
            // No organizations exist - create first one
            orgId = createOrganization(tx)
            println("No organizations found, created $orgId")
            addPersonToOrganization(tx, name, orgId)
            return orgId
        }
        
        // Step 3: Add to existing org or create new one
        if (employeesN < employeeThreshold) {
            addPersonToOrganization(tx, name, orgId)
        } else {
            orgId = createOrganization(tx)
            println("Organization full, created new one: $orgId")
            addPersonToOrganization(tx, name, orgId)
        }
        
        return orgId
    }
    
    private fun createOrganization(tx: TransactionContext): String {
        val result = tx.run(
            """
            CREATE (o:Organization {
                id: randomUUID(), 
                createdDate: datetime()
            })
            RETURN o.id AS id
            """
        )
        return result.single().get("id").asString()
    }
    
    private fun addPersonToOrganization(
        tx: TransactionContext, 
        personName: String, 
        orgId: String
    ) {
        tx.run(
            """
            MATCH (o:Organization {id: ${'$'}orgId})
            MATCH (p:Person {name: ${'$'}name})
            MERGE (p)-[:WORKS_FOR]->(o)
            """,
            mapOf("orgId" to orgId, "name" to personName)
        )
    }
}
```

### Example 3: Explicit Transaction with External API

```kotlin
import org.neo4j.driver.*

class BankingService(private val driver: Driver) {
    
    fun transferToExternalBank(
        customerId: String, 
        externalBankId: Int, 
        amount: Float
    ) {
        driver.session(
            SessionConfig.builder()
                .withDatabase("neo4j")
                .build()
        ).use { session ->
            session.beginTransaction().use { tx ->
                // Step 1: Check balance
                if (!hasEnoughBalance(tx, customerId, amount)) {
                    println("Customer $customerId has insufficient funds")
                    return  // Rolls back automatically
                }
                
                // Step 2: Call external API (CAN'T BE ROLLED BACK!)
                try {
                    callExternalBankAPI(customerId, externalBankId, amount)
                } catch (e: Exception) {
                    println("External bank transfer failed: ${e.message}")
                    return  // Don't update our database
                }
                
                // Step 3: Update our database
                try {
                    decreaseBalance(tx, customerId, amount)
                    tx.commit()
                    println("Successfully transferred $$amount to external bank")
                } catch (e: Exception) {
                    // Can't rollback external transfer!
                    // Need manual intervention
                    logForManualReconciliation(customerId, externalBankId, amount, e)
                    throw RuntimeException("Database update failed after external transfer - needs manual reconciliation", e)
                }
            }
        }
    }
    
    private fun hasEnoughBalance(
        tx: Transaction, 
        customerId: String, 
        amount: Float
    ): Boolean {
        val result = tx.run(
            """
            MATCH (c:Customer {id: ${'$'}id})
            RETURN c.balance >= ${'$'}amount AS sufficient
            """,
            mapOf("id" to customerId, "amount" to amount)
        )
        return result.single().get("sufficient").asBoolean()
    }
    
    private fun callExternalBankAPI(
        customerId: String, 
        externalBankId: Int, 
        amount: Float
    ) {
        // Simulate external API call
        println("Calling external bank API...")
        // If this fails, we throw exception and transaction rolls back
        // If this succeeds, we can't undo it!
    }
    
    private fun decreaseBalance(
        tx: Transaction, 
        customerId: String, 
        amount: Float
    ) {
        tx.run(
            """
            MATCH (c:Customer {id: ${'$'}id})
            SET c.balance = c.balance - ${'$'}amount
            """,
            mapOf("id" to customerId, "amount" to amount)
        )
    }
    
    private fun logForManualReconciliation(
        customerId: String,
        externalBankId: Int,
        amount: Float,
        error: Exception
    ) {
        println("CRITICAL: Manual reconciliation needed!")
        println("Customer: $customerId")
        println("External Bank: $externalBankId")
        println("Amount: $$amount")
        println("Error: ${error.message}")
        // In production: log to monitoring system, create support ticket, etc.
    }
}
```

### Example 4: Application Setup with Dependency Injection

```kotlin
import org.neo4j.driver.*

// Singleton driver configuration
object DatabaseConfig {
    private const val URI = "bolt://localhost:7687"
    private const val USER = "neo4j"
    private const val PASSWORD = "password"
    
    val driver: Driver by lazy {
        GraphDatabase.driver(
            URI,
            AuthTokens.basic(USER, PASSWORD)
        ).also {
            it.verifyConnectivity()
            println("Connected to Neo4j at $URI")
        }
    }
    
    fun close() {
        driver.close()
        println("Neo4j driver closed")
    }
}

// Service layer
class UserService(private val driver: Driver) {
    
    fun createUser(name: String, email: String): String {
        return driver.session(
            SessionConfig.builder()
                .withDatabase("neo4j")
                .build()
        ).use { session ->
            session.executeWrite { tx ->
                val result = tx.run(
                    """
                    CREATE (u:User {
                        id: randomUUID(),
                        name: ${'$'}name,
                        email: ${'$'}email,
                        createdAt: datetime()
                    })
                    RETURN u.id AS id
                    """,
                    mapOf("name" to name, "email" to email)
                )
                result.single().get("id").asString()
            }
        }
    }
    
    fun createFriendship(userId1: String, userId2: String) {
        driver.session(
            SessionConfig.builder()
                .withDatabase("neo4j")
                .build()
        ).use { session ->
            session.executeWrite { tx ->
                tx.run(
                    """
                    MATCH (u1:User {id: ${'$'}id1})
                    MATCH (u2:User {id: ${'$'}id2})
                    MERGE (u1)-[:FRIENDS_WITH]-(u2)
                    """,
                    mapOf("id1" to userId1, "id2" to userId2)
                )
            }
        }
    }
    
    fun getFriendsOfUser(userId: String): List<String> {
        return driver.session(
            SessionConfig.builder()
                .withDatabase("neo4j")
                .build()
        ).use { session ->
            session.executeRead { tx ->
                val result = tx.run(
                    """
                    MATCH (u:User {id: ${'$'}id})-[:FRIENDS_WITH]-(friend:User)
                    RETURN friend.name AS name
                    ORDER BY name
                    """,
                    mapOf("id" to userId)
                )
                result.list().map { it.get("name").asString() }
            }
        }
    }
}

// Main application
fun main() {
    try {
        val userService = UserService(DatabaseConfig.driver)
        
        // Create users
        val alice = userService.createUser("Alice", "alice@example.com")
        val bob = userService.createUser("Bob", "bob@example.com")
        val charlie = userService.createUser("Charlie", "charlie@example.com")
        
        // Create friendships
        userService.createFriendship(alice, bob)
        userService.createFriendship(alice, charlie)
        
        // Query friends
        val alicesFriends = userService.getFriendsOfUser(alice)
        println("