package acolyte.reactivemongo

import scala.util.Try

import reactivemongo.io.netty.channel.ChannelId
import reactivemongo.bson.BSONDocument
import reactivemongo.core.protocol.Response

/**
 * Creates a query response for given channel ID and result.
 * @tparam T Result type
 */
trait QueryResponseMaker[T] extends ((ChannelId, T) ⇒ Option[Try[Response]]) {
  /**
   * @param chanId ID of Mongo channel
   * @param result Optional result to be wrapped into response
   */
  def apply(chanId: ChannelId, result: T): Option[Try[Response]]
}

/** Response maker companion. */
object QueryResponseMaker {
  /** Identity maker for already prepared response. */
  implicit object IdentityQueryResponseMaker
    extends QueryResponseMaker[PreparedResponse] {

    def apply(chanId: ChannelId, already: PreparedResponse): Option[Try[Response]] = already(chanId)
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
      def apply(chanId: ChannelId, result: T): Option[Try[Response]] =
        Some(MongoDB.QuerySuccess(chanId, result))
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
    def apply(chanId: ChannelId, result: BSONDocument): Option[Try[Response]] = Some(MongoDB.QuerySuccess(chanId, Seq(result)))
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
    def apply(chanId: ChannelId, error: String): Option[Try[Response]] =
      Some(MongoDB.QueryError(chanId, error))
  }

  /**
   * Provides response maker for an error.
   *
   * {{{
   * import reactivemongo.io.netty.channel.ChannelId
   * import acolyte.reactivemongo.QueryResponseMaker
   *
   * val maker = implicitly[QueryResponseMaker[(String, Int)]]
   * }}}
   */
  implicit def ErrorCodeQueryResponseMaker = new QueryResponseMaker[(String, Int)] {
    def apply(chanId: ChannelId, error: (String, Int)): Option[Try[Response]] =
      Some(MongoDB.QueryError(chanId, error._1, Some(error._2)))
  }

  /**
   * Provides response maker for handler not supporting specific query.
   *
   * {{{
   * import acolyte.reactivemongo.QueryResponseMaker
   *
   * val maker = implicitly[QueryResponseMaker[None.type]]
   * }}}
   */
  implicit def UndefinedQueryResponseMaker = new QueryResponseMaker[None.type] {
    /** @return None */
    def apply(chanId: ChannelId, undefined: None.type): Option[Try[Response]] = None
  }
}
