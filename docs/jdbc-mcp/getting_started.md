# Acolyte JDBC MCP - Getting Started Guide

## Overview

The **Acolyte JDBC MCP** plugin enables developers to quickly generate Acolyte test handlers from live database data using the Model Context Protocol (MCP). Instead of manually creating test data structures, you can:

1. **Connect** to any JDBC-compatible database
2. **Discover** table schemas via JDBC metadata
3. **Analyze** SQL queries to extract structure
4. **Record** query results with automatic parameter confirmation
5. **Export** Acolyte-compatible handler definitions

**Key Features:**

- ✅ Query extraction with **mandatory user confirmation** (prevents silent regex errors)
- ✅ Multi-parameter set execution and result aggregation
- ✅ Type mapping from SQL types to Acolyte scalar types
- ✅ JSON-RPC MCP protocol over stdin/stdout
- ✅ No code generation—structured metadata only (you write the handler code)

## Installation

### Prerequisites

- **Java 11+** (runtime for the MCP JAR and JDBC drivers)
- A supported JDBC driver (e.g., PostgreSQL, MySQL, H2), or let `connect` load one dynamically from Maven
- An MCP client/agent configured with `acolyte-jdbc` (Claude, Copilot, etc.)

### Configure in Your MCP Client

You do **not** add `jdbc-mcp` as a dependency to the application/test project you want to inspect.
This plugin runs as an external MCP server configured in your agent/client (Claude, Copilot, etc.).

See [installation.md](./installation.md) for agent configuration using `java -jar ...`.

## Quick Start

### 1. Configure and Start the MCP Server

Run the server as an external MCP process with the release JAR:

```bash
java -jar ~/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar
```

In normal usage, your MCP client starts this process using your `acolyte-jdbc` config (see [installation.md](./installation.md)).

### 2. Connect to a Database

In your agent, ask:

```text
Use MCP server "acolyte-jdbc" and connect to jdbc:postgresql://localhost/mydb
with user testuser and password secret.
```

The tool returns a connection ID. Save it for discovery and recording steps.
If needed, pass credentials as env references in `connect` (for example `${DB_USER}` / `${DB_PASSWORD}` or `env:DB_USER` / `env:DB_PASSWORD`).
It also returns security and credential guidance. Agents should always surface these warnings:

- use local/dev/test (non-sensitive) databases, not production;
- handle credentials through environment variables or a secrets manager;
- do not expose secrets in prompts, logs, screenshots, or committed files.

### 3. Discover Table Schema

Ask:

```text
Using acolyte-jdbc, discover table users with my current connection.
```

You will get table columns and JDBC types.

**Column types** are JDBC SQL type codes. Common ones:

- `4` = INTEGER
- `-5` = BIGINT
- `12` = VARCHAR
- `91` = DATE
- `93` = TIMESTAMP

### 4. Extract and Confirm Query Structure

Ask:

```text
Using acolyte-jdbc, record this query: SELECT id, name FROM users WHERE id = ?
```

The tool first returns `awaiting_confirmation` with extracted fields/conditions.

**⚠️ Review the extracted structure:**

- ✅ `selectFields`: Are these the columns you queried?
- ✅ `whereConditions`: Do these match your WHERE clause?
- ✅ `parameterCount`: Is this the number of `?` placeholders?

Then confirm execution:

```text
Yes, confirm and execute that query with acolyte-jdbc.
```

You will get execution stats and rows.

### 5. Close the Connection

Ask:

```text
Close the current acolyte-jdbc connection.
```

## Workflow Example: Prompt-Driven `films` Recording

### Table Definition

Assume your database contains:

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

### User Prompt (Single Request)

```text
Connect to PostgreSQL at jdbc:postgresql://localhost:55000/postgres with user postgres.
Then discover table films.
Then query Sci-Fi films and generate the corresponding Acolyte Scala DSL result.
```

### Agent Flow

1. Connect to PostgreSQL with `acolyte-jdbc`.
2. Discover table `films`.
3. Record query `SELECT title, kind FROM films WHERE kind = 'Sci-Fi'`.
4. Review extraction and confirm execution.

Execution response includes rows (example):

```json
{
  "result": {
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
}
```

For mixed-type query results (e.g., `code`, `title`, `did`, `date_prod`, `kind`, `len`), `dslHints.useStringList` is `false`: generate `RowLists.rowListN(...)` with typed columns, not `RowLists.stringList(...)`.

### Generated Scala Acolyte DSL

```scala
import acolyte.jdbc.RowLists

val filmsSciFiRows =
  RowLists
    .rowList2(classOf[String] -> "title", classOf[String] -> "kind")
    .append("Solaris", "Sci-Fi")
    .append("Alphaville", "Sci-Fi")

val filmsSciFiResult = filmsSciFiRows.asResult
```

