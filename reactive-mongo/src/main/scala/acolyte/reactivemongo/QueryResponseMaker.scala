package acolyte.reactivemongo

import scala.util.Try

import reactivemongo.bson.BSONDocument
import reactivemongo.core.protocol.Response

/**
 * Creates a query response for given channel ID and result.
 * @tparam T Result type
 */
trait QueryResponseMaker[T] extends ((Int, T) ⇒ Option[Try[Response]]) {
  /**
   * @param channelId ID of Mongo channel
   * @param result Optional result to be wrapped into response
   */
  override def apply(channelId: Int, result: T): Option[Try[Response]]
}

/** Response maker companion. */
object QueryResponseMaker {
  /**
   * {{{
   * import reactivemongo.bson.BSONDocument
   * import acolyte.reactivemongo.QueryResponseMaker
   *
   * val maker = implicitly[QueryResponseMaker[Traversable[BSONDocument]]]
   * }}}
   */
  implicit def TraversableQueryResponseMaker[T <: Traversable[BSONDocument]] =
    new QueryResponseMaker[T] {
      override def apply(channelId: Int, result: T): Option[Try[Response]] =
        Some(MongoDB.QuerySuccess(channelId, result))
    }

  /**
   * Provides response maker for an error.
   *
   * {{{
   * import acolyte.reactivemongo.QueryResponseMaker
   *
   * val maker = implicitly[QueryResponseMaker[String]]
   * }}}
   */
  implicit def ErrorQueryResponseMaker = new QueryResponseMaker[String] {
    override def apply(channelId: Int, error: String): Option[Try[Response]] =
      Some(MongoDB.QueryError(channelId, error))
  }

  /**
   * Provides response maker for handler not supporting specific query.
   *
   * {{{
   * import acolyte.reactivemongo.QueryResponseMaker
   *
   * val maker = implicitly[QueryResponseMaker[None.type]]
   * val response = maker(1, None)
   * }}}
   */
  implicit def UndefinedQueryResponseMaker = new QueryResponseMaker[None.type] {
    /** @return None */
    override def apply(channelId: Int, undefined: None.type): Option[Try[Response]] = None
  }
}
