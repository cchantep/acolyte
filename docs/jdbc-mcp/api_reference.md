# API Reference

## Overview

The Acolyte JDBC MCP plugin exposes 4 tools via the **Model Context Protocol (MCP)** using JSON-RPC 2.0 over stdin/stdout.

**All requests** follow this format:

```json
{"jsonrpc":"2.0","method":"tool_name","params":{...},"id":1}
```

**All responses** follow this format:

```json
{"jsonrpc":"2.0","result":{...},"id":1}
```

Or on error:

```json
{"jsonrpc":"2.0","error":{"code":-32600,"message":"Invalid Request"},"id":1}
```

## Tools

### 1. `connect`

Establishes a JDBC connection to a database.

#### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `url` | String | ✅ | JDBC connection URL (e.g., `jdbc:postgresql://localhost:5432/mydb`) or env reference (`${DB_URL}` / `env:DB_URL`) |
| `user` | String | ❌ | Database username (or env reference `${DB_USER}` / `env:DB_USER`) |
| `password` | String | ❌ | Database password (or env reference `${DB_PASSWORD}` / `env:DB_PASSWORD`) |
| `driver` | String | ❌ | JDBC driver class (auto-detected from URL if omitted; env reference supported) |
| `dependency` | String | ❌ | Driver dependency coordinates in `groupId:artifactId:version` format (downloaded dynamically if missing on classpath; env reference supported) |
| `repository` | String | ❌ | Maven repository base URL (default: `https://repo1.maven.org/maven2`; env reference supported) |

#### Request Example

```json
{
  "jsonrpc": "2.0",
  "method": "connect",
  "params": {
    "url": "jdbc:postgresql://localhost:5432/testdb",
    "user": "testuser",
    "password": "password123",
    "driver": "org.postgresql.Driver",
    "dependency": "org.postgresql:postgresql:42.7.8"
  },
  "id": 1
}
```

**Request Example (env references):**

```json
{
  "jsonrpc": "2.0",
  "method": "connect",
  "params": {
    "url": "${DB_URL}",
    "user": "env:DB_USER",
    "password": "${DB_PASSWORD}"
  },
  "id": 1
}
```

#### Response Example

**Success:**

```json
{
  "jsonrpc": "2.0",
  "result": {
    "id": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
    "status": "connected",
    "driver": "org.postgresql.Driver",
    "dependency": "org.postgresql:postgresql",
    "driverSource": "maven",
    "securityWarnings": [
      "Use this MCP connection only against non-sensitive databases (local/dev/test), not production.",
      "Never expose credentials in prompts, logs, screenshots, or committed files."
    ],
    "credentialGuidance": {
      "recommendation": "Use environment variables or a secrets manager and inject credentials at runtime.",
      "avoid": ["hardcoded passwords", "plaintext secrets in scripts"]
    }
  },
  "id": 1
}
```

**Error:**

```json
{
  "jsonrpc": "2.0",
  "result": {
    "error": "Failed to connect: Connection refused"
  },
  "id": 1
}
```

#### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | String (UUID) | Connection ID for use in subsequent `discover` and `record` calls |
| `status` | String | Always `"connected"` on success |
| `driver` | String | Effective driver class used by `connect` |
| `dependency` | String \| null | Effective dependency coordinates used for dynamic driver loading |
| `driverSource` | String | `"classpath"` if already available, `"maven"` if downloaded dynamically |
| `securityWarnings` | Array[String] | Safety warnings the agent should surface to the user after connecting |
| `credentialGuidance` | Object | Credential handling guidance (`recommendation`, `avoid`) |
| `error` | String | Error message if connection failed |

#### Common Errors

| Error | Cause | Fix |
|-------|-------|-----|
| "Invalid URL" | JDBC URL format incorrect | Check URL format: `jdbc:postgresql://host:port/db` |
| "Connection refused" | Database not running or wrong host/port | Verify database is running and accessible |
| "Authentication failed" | Wrong credentials | Check username and password |
| "Driver not found" | JDBC driver class not on classpath | Provide/add dependency as `groupId:artifactId:version` so plugin can download it |
| "Missing environment variable '...'" | Referenced env var was not set | Export the variable before calling `connect` |

