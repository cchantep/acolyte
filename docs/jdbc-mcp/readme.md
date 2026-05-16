# Acolyte JDBC MCP

**Generate Acolyte test handlers from live database data using the Model Context Protocol.**

[![Repository](https://img.shields.io/badge/repository-cchantep%2Facolyte-181717?logo=github)](https://github.com/cchantep/acolyte/)
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/cchantep/acolyte/tree/master.svg?style=shield)](https://app.circleci.com/pipelines/github/cchantep/acolyte)
![License](https://img.shields.io/badge/license-LGPL%202.1-blue)

## What is it?

The **Acolyte JDBC MCP** plugin enables developers to quickly generate Acolyte test handlers from live databases. Instead of manually creating test data structures, you can:

1. **Connect** to any JDBC-compatible database (PostgreSQL, MySQL, H2, Oracle, etc.)
2. **Discover** table schemas and column types
3. **Analyze** SQL queries with mandatory user confirmation
4. **Record** query results as Acolyte-compatible handler definitions
5. **Export** structured metadata for your test code

**Key Innovation:** Query extraction includes **mandatory user confirmation**, which prevents silent errors and catches edge cases like aliases, complex expressions, and string literals.

## Quick Start

### 1. Install

```bash
mkdir -p ~/.local/share/acolyte-jdbc-mcp
curl -fL \
  -o ~/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar \
  https://github.com/cchantep/acolyte/releases/latest/download/acolyte-jdbc-mcp-assembly.jar
```

Output: `~/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar`

### 2. Configure Claude Desktop

Edit `~/Library/Application Support/Claude/claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "acolyte-jdbc": {
      "command": "java",
      "args": [
        "-jar",
        "${HOME}/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar"
      ]
    }
  }
}
```

Restart Claude Desktop. The Acolyte JDBC tools are now available.

### 3. Use in Claude

> "Connect me to PostgreSQL and show me the schema for the `users` table, then analyze a query to extract row data."

Claude will:
- ✅ Call `connect` with your database
- ✅ Call `discover` to list columns
- ✅ Call `record` to extract query structure
- ✅ Ask you to confirm the extraction
- ✅ Help you create the Acolyte handler

## Features

### 🔒 Query Confirmation Workflow

**The Problem:** Query parsing can fail silently on edge cases (aliases, complex WHERE, subqueries).

**The Solution:** Before executing any query, the tool shows you the extracted structure and asks you to confirm it's correct.

```
User: "Analyze: SELECT u.id, u.name FROM users u WHERE u.status = ?"
       ↓
Tool:  "I extracted: fields=[u.id, u.name], conditions=[{field: u.status, operator: =}], params=1"
       ↓
User:  Reviews and confirms the extraction is correct
       ↓
Tool:  Executes query and returns results
```

### 📊 Complete Type Mapping

Maps SQL types to Acolyte scalar types with support for:

- Primitives: INTEGER, BIGINT, DOUBLE, BOOLEAN
- Strings: VARCHAR, CHAR, LONGVARCHAR
- Temporal: DATE, TIME, TIMESTAMP
- Binary: BLOB, CLOB
- Numeric: DECIMAL, NUMERIC (with precision handling)

### 🎯 Multi-Parameter Execution

Execute queries with multiple parameter sets and aggregate results:

```
Query: SELECT * FROM users WHERE status = ?
Parameters: [["active"], ["pending"], ["inactive"]]
Result: 3 result sets with combined row counts
```

### ✅ Specs2 Test Suite

- 44 unit tests (TypeMapping, QueryAnalyzer, QueryExecutor, MCP Protocol)
- 15 integration tests (end-to-end workflows)
- 62/62 passing ✅

## Documentation

- **[Getting Started Guide](./getting_started.md)** — Installation, quick walkthrough, common patterns
- **[Installation & Configuration](./installation.md)** — Download prebuilt JAR, configure Claude/GitHub CLI, security
- **[API Reference](./api_reference.md)** — Detailed tool documentation with examples
- **[Prompts & Examples](./prompts.md)** — Real prompts to invoke tools, use cases, conversation patterns

## Architecture

### Design Principles

1. **Minimal dependencies** — Uses only Play JSON and JDBC
2. **Immutability by default** — `val` + case classes throughout
3. **Type safety** — No `Any` types; sealed traits for variants
4. **Transparent error handling** — Try/Either with proper unwrapping
5. **Offler compliance** — Enforces Acolyte code quality rules

### Module Structure

```
jdbc-mcp/
├── src/main/scala/
│   ├── McpServer.scala          # JSON-RPC server entry point
│   ├── McpProtocol.scala        # Request/response serialization
│   ├── McpTools.scala           # Tool implementations (connect, discover, record, close)
│   ├── QueryAnalyzer.scala      # SQL query structure extraction
│   ├── QueryExecutor.scala      # Multi-parameter query execution
│   └── TypeMapping.scala        # SQL type → Acolyte type conversion
├── src/test/scala/
│   ├── TypeMappingSpec.scala
│   ├── QueryAnalyzerSpec.scala
│   ├── QueryExecutorSpec.scala
│   └── McpIntegrationSpec.scala
├── docs/
│   ├── getting_started.md
│   ├── installation.md
│   ├── api_reference.md
│   └── examples.md (coming)
└── build.sbt                    # SBT build configuration
```

### Tool Implementations

| Tool | Purpose | Parameters |
|------|---------|------------|
| `connect` | Establish JDBC connection | `url`, `user`, `password`, `driver` |
| `discover` | Retrieve table schema | `connectionId`, `table` |
| `record` | Extract & execute query (2-step) | `query`, `connectionId`, `confirmed` |
| `close` | Close connection | `connectionId` |

## Query Confirmation Deep Dive

### Why Confirmation?

Extraction has inherent limitations:

| Pattern | Issue | Example |
|---------|-------|---------|
| Aliases | Not normalized | `SELECT u.id` → Extracts as `u.id` not `id` |
| String literals | May confuse parser | `WHERE name = 'O''Brien'` with escaping |
| Complex WHERE | Nested logic missed | `WHERE (id = ? OR status IN (...))` |
| Subqueries | Not supported | `SELECT * FROM (SELECT id FROM ...)` |
| Function calls | Extracted incorrectly | `SELECT COUNT(*), name` → `COUNT(*)` as field |

**Solution:** Ask the user to verify the extraction is correct before executing.

### Two-Step Workflow

**Step 1: Extract** (no database access)

```json
{"method":"record","params":{"query":"SELECT id FROM users WHERE id = ?"}}
→ {"status":"awaiting_confirmation","extracted":{...},"message":"..."}
```

**Step 2: Confirm & Execute** (requires connection + confirmation)

```json
{"method":"record","params":{"connectionId":"...","query":"...","confirmed":true}}
→ {"status":"executed","executionCount":1,"totalRows":5}
```

### User Experience

- ✅ **Simple queries** (no aliases, straightforward WHERE): One `confirmed=true` parameter
- ✅ **Complex queries** (aliases, multiple conditions): User reviews extraction, confirms if correct, retries if not
- ✅ **Edge cases** (subqueries, function calls): User gets error and tries alternative query
- ✅ **Audit trail**: Extracted structure is logged before execution

## Building from Source

### Prerequisites

- Java 11+
- Scala 2.12.20
- SBT 1.12.11+

### Build Assembly JAR

```bash
sbt "jdbc-mcp/assembly"
```

Output: `jdbc-mcp/target/acolyte-jdbc-mcp-assembly.jar` (~15 MB with dependencies)

### Run Tests

```bash
sbt "jdbc-mcp/test"
```

Run pure unit tests only (exclude external DB integration specs):

```bash
sbt "jdbc-mcp/testOnly * -- exclude integration"
```

### Code Quality Checks

```bash
sbt "jdbc-mcp/compile"  # Zero errors/warnings with -Xfatal-warnings
sbt "jdbc-mcp/scalafmt"  # Format code
sbt scalafixAll           # Lint with Scalafix (Offler rules)
```

## Deployment

### Distribution Options

**Option 1: GitHub Releases**

```bash
# Build and attach acolyte-jdbc-mcp-assembly.jar to GitHub release
sbt "jdbc-mcp/assembly"
```

**Option 2: Local Installation**

```bash
mkdir -p ~/.local/bin
cp jdbc-mcp/target/acolyte-jdbc-mcp-assembly.jar ~/.local/bin/
```

### Configuration for Claude Desktop

See [Installation & Configuration](./installation.md) for detailed setup.

### Configuration for GitHub CLI

MCP integration with GitHub CLI is typically via Claude Copilot. Configure Claude first, then use in GitHub CLI/VS Code Copilot Chat.

## Security

### Best Practices

1. **Never hardcode credentials** — Use environment variables or config files
2. **Use read-only database accounts** for test data extraction
3. **SSH tunnel to remote databases** — Don't expose database over network
4. **Restrict JAR permissions** — `chmod 700 acolyte-jdbc-mcp-assembly.jar`
5. **Validate JDBC URLs** — Prevent SQL injection via connection string

See [Installation & Configuration - Security](./installation.md#security-considerations) for details.

## Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| "JAR not found" | Build with `sbt jdbc-mcp/assembly` and update config path |
| "Connection refused" | Verify database running and accessible; check JDBC URL |
| "Query extraction wrong" | Review extracted structure in Step 1; try simpler query |
| "Out of memory" | Increase heap: `java -Xmx1g -jar acolyte-jdbc-mcp-assembly.jar` |

## Contributing

Contributions welcome! Please:

1. Fork the repository
2. Create a branch for your feature
3. Add tests (target ≥70% coverage)
4. Ensure all 62 tests pass: `sbt "jdbc-mcp/test"`
5. Follow Offler code quality rules
6. Submit a pull request

## Offler Code Quality Rules

All code adheres to **Offler**, Acolyte's strict code quality standard:

- ✅ No `Any` types — Use sealed traits, generics, case classes
- ✅ Immutability first — `val` + List/Map/Seq by default
- ✅ No wildcard imports — Explicit imports only
- ✅ No default values in case classes
- ✅ String interpolation — No concatenation
- ✅ Proper blank line separation
- ✅ No unnecessary intermediate values

See the Acolyte repository for full Offler specification.

## License

GNU LESSER GENERAL PUBLIC LICENSE 2.1 (LGPL 2.1). See LICENSE.txt for details.

## Support

For issues, questions, or feedback:

1. Review [API Reference](./api_reference.md)
2. Open an issue on GitHub
3. Submit a PR with fixes/enhancements

## Acknowledgments

Built for the **Acolyte** JDBC test framework with ❤️

---

## Next Steps

- [Getting Started Guide](./getting_started.md) - Learn how to use the tools
- [Changelog](./changelog.md) - What's new