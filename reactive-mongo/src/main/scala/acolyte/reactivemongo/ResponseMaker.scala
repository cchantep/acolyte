package acolyte.reactivemongo

import scala.util.Try

import reactivemongo.bson.BSONDocument
import reactivemongo.core.protocol.Response

/**
 * Creates a response for given channel ID and result.
 * @tparam T Result type
 */
trait ResponseMaker[T] extends ((Int, T) ⇒ Option[Try[Response]]) {
  /**
   * @param channelId ID of Mongo channel
   * @param result Optional result to be wrapped into response
   */
  override def apply(channelId: Int, result: T): Option[Try[Response]]
}

/** Response maker companion. */
object ResponseMaker {
  /**
   * {{{
   * import reactivemongo.bson.BSONDocument
   * import acolyte.reactivemongo.ResponseMaker
   *
   * val maker = implicitly[ResponseMaker[Traversable[BSONDocument]]]
   * }}}
   */
  implicit def TraversableResponseMaker[T <: Traversable[BSONDocument]] =
    new ResponseMaker[T] {
      override def apply(channelId: Int, result: T): Option[Try[Response]] =
        Some(MongoDB.Success(channelId, result))
    }

  /**
   * Provides response maker for an error.
   *
   * {{{
   * import acolyte.reactivemongo.ResponseMaker
   *
   * val maker = implicitly[ResponseMaker[String]]
   * }}}
   */
  implicit def ErrorResponseMaker = new ResponseMaker[String] {
    override def apply(channelId: Int, error: String): Option[Try[Response]] =
      Some(MongoDB.Error(channelId, error))
  }

  /**
   * Provides response maker for handler not supporting specific query.
   *
   * {{{
   * import acolyte.reactivemongo.ResponseMaker
   *
   * val maker = implicitly[ResponseMaker[None.type]]
   * val response = maker(1, None)
   * }}}
   */
  implicit def UndefinedResponseMaker = new ResponseMaker[None.type] {
    /** @return None */
    override def apply(channelId: Int, undefined: None.type): Option[Try[Response]] = None
  }
}
