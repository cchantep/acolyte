# Installation & Configuration Guide

## Overview

The Acolyte JDBC MCP plugin runs as an MCP server process started by your agent/client (Claude, Copilot CLI, etc.). To use it, you need to:

1. **Download** the prebuilt assembly JAR
2. **Configure** your MCP client (Claude/GitHub CLI) to launch the plugin
3. **Test** the connection

## Install Prebuilt JAR (Recommended)

### Step 1: Download the latest release

Download from GitHub Releases (recommended for end users):

```bash
# macOS/Linux
mkdir -p ~/.local/share/acolyte-jdbc-mcp
curl -fL \
  -o ~/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar \
  https://github.com/cchantep/acolyte/releases/latest/download/acolyte-jdbc-mcp-assembly.jar
```

If you prefer `wget`:

```bash
mkdir -p ~/.local/share/acolyte-jdbc-mcp
wget \
  -O ~/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar \
  https://github.com/cchantep/acolyte/releases/latest/download/acolyte-jdbc-mcp-assembly.jar
```

### Step 2: Verify the JAR

```bash
ls -la ~/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar
java -jar ~/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar
```

The JAR contains all dependencies and is directly executable with `java -jar`.

### Optional: Simplify with Shell Wrapper

Create `bin/acolyte-jdbc-mcp` for convenience:

```bash
#!/bin/bash
# Find the JAR (wherever it's installed)
JAR_PATH="${ACOLYTE_MCP_JAR:-$HOME/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar}"

if [ ! -f "$JAR_PATH" ]; then
  echo "Error: Assembly JAR not found at $JAR_PATH"
  echo "Set ACOLYTE_MCP_JAR environment variable or download the release JAR"
  exit 1
fi

exec java -jar "$JAR_PATH" "$@"
```

Make executable:

```bash
chmod +x bin/acolyte-jdbc-mcp
```

Then use as:

```bash
./bin/acolyte-jdbc-mcp
```

## Configuration for Claude Desktop

### Step 1: Locate Claude Desktop Config

**macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`  
**Windows**: `%APPDATA%\Claude\claude_desktop_config.json`  
**Linux**: `~/.config/Claude/claude_desktop_config.json`

### Step 2: Add MCP Server Configuration

Edit `claude_desktop_config.json` and add the Acolyte JDBC MCP server:

```json
{
  "mcpServers": {
    "acolyte-jdbc": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/acolyte-jdbc-mcp.jar"
      ]
    }
  }
}
```

**Platform-specific examples:**

**macOS/Linux:**

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

**Windows (with environment variable):**

```json
{
  "mcpServers": {
    "acolyte-jdbc": {
      "command": "java",
      "args": [
        "-jar",
        "C:\\Users\\YourUser\\acolyte-jdbc-mcp\\acolyte-jdbc-mcp.jar"
      ]
    }
  }
}
```

**Using shell wrapper:**

```json
{
  "mcpServers": {
    "acolyte-jdbc": {
      "command": "/path/to/acolyte/bin/acolyte-jdbc-mcp"
    }
  }
}
```

### Step 3: Restart Claude Desktop

Close and reopen Claude Desktop. The MCP server should now be available.

### Step 4: Verify Connection

In Claude, ask:
> "Connect me to a PostgreSQL database and explore the schema"

Claude will:

1. Call the `connect` tool with your database credentials
2. Call `discover` to list tables and columns
3. Help you generate test data handlers

## Configuration for GitHub CLI

The GitHub CLI integration with MCP is typically handled through **Claude for GitHub Copilot** or custom MCP client tools. Here are the main approaches:

### Option A: Using Claude with GitHub CLI

If using Claude as your coding assistant in GitHub CLI:

1. **Configure Claude** following the steps above
2. **Use in VS Code/GitHub Copilot Chat**:
   - The Acolyte JDBC MCP will be available as a tool in Claude's context
   - Ask Copilot: "Use the Acolyte JDBC MCP to analyze my test database"

### Option B: Configure Copilot CLI MCP

Use `copilot /mcp add` (interactive) or edit the Copilot CLI MCP config file directly:

- config file: `~/.copilot/mcp-config.json`

- server name: `acolyte-jdbc`
- command: `java`
- args: `-jar /absolute/path/to/acolyte-jdbc-mcp.jar`

Example `~/.copilot/mcp-config.json` entry:

```json
{
  "mcpServers": {
    "acolyte-jdbc": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/acolyte-jdbc-mcp.jar"
      ]
    }
  }
}
```

## Distributing the Plugin

This section is mainly for maintainers/publishers. End users should install from the release JAR above.

### Option 1: Maven Central

For public distribution via Maven Central:

1. **Build with SBT**:
   ```bash
   sbt "jdbc-mcp/assembly"
   sbt "jdbc-mcp/publishMavenCentral"
   ```

2. **Users can reference as**:
   ```bash
   java -jar ~/.m2/repository/org/eu/acolyte/acolyte-jdbc-mcp/1.0.0/acolyte-jdbc-mcp-assembly.jar
   ```

### Option 2: GitHub Releases

1. **Create a release** in the Acolyte repository
2. **Attach the assembly JAR** as an artifact
3. **Users download** from releases page:
   ```bash
   wget https://github.com/acolyte/acolyte/releases/download/v1.0.0/acolyte-jdbc-mcp-assembly.jar
   ```

### Option 3: Local Installation

1. **Download the release JAR**:
   ```bash
   mkdir -p ~/.local/share/acolyte-jdbc-mcp
   curl -fL \
     -o ~/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar \
     https://github.com/cchantep/acolyte/releases/latest/download/acolyte-jdbc-mcp-assembly.jar
   ```

2. **Optional convenience symlink**:
   ```bash
   mkdir -p ~/.local/bin
   ln -sf ~/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar ~/.local/bin/acolyte-jdbc-mcp.jar
   ```

3. **Add to PATH**:
   ```bash
   export PATH="$HOME/.local/bin:$PATH"
   ```

## Environment Configuration

### JVM Options

For performance tuning, set JVM options before launching:

```bash
export JAVA_OPTS="-Xmx512m -Xms256m"
java -jar "$HOME/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar"
```

### Database Credentials

Database credentials are provided to the `connect` MCP tool (not as JVM startup flags).

**Option 1: Direct values in connect call** (acceptable for local ad-hoc usage only)

```bash
Use MCP server "acolyte-jdbc" and connect to jdbc:postgresql://localhost/testdb with user app and password secret.
```

**Option 2: Environment-variable references in connect call** (recommended)

```bash
export DB_URL="jdbc:postgresql://localhost/testdb"
export DB_USER="app"
export DB_PASSWORD="secret"
```

Then ask your agent:

```bash
Use MCP server "acolyte-jdbc" and connect with:
- url: ${DB_URL}
- user: ${DB_USER}
- password: ${DB_PASSWORD}
```

Supported env reference formats in `connect` parameters are:

- `${VAR_NAME}`
- `env:VAR_NAME`

Important: these environment variables must be available in the process environment where `acolyte-jdbc` is launched.

**Option 3: Configuration File** (for complex setups)

Create `~/.acolyte/mcp-config.json`:

```json
{
  "connections": [
    {
      "name": "production",
      "url": "jdbc:postgresql://prod-db:5432/live_data",
      "user": "app_readonly"
    },
    {
      "name": "staging",
      "url": "jdbc:postgresql://staging-db:5432/test_data",
      "user": "app_test"
    }
  ]
}
```

## Security Considerations

### 1. Never Hardcode Credentials

❌ **Bad** - Commit to repository:

```json
{
  "user": "dbadmin",
  "password": "SuperSecret123"
}
```

✅ **Good** - Use environment variables:

```bash
export DB_USER="dbadmin"
export DB_PASSWORD="SuperSecret123"
export DB_URL="jdbc:postgresql://localhost/testdb"
# Then use connect with ${DB_URL}, ${DB_USER}, ${DB_PASSWORD}
```

### 2. Restrict JAR Permissions

```bash
# Only you can read/execute
chmod 700 "$HOME/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar"
```

### 3. Use Read-Only Database Accounts

For test data extraction, create a database user with minimal privileges:

```sql
-- PostgreSQL example
CREATE USER acolyte_reader WITH PASSWORD 'read_only_password';
GRANT CONNECT ON DATABASE testdb TO acolyte_reader;
GRANT USAGE ON SCHEMA public TO acolyte_reader;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO acolyte_reader;
```

### 4. Tunnel via SSH for Remote Databases

```bash
# Create SSH tunnel to database server
ssh -L 5432:remote-db:5432 user@remote-server

