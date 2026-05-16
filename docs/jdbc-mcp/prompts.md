# MCP Plugin Prompts & Examples

This guide shows real prompts you can use with any MCP-capable agent (Claude, Copilot, etc.) to invoke the Acolyte JDBC MCP plugin. These examples demonstrate how natural language queries translate to tool calls.

To avoid routing ambiguity, include an explicit target in your prompts, such as:

```text
Use MCP server "acolyte-jdbc" for this task.
```

## Quick Reference: Common Prompts

| Goal | Example Prompt |
|------|---|
| **Connect** | "Use MCP server acolyte-jdbc and connect to my local PostgreSQL database" |
| **Discover Schema** | "Using acolyte-jdbc, show me the tables in the database" |
| **Explore Table** | "Using acolyte-jdbc, what columns does the users table have?" |
| **Record Query** | "Using acolyte-jdbc, record a query to get all users with email addresses" |
| **Films + Acolyte DSL** | "Use acolyte-jdbc: connect, discover films, query Sci-Fi films, then generate Acolyte Scala DSL" |
| **Complex Query** | "Using acolyte-jdbc, find users created in the last 30 days with admin role" |

---

## 1. Basic Database Connection

### Prompt

```
Use MCP server "acolyte-jdbc". I need to connect to a PostgreSQL database. The connection string is 
jdbc:postgresql://localhost:5432/myapp with username 'postgres' and 
password 'secret123'
```

### What the Agent Does

Calls the `acolyte-jdbc` MCP `connect` tool with:

```json
{
  "url": "jdbc:postgresql://localhost:5432/myapp",
  "user": "postgres",
  "password": "secret123"
}
```

### Response

```json
{
  "status": "connected",
  "connectionId": "conn-b3d9-4f2a-a8c1",
  "database": "PostgreSQL 14.5",
  "schema": "public",
  "readyToDiscover": true,
  "securityWarnings": [
    "Use this MCP connection only against non-sensitive databases (local/dev/test), not production.",
    "Never expose credentials in prompts, logs, screenshots, or committed files."
  ],
  "credentialGuidance": {
    "recommendation": "Use environment variables or a secrets manager and inject credentials at runtime.",
    "avoid": ["hardcoded passwords", "plaintext secrets in scripts"]
  }
}
```

When returning this response, the agent should explicitly repeat the safety and credential warnings before continuing.

### Next Steps

"Now show me what tables are available."

---

## 2. Discover Database Schema

### Prompt

```
What tables do we have in this database? I'm particularly interested 
in anything related to users or orders.
```

### What the Agent Does

Calls the `acolyte-jdbc` MCP `discover` tool:

```json
{
  "connectionId": "conn-b3d9-4f2a-a8c1",
  "schemaPattern": "public"
}
```

### Response

```json
{
  "status": "discovered",
  "tables": ["users", "orders", "products", "order_items"],
  "schemas": ["public"],
  "totalTables": 4
}
```

### Next Steps

"Tell me the structure of the users table."

---

## 3. Explore Table Structure

### Prompt

```
What columns are in the users table? I need to know the data types 
and whether they're nullable.
```

### What the Agent Does

Calls the `acolyte-jdbc` MCP `record` tool with `extractOnly=true` for a discovery query:

```json
{
  "connectionId": "conn-b3d9-4f2a-a8c1",
  "query": "SELECT * FROM users LIMIT 0",
  "extractOnly": true
}
```

### Response

```json
{
  "status": "extracted",
  "query": "SELECT * FROM users LIMIT 0",
  "extracted": {
    "columns": [
      {
        "name": "id",
        "type": "BIGINT",
        "nullable": false,
        "javaType": "java.lang.Long"
      },
      {
        "name": "email",
        "type": "VARCHAR",
        "nullable": false,
        "javaType": "java.lang.String"
      },
      {
        "name": "created_at",
        "type": "TIMESTAMP",
        "nullable": false,
        "javaType": "java.sql.Timestamp"
      },
      {
        "name": "last_login",
        "type": "TIMESTAMP",
        "nullable": true,
        "javaType": "java.sql.Timestamp"
      },
      {
        "name": "is_active",
        "type": "BOOLEAN",
        "nullable": false,
        "javaType": "java.lang.Boolean"
      }
    ]
  },
  "confirmationRequired": false
}
```

