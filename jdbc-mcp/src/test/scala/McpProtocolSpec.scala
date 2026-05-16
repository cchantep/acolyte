package acolyte.jdbc.mcp

import play.api.libs.json._

final class McpProtocolSpec extends org.specs2.mutable.Specification {
  "McpProtocol".title

  "parseRequest" should {
    "parse valid JSON-RPC request" in {
      val json = """{"jsonrpc":"2.0","method":"test","id":1}"""

      JsonRpcProtocol.parseRequest(json) must beSuccessfulTry.which { req =>
        req.method must_=== "test" and {
          req.jsonrpc must_=== "2.0"
        } and {
          req.id must beSome(JsNumber(1))
        }
      }
    }

    "parse request with params" in {
      val json =
        """{"jsonrpc":"2.0","method":"connect","params":{"host":"localhost"},"id":1}"""

      JsonRpcProtocol.parseRequest(json) must beSuccessfulTry.which { req =>
        req.method must_=== "connect" and {
          req.params must beSome[JsValue].which { p =>
            (p \ "host").asOpt[String] must beSome("localhost")
          }
        }
      }
    }

    "reject malformed JSON" in {
      JsonRpcProtocol.parseRequest("""{"invalid json}""") must beFailedTry
    }

    "handle missing id" in {
      val json = """{"jsonrpc":"2.0","method":"notify"}"""

      JsonRpcProtocol.parseRequest(json) must beSuccessfulTry.which(
        _.id must beNone
      )
    }
  }

  "serializeResponse" should {
    "serialize success response" in {
      val response = JsonRpcProtocol.createSuccessResponse(
        Json.toJson("ok"),
        Some(JsNumber(1))
      )

      val json = JsonRpcProtocol.serializeResponse(response)

      json.contains("\"result\"") must beTrue and {
        json.contains("\"ok\"") must beTrue
      }
    }

    "serialize error response" in {
      val response = JsonRpcProtocol.createErrorResponse(
        -32601,
        "Method not found",
        Some(JsNumber(1))
      )

      val json = JsonRpcProtocol.serializeResponse(response)

      json.contains("\"error\"") must beTrue and {
        json.contains("Method not found") must beTrue
      }
    }

    "include id in response" in {
      val response = JsonRpcProtocol.createSuccessResponse(
        Json.toJson("data"),
        Some(JsNumber(42))
      )
      val json = JsonRpcProtocol.serializeResponse(response)

      json.contains("42") must beTrue
    }
  }

  "createErrorResponse" should {
    "create error with correct code" in {
      val response =
        JsonRpcProtocol.createErrorResponse(-32700, "Parse error", None)

      response.error.map(_.code) must beSome(-32700)
    }

    "set result to None on error" in {
      val response =
        JsonRpcProtocol.createErrorResponse(-32600, "Invalid request", None)

      response.result must beNone
    }
  }

  "createSuccessResponse" should {
    "create response with result" in {
      val result = Json.toJson(Json.obj("status" -> "success"))
      val response =
        JsonRpcProtocol.createSuccessResponse(result, Some(JsNumber(1)))

      response.result must beSome[JsValue].which { r =>
        (r \ "status").asOpt[String] must beSome("success")
      } and {
        response.error must beNone
      }
    }

    "set error to None on success" in {
      val response = JsonRpcProtocol.createSuccessResponse(Json.obj(), None)

      response.error must beNone
    }
  }
}