# Connect through tunnel
# with MCP connect url: jdbc:postgresql://localhost:5432/testdb
```

## Troubleshooting Configuration

### Issue: "JAR not found"

```
Error: Assembly JAR not found at [path]
```

**Solution:**

1. Verify JAR exists: `ls -la ~/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar`
2. Re-download from release
3. Update config with absolute path

### Issue: "Connection refused" in Claude

Claude tries to start the MCP server but fails.

**Solution:**

1. **Test manually**:
   ```bash
   java -jar ~/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar
   # Should print "MCP server listening..."
   # Type Ctrl+C to exit
   ```

2. **Check Java version**:
   ```bash
   java -version
   # Should be Java 11+
   ```

3. **Verify JAR path used by MCP config is correct and readable**

### Issue: "No main class"

```
Error: Could not find or load main class acolyte.jdbc.mcp.McpServer
```

**Solution:**

1. Re-download the release JAR (you may have a stale/corrupted file).
2. Check JAR manifest:
   ```bash
   unzip -p ~/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar META-INF/MANIFEST.MF | grep Main-Class
   ```

### Issue: "Out of memory"

```
java.lang.OutOfMemoryError: Java heap space
```

**Solution:** Increase heap size:

```bash
java -Xmx1g -jar "$HOME/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar"
```

## Verification Checklist

- [ ] Assembly JAR downloaded successfully: `ls -la ~/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar`
- [ ] JAR is executable: `java -jar ~/.local/share/acolyte-jdbc-mcp/acolyte-jdbc-mcp.jar` (should print help or start)
- [ ] Claude config file exists and is valid JSON
- [ ] JAR path in config is absolute and correct
- [ ] Java 11+ installed: `java -version`
- [ ] Database connection credentials working: (e.g. `psql -U dbuser -h dbhost -d dbname -c "SELECT 1"`)
- [ ] MCP server starts: Manual test with `java -jar ...`
- [ ] Claude recognizes new tools after restart

## Next Steps

- [Getting Started Guide](./getting_started.md) - Learn how to use the tools
- [API Reference](./api_reference.md) - Detailed tool documentation
- [Examples](./examples.md) - Real-world usage patterns