---

## 4. Simple Query Recording

### Prompt

```
I want to record a query to get all active users with their email 
addresses and creation dates.
```

### What the Agent Does

**Step 1: Extract & Show**

Calls the `acolyte-jdbc` MCP `record` tool (default: `confirmed=false`):

```json
{
  "connectionId": "conn-b3d9-4f2a-a8c1",
  "query": "SELECT id, email, created_at FROM users WHERE is_active = true"
}
```

### Response (Extraction)

```json
{
  "status": "extracted",
  "query": "SELECT id, email, created_at FROM users WHERE is_active = true",
  "extracted": {
    "tables": ["users"],
    "columns": ["id", "email", "created_at"],
    "filters": ["is_active = true"],
    "rowsAffected": null
  },
  "confirmationRequired": true,
  "message": "Please review the extracted query structure. Does this look correct?"
}
```

### User Reviews

```
Yes, that looks right. Go ahead and execute the query.
```

**Step 2: Execute with Confirmation**

The agent calls the `record` tool again with `confirmed=true`:

```json
{
  "connectionId": "conn-b3d9-4f2a-a8c1",
  "query": "SELECT id, email, created_at FROM users WHERE is_active = true",
  "confirmed": true
}
```

### Response (Execution)

```json
{
  "status": "executed",
  "query": "SELECT id, email, created_at FROM users WHERE is_active = true",
  "executionCount": 1,
  "totalRows": 247,
  "rows": [
    {
      "id": 1,
      "email": "alice@example.com",
      "created_at": "2024-01-15T10:30:00Z"
    },
    {
      "id": 2,
      "email": "bob@example.com",
      "created_at": "2024-02-03T14:22:00Z"
    },
    {
      "id": 3,
      "email": "charlie@example.com",
      "created_at": "2024-03-10T09:45:00Z"
    }
  ]
}
```

---

## 5. Complex Query with WHERE Clause

### Prompt

```
Show me all orders from the last 30 days that have a total value 
greater than $100, ordered by date descending. I want to see the 
customer name, order date, and total.
```

### What the Agent Does

**Step 1: Extract**

```json
{
  "connectionId": "conn-b3d9-4f2a-a8c1",
  "query": "SELECT o.id, c.name, o.created_at, o.total FROM orders o JOIN customers c ON o.customer_id = c.id WHERE o.created_at >= CURRENT_DATE - INTERVAL 30 DAY AND o.total > 100 ORDER BY o.created_at DESC"
}
```

### Response (Extraction)

```json
{
  "status": "extracted",
  "extracted": {
    "tables": ["orders", "customers"],
    "joins": ["customers"],
    "columns": ["o.id", "c.name", "o.created_at", "o.total"],
    "filters": [
      "o.created_at >= CURRENT_DATE - INTERVAL 30 DAY",
      "o.total > 100"
    ],
    "orderBy": "o.created_at DESC"
  },
  "message": "I've extracted the query structure. The JOIN and DATE filtering look correct. Shall I execute?"
}
```

### User Confirms

```
Perfect! Execute it.
```

**Step 2: Execute**

```json
{
  "connectionId": "conn-b3d9-4f2a-a8c1",
  "query": "SELECT o.id, c.name, o.created_at, o.total FROM orders o JOIN customers c ON o.customer_id = c.id WHERE o.created_at >= CURRENT_DATE - INTERVAL 30 DAY AND o.total > 100 ORDER BY o.created_at DESC",
  "confirmed": true
}
```

### Response (Execution)

```json
{
  "status": "executed",
  "executionCount": 1,
  "totalRows": 523,
  "rows": [
    {
      "id": 98765,
      "name": "Acme Corp",
      "created_at": "2024-05-14T16:20:00Z",
      "total": 2450.50
    },
    {
      "id": 98764,
      "name": "TechStart Inc",
      "created_at": "2024-05-14T13:45:00Z",
      "total": 1875.25
    }
  ]
}
```

