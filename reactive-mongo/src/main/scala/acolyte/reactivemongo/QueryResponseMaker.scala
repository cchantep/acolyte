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
  def apply(channelId: Int, result: T): Option[Try[Response]]
}

/** Response maker companion. */
object QueryResponseMaker {
  /** Identity maker for already prepared response. */
  implicit object IdentityQueryResponseMaker 
      extends QueryResponseMaker[PreparedResponse] {

    def apply(channelId: Int, already: PreparedResponse): Option[Try[Response]] = already(channelId)
  }

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
      def apply(channelId: Int, result: T): Option[Try[Response]] =
        Some(MongoDB.QuerySuccess(channelId, result))
    }

  /**
   * {{{
   * import reactivemongo.bson.BSONDocument
   * import acolyte.reactivemongo.QueryResponseMaker
   *
   * val maker = implicitly[QueryResponseMaker[BSONDocument]]
   * }}}
   */
  implicit def SingleQueryResponseMaker = new QueryResponseMaker[BSONDocument] {
      def apply(channelId: Int, result: BSONDocument): Option[Try[Response]] = Some(MongoDB.QuerySuccess(channelId, Seq(result)))
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
    def apply(channelId: Int, error: String): Option[Try[Response]] =
      Some(MongoDB.QueryError(channelId, error))
  }

  /**
   * Provides response maker for an error.
   *
   * {{{
   * import acolyte.reactivemongo.QueryResponseMaker
   *
   * val maker = implicitly[QueryResponseMaker[(String, Int)]]
   * }}}
   */
  implicit def ErrorCodeQueryResponseMaker = new QueryResponseMaker[(String, Int)] {
    def apply(channelId: Int, error: (String, Int)): Option[Try[Response]] =
      Some(MongoDB.QueryError(channelId, error._1, Some(error._2)))
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
    def apply(channelId: Int, undefined: None.type): Option[Try[Response]] = None
  }
}