Use `filmsSciFiResult` in your query handler for `SELECT title, kind FROM films WHERE kind = 'Sci-Fi'`.

## Query Confirmation: Why It Matters

### The Problem

The MCP plugin uses regex patterns to parse SQL queries. Regex **cannot always correctly extract query structure**, especially with:

- Aliases: `SELECT u.id FROM users u`
- Complex expressions: `WHERE (id = ? OR status IN (?, ?))`
- String literals with quotes: `WHERE name = 'O''Brien'`
- Subqueries: `SELECT * FROM (SELECT id FROM ...)`

If the regex fails, it produces **wrong results silently**—no error, just incorrect extraction.

### The Solution

**Mandatory User Confirmation**: Before executing any query, the MCP tool shows you the extracted structure and asks you to confirm it's correct.

**Three Outcomes:**

1. ✅ **Extraction correct** → Confirm with `confirmed=true` → Execute
2. ❌ **Extraction wrong** → Reject and retry with simpler query
3. ⚠️ **Analysis error** → Get error message → Try alternative query

**Example: Alias Extraction Issue**

Query: `SELECT u.id, u.name FROM users u WHERE u.status = ?`

Extraction might show:

```json
{
  "selectFields": ["u.id", "u.name"],
  "whereConditions": [{"field": "u.status", "operator": "=", "placeholder": "?"}]
}
```

**Human review**: Extracted `u.id` instead of `id`. This is **incorrect for Acolyte mapping**.

**Your options:**

1. **Rewrite query** without alias: `SELECT id, name FROM users WHERE status = ?`
2. **Accept extraction**: If you handle aliased fields in your handler code
3. **Document the limitation** in your test comments

## Common Patterns

### Pattern 1: Simple SELECT

```sql
SELECT id, name FROM users
```

✅ Simple regex patterns—usually correct.

### Pattern 2: SELECT with Parameters

```sql
SELECT id, name FROM users WHERE id = ?
```

✅ Regex handles well. Extraction identifies 1 parameter.

### Pattern 3: Multiple Conditions

```sql
SELECT * FROM orders WHERE customer_id = ? AND status LIKE ?
```

⚠️ Verify extraction shows 2 conditions and 2 parameters.

### Pattern 4: Complex WHERE (⚠️ PROBLEMATIC)

```sql
SELECT * FROM products WHERE (category = ? AND price > ?) OR (discount = ?)
```

❌ Regex may struggle with nested parentheses. **Recommendation**: Flatten to `WHERE category = ? AND price > ? OR discount = ?` if possible.

### Pattern 5: Aliases (⚠️ RISKY)

```sql
SELECT u.id, u.name FROM users u WHERE u.id = ?
```

❌ Extraction shows `u.id` and `u.name` (not normalized). **Better**: Rewrite as `SELECT id, name FROM users WHERE id = ?`

## Troubleshooting

### Q: I got "None is not Some" error during extraction

**Cause**: Extracted structure is missing expected fields.

**Fix**: Verify the query is valid SQL. Try simplifying (remove complex expressions, aliases).

### Q: Query extraction seems wrong—too many/few parameters

**Cause**: Regex counted literals (e.g., `'value?'`) as parameters or skipped `?` in strings.

**Fix**: 

1. Check if any `?` are inside string literals
2. If yes, escape them (depending on your DB dialect)
3. Reject extraction and retry

### Q: Can I use subqueries?

**Not recommended**. The regex parser doesn't support subqueries well.

**Workaround**: Run the subquery separately to get results, then use top-level query.

### Q: Connection fails with "Invalid URL"

**Cause**: JDBC URL format incorrect or driver not available.

**Fix**: Check:

- JDBC URL is correctly formatted (e.g., `jdbc:postgresql://host:5432/dbname`)
- Driver JAR is on classpath
- Database is running and accessible from your machine

### Q: How do I handle NULL values?

**Approach**: Let the database return NULLs naturally, then represent them in Acolyte as `null` (Scala) or use `Option` types if needed.

## Best Practices

1. **Start Simple**: Begin with basic SELECT queries (no WHERE), then add WHERE conditions incrementally.

2. **Verify Extraction**: Always review the extracted structure before confirming execution.

3. **Normalize Queries**: Avoid aliases, subqueries, and complex expressions. Flatten WHERE clauses when possible.

4. **Use Parameter Markers**: Prefer `?` for parameters over string literals when recording test data.

5. **Document Edge Cases**: If you encounter extraction issues, document them in test comments for future contributors.

6. **Test Locally**: Use H2 in-memory database for fast, repeatable testing before connecting to production.

7. **Close Connections**: Always close connections when done to avoid leaking resources.

## Next Steps

- Read [API Reference](./api_reference.md) for detailed tool definitions and JSON-RPC payloads
- Review [Examples](./examples.md) for real-world usage patterns
