package acolyte.reactivemongo

import scala.util.Try

import _root_.reactivemongo.bson.BSONDocument
import _root_.reactivemongo.core.protocol.Response

/** Write response factory. */
object WriteResponse {
  /** Creates a response for given `body`. */
  def apply[T](body: ⇒ T)(implicit mkResponse: WriteResponseMaker[T]): PreparedResponse = new PreparedResponse {
    def apply(chanId: Int) = mkResponse(chanId, body)
  }

  /**
   * Named factory for error response.
   *
   * @param message Error message
   */
  def failed(message: String) = apply(message)

  /**
   * Named factory for error response.
   *
   * @param message Error message
   * @param code Error code
   */
  def failed(message: String, code: Int) = apply(message -> code)

  /**
   * Factory for successful response.
   *
   * @param updatedExisting Some existing document has been updated
   */
  def successful(updatedExisting: Boolean = false) = apply(updatedExisting)

  /**
   * Empty/undefined response, returned by handler no supporting
   * a specific write operation that may be handled by others.
   */
  lazy val empty = apply(None)
}
