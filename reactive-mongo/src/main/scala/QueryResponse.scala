package acolyte.reactivemongo

import _root_.reactivemongo.bson.BSONDocument

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
  def failed(message: String, code: Int) = apply(message → code)

  /** Factory for successful response. */
  def successful(result: BSONDocument*) = apply(result)

  /**
   * Prepares a response to a successful count command.
   *
   * @param result Count result
   */
  def count(result: Int = 0) = apply(BSONDocument("ok" → 1, "n" → result))

  /**
   * Prepares a response to a successful findAndModify command.
   *
   * @param result FindAndModify result
   */
  def findAndModify(result: BSONDocument) =
    apply(BSONDocument("ok" → 1, "value" → result))

  /**
   * Undefined response, returned by handler no supporting
   * a specific query that may be handled by others.
   */
  lazy val undefined = apply(None)

  /** Successful empty response (list of zero document). */
  lazy val empty = apply(List.empty[BSONDocument])
}
