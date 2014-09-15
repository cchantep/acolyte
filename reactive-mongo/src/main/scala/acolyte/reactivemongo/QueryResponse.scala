package acolyte.reactivemongo

import scala.util.Try

import _root_.reactivemongo.bson.BSONDocument
import _root_.reactivemongo.core.protocol.Response

/** Query response factory. */
object QueryResponse {
  /** Creates a response for given `body`. */
  def apply[T](body: ⇒ T)(implicit mkResponse: QueryResponseMaker[T]): PreparedResponse = new PreparedResponse {
    def apply(chanId: Int) = mkResponse(chanId, body)
  }

  /** Named factory for error response. */
  def failed(message: String) = apply(message)

  /**
   * Named factory for error response.
   *
   * @param message Error message
   * @param code Error code
   */
  def failed(message: String, code: Int) = apply(message -> code)

  /** Factory for successful response. */
  def successful(result: BSONDocument*) = apply(result)

  /**
   * Empty/undefined response, returned by handler no supporting
   * a specific query that may be handled by others.
   */
  lazy val empty = apply(None)
}