---

## 6. Query with Table Aliases

### Prompt

```
I want a query showing users who placed orders but don't have 
verified email addresses. Join users and orders tables.
```

### What the Agent Does

**Step 1: Extract**

```json
{
  "connectionId": "conn-b3d9-4f2a-a8c1",
  "query": "SELECT u.id, u.email, COUNT(o.id) as order_count FROM users u LEFT JOIN orders o ON u.id = o.user_id WHERE u.email_verified = false GROUP BY u.id, u.email"
}
```

### Response (Extraction) ⚠️

```json
{
  "status": "extracted",
  "extracted": {
    "tables": ["users", "orders"],
    "columns": ["u.id", "u.email", "COUNT(o.id)"],
    "aliases": {
      "u": "users",
      "o": "orders"
    },
    "joins": ["LEFT JOIN orders"],
    "filters": ["u.email_verified = false"],
    "groupBy": ["u.id", "u.email"]
  },
  "message": "I notice aliases (u, o) are used in the extracted columns. Please confirm these are correct before execution."
}
```

### Why Confirmation Matters

Regex extraction can struggle with complex aliases in JOIN conditions. User confirms the aliases are right:

```
Yes, u is users and o is orders. The query is correct.
```

**Step 2: Execute**

```json
{
  "connectionId": "conn-b3d9-4f2a-a8c1",
  "query": "SELECT u.id, u.email, COUNT(o.id) as order_count FROM users u LEFT JOIN orders o ON u.id = o.user_id WHERE u.email_verified = false GROUP BY u.id, u.email",
  "confirmed": true
}
```

### Response (Execution)

```json
{
  "status": "executed",
  "executionCount": 1,
  "totalRows": 89,
  "rows": [
    {
      "id": 42,
      "email": "unverified@example.com",
      "order_count": 3
    }
  ]
}
```

---

## 7. Films Workflow: Connect → Discover → Sci-Fi Query → Acolyte DSL

### Scenario

Table `films`:

```sql
CREATE TABLE films (
  code      VARCHAR(5),
  title     VARCHAR(40),
  did       INTEGER,
  date_prod DATE,
  kind      VARCHAR(10),
  len       INTERVAL HOUR TO MINUTE
);
```

### User Prompt

```
Use MCP server "acolyte-jdbc". Connect to jdbc:postgresql://localhost:55000/postgres with user postgres.
Then discover table films.
Then query Sci-Fi films and generate the corresponding Acolyte Scala DSL.
```

### Tool Calls

**1. connect**

```json
{
  "url": "jdbc:postgresql://localhost:55000/postgres",
  "user": "postgres",
  "password": "mysecretpassword"
}
```

**2. discover**

```json
{
  "connectionId": "<connId>",
  "table": "films"
}
```

**3. record (extract)**

```json
{
  "query": "SELECT title, kind FROM films WHERE kind = 'Sci-Fi'"
}
```

**4. record (execute)**

```json
{
  "connectionId": "<connId>",
  "query": "SELECT title, kind FROM films WHERE kind = 'Sci-Fi'",
  "confirmed": true
}
```

### Example Execution Result

```json
{
  "status": "executed",
  "query": "SELECT title, kind FROM films WHERE kind = 'Sci-Fi'",
  "executionCount": 1,
  "totalRows": 2,
  "columns": [
    {
      "index": 1,
      "name": "title",
      "jdbcType": 12,
      "jdbcTypeName": "VARCHAR",
      "javaClassName": "java.lang.String"
    },
    {
      "index": 2,
      "name": "kind",
      "jdbcType": 12,
      "jdbcTypeName": "VARCHAR",
      "javaClassName": "java.lang.String"
    }
  ],
  "dslHints": {
    "recommendedRowListFactory": "rowList2",
    "useStringList": true,
    "rule": "Use RowLists.stringList only when every selected column is textual."
  },
  "rows": [
    ["Solaris", "Sci-Fi"],
    ["Alphaville", "Sci-Fi"]
  ]
}
```

