package acolyte.reactivemongo

import scala.util.Try

import _root_.reactivemongo.bson.BSONDocument
import _root_.reactivemongo.core.protocol.Response

/** Response to Mongo query executed with Acolyte driver. */
sealed trait QueryResponse {
  /** Applies this response to specified Mongo channel. */
  def apply(chanId: Int): Option[Try[Response]]
}

/** Query response companion. */
object QueryResponse {
  /** Mongo Error, in response to some request. */
  /** Successful result */

  /** Creates a response for given `body`. */
  def apply[T](body: ⇒ T)(implicit mkResponse: ResponseMaker[T]): QueryResponse = new QueryResponse {
    def apply(chanId: Int) = mkResponse(chanId, body)
  }

  /** Named factory for error response. */
  def failed(message: String) = apply(message)

  /** Factory for successful response. */
  def successful(result: BSONDocument*) = apply(result)

  /**
   * Empty/undefined response, returned by handler no supporting a specific
   * query that may be handled by others.
   */
  lazy val empty = apply(None)
}
