package acolyte.jdbc.mcp

import play.api.libs.json._

final class ConnectToolSpec extends org.specs2.mutable.Specification {
  "ConnectTool".title

  "MCP Connect Tool" should {
    "reject unresolved environment variable references" in {
      val result = McpTools.ConnectTool.handle(
        Json.obj("url" -> f"$${ACOLYTE_TEST_UNSET_DB_URL}")
      )

      result must beSuccessfulTry.which { response =>
        (response \ "status").asOpt[String] must beSome(
          "invalid_parameters"
        ) and {
          (response \ "error").asOpt[String] must beSome[String].which(
            _.contains("Missing environment variable")
          )
        }
      }
    }

    "resolve env:VAR references before connection handling" in {
      val result = McpTools.ConnectTool.handle(Json.obj("url" -> "env:HOME"))

      result must beSuccessfulTry.which { response =>
        (response \ "status").asOpt[String] must beSome("driver_missing") and {
          (response \ "url").asOpt[String] must beSome(sys.env("HOME"))
        }
      }
    }
  }
}
