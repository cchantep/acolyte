package acolyte.jdbc.mcp

import scala.util.Try

import play.api.libs.json.{ Format, JsValue, Json }

final case class JsonRpcRequest(
    jsonrpc: String,
    method: String,
    params: Option[JsValue],
    id: Option[JsValue])

final case class JsonRpcResponse(
    jsonrpc: String,
    result: Option[JsValue],
    error: Option[JsonRpcError],
    id: Option[JsValue])

final case class JsonRpcError(
    code: Int,
    message: String,
    data: Option[JsValue])

object JsonRpcProtocol {

  implicit val jsonRpcErrorFormat: Format[JsonRpcError] =
    Json.format[JsonRpcError]

  implicit val jsonRpcRequestFormat: Format[JsonRpcRequest] =
    Json.format[JsonRpcRequest]

  implicit val jsonRpcResponseFormat: Format[JsonRpcResponse] =
    Json.format[JsonRpcResponse]

  def parseRequest(json: String): Try[JsonRpcRequest] =
    Try(Json.parse(json).as[JsonRpcRequest])

  def serializeResponse(response: JsonRpcResponse): String =
    Json.stringify(Json.toJson(response))

  def createErrorResponse(
      code: Int,
      message: String,
      id: Option[JsValue]
    ): JsonRpcResponse =
    JsonRpcResponse(
      jsonrpc = "2.0",
      result = None,
      error = Some(JsonRpcError(code, message, None)),
      id = id
    )

  def createSuccessResponse(
      result: JsValue,
      id: Option[JsValue]
    ): JsonRpcResponse =
    JsonRpcResponse(
      jsonrpc = "2.0",
      result = Some(result),
      error = None,
      id = id
    )
}
