package acolyte.jdbc.mcp

import java.util.concurrent.atomic.AtomicInteger

import java.sql.{ Connection, DriverManager }

import play.api.libs.json._

final class McpIntegrationSpec extends org.specs2.mutable.Specification {
  "MCP Integration".title

  private val dbCounter = new AtomicInteger(0)

  "Connect and Discover" should {
    "connect to database and discover schema" in withIsolatedConnection {
      conn =>
        val metadata = conn.getMetaData
        val tables = metadata.getTables(null, null, "USERS", null)

        tables.next() must beTrue and {
          tables.getString("TABLE_NAME") must_=== "USERS"
        }
    }

    "extract column metadata for table" in withIsolatedConnection { conn =>
      val metadata = conn.getMetaData
      val columns = metadata.getColumns(null, null, "USERS", null)
      val columnNames = Iterator
        .continually(columns.next())
        .takeWhile(identity)
        .map { _ => columns.getString("COLUMN_NAME") }
        .toList

      columnNames must contain("ID") and {
        columnNames must contain("NAME")
      } and {
        columnNames must contain("EMAIL")
      }
    }
  }

  "Query Execution" should {
    "execute SELECT query and capture rows" in withIsolatedConnection { conn =>
      val result = QueryExecutor.executeQuery(
        conn,
        "SELECT * FROM users",
        List(List.empty)
      )

      result must beSuccessfulTry.which { executions =>
        (executions.length must_=== 1) and (executions(0).rowCount must_=== 2)
      }
    }

    "execute parametrized queries" in withIsolatedConnection { conn =>
      val result = QueryExecutor.executeQuery(
        conn,
        "SELECT * FROM users WHERE id = ?",
        List(List(1), List(2))
      )

      result must beSuccessfulTry.which { executions =>
        executions.length must_=== 2
      }
    }

    "execute multiple parameter sets independently" in withIsolatedConnection {
      conn =>
        val result = QueryExecutor.executeQuery(
          conn,
          "SELECT name FROM users WHERE id = ?",
          List(List(1), List(2))
        )

        result must beSuccessfulTry.which(_.length must_=== 2)
    }
  }

  "Type Mapping" should {
    "map SQL types to Scala types" in {
      (TypeMapping.mapSqlType("VARCHAR") must beSuccessfulTry("String")) and {
        TypeMapping.mapSqlType("BIGINT") must beSuccessfulTry("Long")
      } and {
        TypeMapping.mapSqlType("BOOLEAN") must beSuccessfulTry("Boolean")
      }
    }

    "handle ambiguous types with defaults" in {
      TypeMapping.isAmbiguous("NUMERIC") must beTrue and {
        TypeMapping.getAmbiguousDefault("NUMERIC") must beSome("BigDecimal")
      }
    }

    "reject unsupported types" in {
      TypeMapping.mapSqlType("UNSUPPORTED_TYPE") must beFailedTry
    }
  }

  "Query Analysis" should {
    "parse SELECT queries" in {
      val result =
        QueryAnalyzer.analyzeQuery("SELECT id, name FROM users WHERE id = ?")

      result must beSuccessfulTry.which { structure =>
        structure.selectFields must contain("id") and {
          structure.selectFields must contain("name")
        } and {
          structure.parameterCount must_=== 1
        }
      }
    }

    "identify parameter positions" in {
      val positions =
        QueryAnalyzer.identifyParameterPositions("WHERE id = ? AND name = ?")

      positions.length must_=== 2 and {
        positions.headOption must beSome[Int].which {
          _ must be_<(positions.last)
        }
      }
    }

    "handle queries without parameters" in {
      val result = QueryAnalyzer.analyzeQuery("SELECT * FROM users")

      result must beSuccessfulTry.which {
        _.parameterCount must_=== 0
      }
    }
  }

  "MCP Protocol" should {
    "parse valid JSON-RPC requests" in {
      val json = """{"jsonrpc":"2.0","method":"test","params":{},"id":1}"""

      JsonRpcProtocol.parseRequest(json) must beSuccessfulTry.which {
        _.method must_=== "test"
      }
    }

    "serialize responses correctly" in {
      val response = JsonRpcProtocol.createSuccessResponse(
        Json.obj("result" -> "success"),
        Some(JsNumber(1))
      )
      val serialized = JsonRpcProtocol.serializeResponse(response)

      serialized must contain("2.0") and { serialized must contain("result") }
    }

    "handle error responses" in {
      val response =
        JsonRpcProtocol.createErrorResponse(-32600, "Invalid Request", None)

      response.error must beSome[JsonRpcError].which { err =>
        (err.code must_=== -32600) and (err.message must_=== "Invalid Request")
      } and {
        response.result must beNone
      }
    }
  }