---

### 2. `discover`

Retrieves table schema and column metadata via JDBC.

#### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `connectionId` | String (UUID) | ✅ | Connection ID from `connect` response |
| `table` | String | ✅ | Table name (case-sensitive; `USERS` ≠ `users` depending on DB) |

#### Request Example

```json
{
  "jsonrpc": "2.0",
  "method": "discover",
  "params": {
    "connectionId": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
    "table": "users"
  },
  "id": 2
}
```

#### Response Example

**Success:**

```json
{
  "jsonrpc": "2.0",
  "result": {
    "table": "users",
    "columns": [
      {"name": "id", "type": 4},
      {"name": "name", "type": 12},
      {"name": "email", "type": 12},
      {"name": "created_at", "type": 93}
    ]
  },
  "id": 2
}
```

#### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `table` | String | Table name |
| `columns` | Array | List of column objects |
| `columns[].name` | String | Column name |
| `columns[].type` | Integer | JDBC SQL type code (see table below) |

#### JDBC SQL Type Codes

| Code | Type | Description |
|------|------|-------------|
| `-5` | BIGINT | 64-bit integer |
| `-6` | TINYINT | 8-bit integer |
| `1` | CHAR | Fixed-length string |
| `2` | NUMERIC | Decimal with precision |
| `3` | DECIMAL | Decimal number |
| `4` | INTEGER | 32-bit integer |
| `6` | FLOAT | Floating-point number |
| `7` | REAL | Single-precision float |
| `8` | DOUBLE | Double-precision float |
| `12` | VARCHAR | Variable-length string |
| `-1` | LONGVARCHAR | Very long string |
| `91` | DATE | Date (YYYY-MM-DD) |
| `92` | TIME | Time (HH:MM:SS) |
| `93` | TIMESTAMP | Date and time |
| `-2` | BINARY | Binary data |
| `16` | BOOLEAN | Boolean (true/false) |

#### Common Errors

| Error | Cause | Fix |
|-------|-------|-----|
| "Invalid connection" | Connection ID not found or expired | Verify connection ID and reconnect if needed |
| "Table not found" | Table doesn't exist or wrong name | Check table name and capitalization |
| "Permission denied" | User lacks SELECT privileges on table | Grant SELECT to database user |

---

### 3. `record`

**Two-step workflow:**
1. **Extract query structure** (Step 1)
2. **Execute confirmed query** (Step 2)

#### Step 1: Extract Query Structure

Analyzes a SQL query and returns extracted structure without executing it.

**Parameters (Step 1):**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `query` | String | ✅ | SQL SELECT query to analyze |
| `connectionId` | String | ❌ | Connection ID (not needed for extraction-only) |
| `confirmed` | Boolean | ❌ | If omitted or `false`, performs extraction only |

**Request Example (Step 1):**

```json
{
  "jsonrpc": "2.0",
  "method": "record",
  "params": {
    "query": "SELECT id, name, email FROM users WHERE status = ?"
  },
  "id": 3
}
```

**Response Example (Step 1):**

```json
{
  "jsonrpc": "2.0",
  "result": {
    "status": "awaiting_confirmation",
    "query": "SELECT id, name, email FROM users WHERE status = ?",
    "extracted": {
      "selectFields": ["id", "name", "email"],
      "whereConditions": [
        {
          "field": "status",
          "operator": "=",
          "placeholder": "?"
        }
      ],
      "parameterCount": 1
    },
    "message": "Extracted 3 field(s), 1 condition(s), 1 parameter(s). Please confirm this extraction is correct before execution."
  },
  "id": 3
}
```