### Agent Rule for DSL Generation

- If `dslHints.useStringList` is `true`, `RowLists.stringList(...)` is allowed.
- If `dslHints.useStringList` is `false`, always generate `RowLists.rowListN(...)` with typed columns from `columns[]`; never use `stringList`.

### Generated Acolyte Scala DSL

```scala
import acolyte.jdbc.RowLists

val filmsSciFiRows =
  RowLists
    .rowList2(classOf[String] -> "title", classOf[String] -> "kind")
    .append("Solaris", "Sci-Fi")
    .append("Alphaville", "Sci-Fi")

val filmsSciFiResult = filmsSciFiRows.asResult
```

---

## 8. Error Handling Examples

### Invalid Connection ID

**Prompt:**

```
Show me data from the orders table
```

**Issue:** No active connection

**Response:**

```json
{
  "status": "error",
  "error": "No connection established",
  "code": "ERR_NO_CONNECTION",
  "suggestion": "Please connect to a database first using a connection string"
}
```

### Syntax Error in Query

**Prompt:**

```
Get all users from the users table
```

**The agent might interpret as:** `SELECT * FROM users WHRE active = 1` (typo: WHRE)

**Response:**

```json
{
  "status": "error",
  "error": "SQL syntax error: Unexpected token WHRE",
  "code": "ERR_SQL_SYNTAX",
  "suggestion": "Did you mean WHERE instead of WHRE?"
}
```

### Table Not Found

**Prompt:**

```
Show me the employees table
```

**Response:**

```json
{
  "status": "error",
  "error": "Table 'employees' not found",
  "code": "ERR_TABLE_NOT_FOUND",
  "suggestion": "Available tables: users, orders, products. Did you mean one of these?"
}
```

---

## 9. Type Discovery Examples

### Prompt

```
What JDBC types are available? I need to know how to handle different 
column types in my application.
```

### What the Agent Does

Calls `discover` with type discovery:

```json
{
  "connectionId": "conn-b3d9-4f2a-a8c1",
  "discoverTypes": true
}
```

### Response

```json
{
  "status": "discovered",
  "jdbcTypes": [
    {
      "code": -6,
      "name": "TINYINT",
      "javaType": "java.lang.Byte",
      "description": "Small integer (0-255)"
    },
    {
      "code": 4,
      "name": "INTEGER",
      "javaType": "java.lang.Integer",
      "description": "Standard integer"
    },
    {
      "code": -5,
      "name": "BIGINT",
      "javaType": "java.lang.Long",
      "description": "Large integer"
    },
    {
      "code": 12,
      "name": "VARCHAR",
      "javaType": "java.lang.String",
      "description": "Variable length string"
    }
  ]
}
```

---

## 10. Batch Operations Example

### Prompt

```
I want to record multiple queries to analyze user behavior:
1. Active users last week
2. New user registrations this month
3. Users who haven't logged in for 90 days
```

### What the Agent Does

Calls `record` three times with different queries:

**Query 1: Active Last Week**

```json
{
  "connectionId": "conn-b3d9-4f2a-a8c1",
  "query": "SELECT COUNT(*) as active_users FROM users WHERE last_login >= CURRENT_DATE - INTERVAL 7 DAY"
}
```

**Query 2: New This Month**

```json
{
  "connectionId": "conn-b3d9-4f2a-a8c1",
  "query": "SELECT COUNT(*) as new_users FROM users WHERE created_at >= CURRENT_DATE - INTERVAL 30 DAY"
}
```

**Query 3: Inactive 90+ Days**

```json
{
  "connectionId": "conn-b3d9-4f2a-a8c1",
  "query": "SELECT COUNT(*) as inactive_users FROM users WHERE last_login < CURRENT_DATE - INTERVAL 90 DAY"
}
```

### Combined Response

