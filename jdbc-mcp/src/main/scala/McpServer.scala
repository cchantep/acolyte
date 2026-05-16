package acolyte.jdbc.mcp

import java.util.concurrent.ConcurrentHashMap

import scala.util.{ Failure, Success, Try }
import scala.util.control.NonFatal

import scala.io.StdIn

import play.api.libs.json._

trait JsonRpcHandler {
  def handle(params: JsValue): Try[JsValue]
}

final class McpServer {
  private val handlers = new ConcurrentHashMap[String, JsonRpcHandler]()

  def registerTool(name: String, handler: JsonRpcHandler): Unit = {
    val _ = handlers.put(name, handler)
  }

  def start(): Unit = {
    try {
      var line = StdIn.readLine()

      while (line != null) {
        processRequest(line)
        line = StdIn.readLine()
      }
    } finally {
      // Resource cleanup if needed
    }
  }

  private def processRequest(json: String): Unit = {
    val response = JsonRpcProtocol.parseRequest(json) match {
      case Success(request) => handleRequest(request)

      case Failure(e) =>
        JsonRpcProtocol.createErrorResponse(
          -32700,
          s"Parse error: ${e.getMessage}",
          None
        )
    }

    writeResponse(response)
  }

  private def handleRequest(request: JsonRpcRequest): JsonRpcResponse = {
    request.method match {
      case "initialize" =>
        JsonRpcProtocol.createSuccessResponse(initializeResponse(), request.id)

      case "tools/list" =>
        JsonRpcProtocol.createSuccessResponse(toolsListResponse(), request.id)

      case "tools/call" =>
        handleToolCall(request)

      case "notifications/initialized" =>
        JsonRpcProtocol.createSuccessResponse(Json.obj(), request.id)

      case "ping" =>
        JsonRpcProtocol.createSuccessResponse(Json.obj(), request.id)

      case method =>
        Option(handlers.get(method)).fold(
          JsonRpcProtocol.createErrorResponse(
            -32601,
            s"Method not found: $method",
            request.id
          )
        )(handler =>
          try {
            handler.handle(paramsObject(request.params)) match {
              case Success(value) =>
                JsonRpcProtocol.createSuccessResponse(value, request.id)

              case Failure(e) =>
                JsonRpcProtocol.createErrorResponse(
                  -32603,
                  s"Internal error: ${e.getMessage}",
                  request.id
                )
            }
          } catch {
            case NonFatal(e) =>
              JsonRpcProtocol.createErrorResponse(
                -32603,
                s"Internal error: ${e.getMessage}",
                request.id
              )
          }
        )
    }
  }

  private def initializeResponse(): JsObject = Json.obj(
    "protocolVersion" -> "2024-11-05",
    "capabilities" -> Json.obj("tools" -> Json.obj()),
    "serverInfo" -> Json.obj(
      "name" -> "acolyte-jdbc",
      "version" -> "1.0.0"
    )
  )

  private def toolsListResponse(): JsObject = Json.obj(
    "tools" -> Json.arr(
      Json.obj(
        "name" -> "connect",
        "description" -> "Open JDBC connection",
        "inputSchema" -> Json.obj(
          "type" -> "object",
          "properties" -> Json.obj(
            "url" -> Json.obj("type" -> "string"),
            "user" -> Json.obj("type" -> "string"),
            "password" -> Json.obj("type" -> "string"),
            "driver" -> Json.obj("type" -> "string"),
            "dependency" -> Json.obj("type" -> "string"),
            "repository" -> Json.obj("type" -> "string")
          ),
          "required" -> Json.arr("url")
        )
      ),
      Json.obj(
        "name" -> "discover",
        "description" -> "Discover table columns from JDBC metadata",
        "inputSchema" -> Json.obj(
          "type" -> "object",
          "properties" -> Json.obj(
            "connectionId" -> Json.obj("type" -> "string"),
            "table" -> Json.obj("type" -> "string")
          ),
          "required" -> Json.arr("connectionId", "table")
        )
      ),
      Json.obj(
        "name" -> "record",
        "description" -> "Extract query structure, then execute after confirmation",
        "inputSchema" -> Json.obj(
          "type" -> "object",
          "properties" -> Json.obj(
            "connectionId" -> Json.obj("type" -> "string"),
            "query" -> Json.obj("type" -> "string"),
            "confirmed" -> Json.obj("type" -> "boolean")
          ),
          "required" -> Json.arr("query")
        )
      ),
      Json.obj(
        "name" -> "close",
        "description" -> "Close JDBC connection",
        "inputSchema" -> Json.obj(
          "type" -> "object",
          "properties" -> Json.obj(
            "connectionId" -> Json.obj("type" -> "string")
          ),
          "required" -> Json.arr("connectionId")
        )
      )
    )
  )

  private def handleToolCall(request: JsonRpcRequest): JsonRpcResponse = {
    val params = paramsObject(request.params)
    val toolName = (params \ "name").asOpt[String]
    val arguments = (params \ "arguments").asOpt[JsObject]

    toolName.fold(
      JsonRpcProtocol
        .createErrorResponse(-32602, "Missing tool name", request.id)
    )(name =>
      Option(handlers.get(name)).fold(
        JsonRpcProtocol
          .createErrorResponse(-32601, s"Tool not found: $name", request.id)
      )(handler =>
        handler.handle(arguments.fold(Json.obj())(identity)) match {
          case Success(value) => {
            val errorMessage = (value \ "error").asOpt[String]
            val response = Json.obj(
              "content" -> Json.arr(
                Json.obj(
                  "type" -> "text",
                  "text" -> Json.stringify(value)
                )
              ),
              "structuredContent" -> value,
              "isError" -> errorMessage.isDefined
            )

            JsonRpcProtocol.createSuccessResponse(response, request.id)
          }

          case Failure(e) =>
            JsonRpcProtocol.createErrorResponse(
              -32603,
              s"Tool execution failed: ${e.getMessage}",
              request.id
            )
        }
      )
    )
  }

  private def paramsObject(params: Option[JsValue]): JsObject =
    params.collect { case obj: JsObject => obj }.fold(Json.obj())(identity)

  private def writeResponse(response: JsonRpcResponse): Unit =
    println(JsonRpcProtocol `serializeResponse` response)
}

object McpServer {

  def main(args: Array[String]): Unit = {
    val server = new McpServer()

    server.registerTool("connect", McpTools.ConnectTool)
    server.registerTool("discover", McpTools.DiscoverTool)
    server.registerTool("record", McpTools.RecordTool)
    server.registerTool("close", McpTools.CloseTool)

    server.start()
  }
}