**Step 1 Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `status` | String | Always `"awaiting_confirmation"` |
| `query` | String | Original SQL query |
| `extracted.selectFields` | Array[String] | Column names from SELECT clause |
| `extracted.whereConditions` | Array[Object] | WHERE clause conditions (if any) |
| `extracted.parameterCount` | Integer | Number of `?` placeholders found |
| `message` | String | Human-readable extraction summary |

**Condition Object Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `field` | String | Column name in WHERE clause |
| `operator` | String | Comparison operator (`=`, `>`, `<`, `>=`, `<=`, `!=`, `LIKE`, `IN`, `BETWEEN`, `IS`) |
| `placeholder` | String | Always `"?"` for parameterized queries |

#### Step 2: Execute Confirmed Query

Executes the query after user confirms extraction is correct.

**Parameters (Step 2):**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `query` | String | ✅ | Same SQL query as Step 1 |
| `connectionId` | String | ✅ | Connection ID from `connect` response |
| `confirmed` | Boolean | ✅ | Must be `true` to execute |

**Request Example (Step 2):**

```json
{
  "jsonrpc": "2.0",
  "method": "record",
  "params": {
    "connectionId": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
    "query": "SELECT id, name, email FROM users WHERE status = ?",
    "confirmed": true
  },
  "id": 4
}
```

**Response Example (Step 2):**

```json
{
  "jsonrpc": "2.0",
  "result": {
    "status": "executed",
    "query": "SELECT id, name, email FROM users WHERE status = ?",
    "executionCount": 1,
    "totalRows": 42,
    "columns": [
      {
        "index": 1,
        "name": "id",
        "jdbcType": 4,
        "jdbcTypeName": "INTEGER",
        "javaClassName": "java.lang.Integer"
      },
      {
        "index": 2,
        "name": "name",
        "jdbcType": 12,
        "jdbcTypeName": "VARCHAR",
        "javaClassName": "java.lang.String"
      }
    ],
    "dslHints": {
      "recommendedRowListFactory": "rowList3",
      "useStringList": false,
      "rule": "Use RowLists.rowListN with typed columns; do not use RowLists.stringList for mixed or non-text columns."
    },
    "rows": [
      [1, "Alice", "alice@example.com"]
    ]
  },
  "id": 4
}
```

**Step 2 Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `status` | String | Always `"executed"` on success |
| `query` | String | Original SQL query |
| `executionCount` | Integer | Number of result sets returned |
| `totalRows` | Integer | Total rows across all result sets |
| `columns` | Array[Object] | Result-set column metadata |
| `columns[].index` | Integer | 1-based column index |
| `columns[].name` | String | Result column label |
| `columns[].jdbcType` | Integer | JDBC SQL type code |
| `columns[].jdbcTypeName` | String | Database-specific JDBC type name |
| `columns[].javaClassName` | String | Java class reported by JDBC for this column |
| `dslHints.recommendedRowListFactory` | String | Suggested Acolyte row-list factory (`rowListN`) based on column count |
| `dslHints.useStringList` | Boolean | `true` only when all selected columns are textual |
| `dslHints.rule` | String | Explicit guidance to avoid invalid `stringList` generation |
| `rows` | Array[Array] | Query rows in column order |

#### Query Confirmation Workflow

**⚠️ IMPORTANT:** The extraction step is **mandatory**. You cannot skip confirmation.

**Valid workflow:**

1. Call `record(query=...)` → Get extraction
2. Review extracted structure
3. Call `record(connectionId=..., query=..., confirmed=true)` → Execute

**Invalid (will fail):**

```json
// ❌ Wrong: confirmed=true without extraction step
{"jsonrpc":"2.0","method":"record","params":{"connectionId":"...","query":"...","confirmed":true},"id":1}
```

First call extract, then confirm.

#### Supported SQL Patterns