```json
{
  "status": "analyzed",
  "results": [
    {
      "query": "Active users last week",
      "rows": 1,
      "data": [{"active_users": 1247}]
    },
    {
      "query": "New user registrations this month",
      "rows": 1,
      "data": [{"new_users": 89}]
    },
    {
      "query": "Users who haven't logged in for 90 days",
      "rows": 1,
      "data": [{"inactive_users": 342}]
    }
  ]
}
```

---

## 11. Conversation Memory

### Prompt Sequence

```
User: Connect to my production database
  → Agent: [connects, stores connectionId internally]

User: What tables do we have?
  → Agent: [uses stored connectionId, discovers tables]

User: Show me the users table structure
  → Agent: [reuses same connectionId]

User: Get all active users
  → Agent: [continues using same connectionId]

User: Close the connection
  → Agent: [calls close with stored connectionId]
```

**Key Point:** The agent maintains context of the active connection throughout the conversation. You don't need to repeat the connection string.

---

## 12. Real-World Use Cases

### Use Case 1: Data Validation

```
I need to validate our data quality. Can you:
1. Count total users in the database
2. Count users missing email addresses
3. Count users with invalid email format (not containing @)
4. Show me a sample of the invalid emails
```

### Use Case 2: Performance Investigation

```
Our reports are running slowly. Can you analyze:
1. How many orders do we have total?
2. How many orders per customer on average?
3. What's the date range of orders?
4. Show top 5 customers by order count
```

### Use Case 3: Business Intelligence

```
I'm creating a dashboard. I need:
1. Daily active users for the last 30 days
2. New user registrations by day
3. Total revenue by product category
4. Customer churn rate (users inactive >60 days)
```

### Use Case 4: Data Migration Verification

```
We're migrating to a new database. Please verify:
1. Row counts in all tables match (expected: users=5000, orders=25000)
2. No NULL values in critical columns (id, email, created_at)
3. Date ranges are reasonable (not from future or before 2020)
4. Sample 10 random records from each table
```

---

## Tips for Better Prompts

### ✅ Good Prompts

```
Show me all customers who made a purchase over $500 in the last quarter
```

Clear intent, specific filters, temporal scope.

```
I need the product names and total sales for each category this year
```

Specific columns, aggregation, clear date range.

### ❌ Ambiguous Prompts

```
Get me some data
```
Too vague. What data? What filters?

```
Show everything from users
```
Unclear - all columns? With filters? Limit?

### 💡 Best Practices

1. **Be specific about what you need** - "top 10 customers by revenue" not "customer data"
2. **Include time ranges** - "last 30 days", "this quarter", "since 2024"
3. **Mention filters early** - "active users", "completed orders", "verified emails"
4. **Ask for confirmation on complex queries** - "Does this look right?"
5. **Start simple** - Explore schema first, then write complex queries

---

## Connection String Examples

### PostgreSQL

```
jdbc:postgresql://localhost:5432/mydatabase
jdbc:postgresql://prod.example.com:5432/app?sslmode=require
```

### MySQL

```
jdbc:mysql://localhost:3306/mydatabase
jdbc:mysql://prod.example.com:3306/app?useSSL=true
```

### SQLite

```
jdbc:sqlite:./myapp.db
jdbc:sqlite:/path/to/database.db
```

### H2 (In-Memory Testing)

```
jdbc:h2:mem:testdb
jdbc:h2:file:./data/testdb
```

### Oracle

```
jdbc:oracle:thin:@localhost:1521:ORCL
jdbc:oracle:thin:@prod-db.example.com:1521:PROD
```

---

## Summary

The MCP plugin understands natural language prompts and translates them into database operations. Key points:

| Feature | How to Use |
|---------|-----------|
| **Connection** | Provide JDBC URL, username, password |
| **Discovery** | Ask "what tables do we have?" or "show me schema" |
| **Queries** | Describe what data you want in natural language |
| **Confirmation** | Review extracted queries before execution |
| **Context** | The agent remembers your connection across messages |
| **Errors** | Clear messages help you fix syntax or logic issues |

The two-step query workflow (extract → confirm → execute) ensures you catch regex parsing errors before data issues occur.
