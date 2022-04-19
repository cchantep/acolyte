package acolyte.reactivemongo

import java.util.UUID

import reactivemongo.io.netty.channel.ChannelId

import _root_.reactivemongo.api.bson.{ BSONBinary, BSONDocument, BSONWriter }

/** Query response factory. */
object QueryResponse {

  /** Creates a response for given `body`. */
  def apply[T](
      body: => T
    )(implicit
      mkResponse: QueryResponseMaker[T]
    ): PreparedResponse = new PreparedResponse {
    def apply(chanId: ChannelId) = mkResponse(chanId, body)
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
   * Prepares a response to a successful count command.
   *
   * @param result Count result
   */
  def count(result: Int = 0) = apply(BSONDocument("ok" -> 1, "n" -> result))

  /**
   * Prepares a response to a successful findAndModify command.
   *
   * @param result FindAndModify result
   */
  def findAndModify[T: BSONWriter](result: T) =
    apply(BSONDocument("ok" -> 1, "value" -> result))

  /**
   * Prepares a response to a successful startSession command.
   */
  def startSession(
      uuid: UUID = UUID.randomUUID(),
      timeoutMinutes: Int = 1
    ) = QueryResponse(
    BSONDocument(
      "ok" -> 1,
      "timeoutMinutes" -> timeoutMinutes,
      "id" -> BSONDocument("id" -> BSONBinary(uuid))
    )
  )

  /**
   * Prepares a response for a successful firstBatch.
   */
  def firstBatch(
      cursorId: Long,
      ns: String,
      firstBatch: Seq[BSONDocument]
    ) =
    QueryResponse(Tuple3(cursorId, ns, firstBatch))(
      QueryResponseMaker.firstBatchMaker
    )

  /**
   * Undefined response, returned by handler no supporting
   * a specific query that may be handled by others.
   */
  lazy val undefined = apply(None)

  /** Successful empty response (list of zero document). */
  lazy val empty = apply(List.empty[BSONDocument])
}