| Pattern | Supported | Notes |
|---------|-----------|-------|
| Simple SELECT | ✅ | `SELECT * FROM table` |
| SELECT with WHERE | ✅ | `SELECT * FROM table WHERE id = ?` |
| Multiple conditions | ✅ | `WHERE id = ? AND name LIKE ?` |
| Aliases | ⚠️ | Extracted but not normalized (e.g., `u.id` stays as `u.id`) |
| Subqueries | ❌ | Not supported; extract parent query separately |
| Function calls | ⚠️ | Extracted but may not resolve correctly (e.g., `COUNT(*)` as field name) |
| Complex WHERE | ⚠️ | Nested parentheses may cause extraction errors |
| String literals | ✅ | Correctly ignored (not counted as parameters) |

#### Common Errors

| Error | Cause | Fix |
|-------|-------|-----|
| `None is not Some` | Extraction failed (invalid SQL or complex expressions) | Simplify query, remove aliases, flatten WHERE |
| `'extracted' is undefined` | Extraction error; field is missing from response | Review query syntax; try simpler query |
| Parameter count mismatch | Regex counted literals as parameters | Check if `?` appear inside string literals |

---

### 4. `close`

Closes a database connection and frees resources.

#### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `connectionId` | String (UUID) | ✅ | Connection ID from `connect` response |

#### Request Example

```json
{
  "jsonrpc": "2.0",
  "method": "close",
  "params": {
    "connectionId": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d"
  },
  "id": 5
}
```

#### Response Example

**Success:**

```json
{
  "jsonrpc": "2.0",
  "result": {
    "status": "closed",
    "connectionId": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d"
  },
  "id": 5
}
```

#### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `status` | String | Always `"closed"` |
| `connectionId` | String | Connection ID that was closed |

#### Common Errors

| Error | Cause | Fix |
|-------|-------|-----|
| "Invalid connection" | Connection ID not found | Verify connection ID; already closed? |

---

## Error Handling

### JSON-RPC Error Codes

| Code | Meaning | Example |
|------|---------|---------|
| `-32600` | Invalid Request | Missing `jsonrpc` or `method` field |
| `-32601` | Method Not Found | Tool name misspelled |
| `-32602` | Invalid Params | Missing required parameter |
| `-32603` | Internal Error | Unexpected server error |
| `-32000` to `-32099` | Server Error | Connection failed, database unreachable |

### Example Error Response

```json
{
  "jsonrpc": "2.0",
  "error": {
    "code": -32602,
    "message": "Invalid params",
    "data": "Missing required parameter: connectionId"
  },
  "id": 1
}
```

---

## Complete Workflow Example

### Scenario: Extract and record a user query

```json
// Step 1: Connect
{"jsonrpc":"2.0","method":"connect","params":{"url":"jdbc:postgresql://localhost/testdb","user":"user","password":"pass"},"id":1}
→ Response: {"result":{"id":"conn-123","status":"connected"},"id":1}

// Step 2: Discover schema
{"jsonrpc":"2.0","method":"discover","params":{"connectionId":"conn-123","table":"users"},"id":2}
→ Response: {"result":{"table":"users","columns":[{"name":"id","type":4},{"name":"name","type":12}]},"id":2}

// Step 3: Extract query (mandatory first step)
{"jsonrpc":"2.0","method":"record","params":{"query":"SELECT id, name FROM users WHERE id = ?"},"id":3}
→ Response: {"result":{"status":"awaiting_confirmation","extracted":{...},"message":"..."},"id":3}

// Step 4: Confirm and execute
{"jsonrpc":"2.0","method":"record","params":{"connectionId":"conn-123","query":"SELECT id, name FROM users WHERE id = ?","confirmed":true},"id":4}
→ Response: {"result":{"status":"executed","totalRows":1},"id":4}

// Step 5: Close
{"jsonrpc":"2.0","method":"close","params":{"connectionId":"conn-123"},"id":5}
→ Response: {"result":{"status":"closed"},"id":5}
```

---

## Next Steps

- [Getting Started Guide](./getting_started.md) - Learn how to use the tools
- [Installation & Configuration](./installation.md) - Set up for Claude or GitHub CLI
- [Examples](./examples.md) - Real-world usage patterns