  "Query Confirmation Workflow" should {
    "extract query structure without execution (awaiting confirmation)" in {
      val params = Json.obj(
        "query" -> "SELECT id, name FROM users WHERE id = ?"
      )
      val result = McpTools.RecordTool.handle(params)

      result must beSuccessfulTry.which { response =>
        (response \ "status").asOpt[String] must beSome(
          "awaiting_confirmation"
        ) and {
          val extracted = response \ "extracted"

          (extracted \ "selectFields")
            .validate[List[String]] must_=== JsSuccess(List("id", "name")) and {
            (extracted \ "whereConditions")
              .validate[List[JsObject]] must beLike {
              case JsSuccess(conditions, _) =>
                conditions.length must_=== 1 and {
                  (conditions(0) \ "field").asOpt[String] must beSome("id")
                } and {
                  (conditions(0) \ "operator").asOpt[String] must beSome("=")
                } and {
                  (extracted \ "parameterCount").asOpt[Int] must beSome(1)
                }
            }
          }
        } and {
          (response \ "message").asOpt[String] must beSome[String].which(
            _.contains("Extracted") must beTrue
          )
        }
      }
    }

    "execute query after confirmation" in {
      val url = setupTestDatabase()
      val connId = "test-conn-" + System.nanoTime()
      val conn = DriverManager.getConnection(url)

      try {
        val _ = McpTools.connections.put(connId, conn)

        val params = Json.obj(
          "connectionId" -> connId,
          "query" -> "SELECT * FROM users",
          "confirmed" -> true
        )
        val result = McpTools.RecordTool.handle(params)

        result must beSuccessfulTry.which { response =>
          (response \ "status").asOpt[String] must beSome("executed") and {
            (response \ "totalRows").asOpt[Int] must beSome(2)
          } and {
            (response \ "columns")
              .asOpt[List[JsObject]]
              .map(_.length) must beSome(3)
          } and {
            (response \ "dslHints" \ "useStringList")
              .asOpt[Boolean] must beSome(false)
          }
        }
      } finally {
        conn.close()

        val _ = McpTools.connections.remove(connId)
      }
    }

    "recommend stringList only for fully textual result sets" in {
      val url = setupTestDatabase()
      val connId = s"test-conn-${System.nanoTime()}"
      val conn = DriverManager.getConnection(url)

      try {
        val _ = McpTools.connections.put(connId, conn)

        val params = Json.obj(
          "connectionId" -> connId,
          "query" -> "SELECT name, email FROM users",
          "confirmed" -> true
        )
        val result = McpTools.RecordTool.handle(params)

        result must beSuccessfulTry.which { response =>
          (response \ "status").asOpt[String] must beSome("executed") and {
            (response \ "dslHints" \ "useStringList")
              .asOpt[Boolean] must beSome(true)
          } and {
            (response \ "dslHints" \ "recommendedRowListFactory")
              .asOpt[String] must beSome("rowList2")
          }
        }
      } finally {
        conn.close()

        val _ = McpTools.connections.remove(connId)
      }
    }

    "handle complex WHERE clause extraction" in {
      val params = Json.obj(
        "query" -> "SELECT id, name FROM users WHERE id = ? AND name LIKE ?"
      )
      val result = McpTools.RecordTool.handle(params)

      result must beSuccessfulTry.which { response =>
        val extracted = response \ "extracted"

        (extracted \ "whereConditions").validate[List[JsObject]] must beLike {
          case JsSuccess(conditions, _) =>
            conditions.length must_=== 2 and {
              (conditions(0) \ "operator").asOpt[String] must beSome("=")
            } and {
              (conditions(1) \ "operator").asOpt[String] must beSome("LIKE")
            } and {
              (extracted \ "parameterCount").asOpt[Int] must beSome(2)
            }
        }
      }
    }
  }

  "End-to-End Workflow" should {
    "simulate connect → discover → query workflow" in {
      val url = setupTestDatabase()

      val conn = DriverManager.getConnection(url)

      try {
        val metadata = conn.getMetaData
        val columns = metadata.getColumns(null, null, "USERS", null)
        val colNames = Iterator
          .continually(columns.next())
          .takeWhile(identity)
          .map { _ => columns.getString("COLUMN_NAME") }
          .toList

        colNames.length must be_>(0) and {
          QueryExecutor.executeQuery(
            conn,
            "SELECT * FROM users",
            List(List.empty)
          ) must beSuccessfulTry.which {
            _.length must_=== 1
          }
        }
      } finally {
        conn.close()
      }
    }
  }

  // ---

  private def withIsolatedConnection[T](f: Connection => T): T = {
    lazy val conn = DriverManager.getConnection(setupTestDatabase())

    try {
      f(conn)
    } finally {
      conn.close()
    }
  }

  private def setupTestDatabase(): String = {
    val dbNum = dbCounter.incrementAndGet()
    val url = s"jdbc:h2:mem:test$dbNum;DB_CLOSE_DELAY=-1"
    val conn = DriverManager.getConnection(url)

    try {
      val stmt = conn.createStatement()

      stmt.execute(
        "CREATE TABLE users (id INT, name VARCHAR(100), email VARCHAR(100))"
      )
      stmt.execute("INSERT INTO users VALUES (1, 'Alice', 'alice@example.com')")
      stmt.execute("INSERT INTO users VALUES (2, 'Bob', 'bob@example.com')")

      stmt.close()
    } finally {
      conn.close()
    }

    url
  }
}
