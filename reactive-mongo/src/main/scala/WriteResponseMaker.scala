package acolyte.reactivemongo

import scala.util.Try

import reactivemongo.core.protocol.Response

/**
 * Creates a write response for given channel ID and result.
 * @tparam T Result type
 */
trait WriteResponseMaker[T] extends ((Int, T) ⇒ Option[Try[Response]]) {
  /**
   * @param channelId ID of Mongo channel
   * @param result Optional result to be wrapped into response
   */
  def apply(channelId: Int, result: T): Option[Try[Response]]
}

/** Response maker companion. */
object WriteResponseMaker {
  /** Identity maker for already prepared response. */
  implicit object IdentityWriteResponseMaker
      extends WriteResponseMaker[PreparedResponse] {

    def apply(channelId: Int, already: PreparedResponse): Option[Try[Response]] = already(channelId)
  }

  /**
   * {{{
   * import acolyte.reactivemongo.WriteResponseMaker
   *
   * val maker = implicitly[WriteResponseMaker[(Int, Boolean)]]
   * }}}
   */
  implicit def SuccessWriteResponseMaker = new WriteResponseMaker[(Int, Boolean)] {
    def apply(channelId: Int, up: (Int, Boolean)): Option[Try[Response]] = Some(MongoDB.WriteSuccess(channelId, up._1, up._2))
  }

  /**
   * {{{
   * import acolyte.reactivemongo.WriteResponseMaker
   *
   * val maker = implicitly[WriteResponseMaker[Int]]
   * }}}
   */
  implicit def SuccessNotUpdatedWriteResponseMaker =
    new WriteResponseMaker[Int] {
      def apply(channelId: Int, count: Int): Option[Try[Response]] =
        Some(MongoDB.WriteSuccess(channelId, count))
    }

  /**
   * {{{
   * import acolyte.reactivemongo.WriteResponseMaker
   *
   * val maker = implicitly[WriteResponseMaker[Unit]]
   * }}}
   */
  implicit def UnitWriteResponseMaker = new WriteResponseMaker[Unit] {
    def apply(channelId: Int, effect: Unit): Option[Try[Response]] = Some(MongoDB.WriteSuccess(channelId, 0, false))
  }

  /**
   * Provides response maker for an error.
   *
   * {{{
   * import acolyte.reactivemongo.WriteResponseMaker
   *
   * val maker = implicitly[WriteResponseMaker[String]]
   * }}}
   */
  implicit def ErrorWriteResponseMaker = new WriteResponseMaker[String] {
    def apply(channelId: Int, error: String): Option[Try[Response]] =
      Some(MongoDB.WriteError(channelId, error, None))
  }

  /**
   * Provides response maker for an error.
   *
   * {{{
   * import acolyte.reactivemongo.WriteResponseMaker
   *
   * val maker = implicitly[WriteResponseMaker[(String, Int)]]
   * }}}
   */
  implicit def ErrorCodeWriteResponseMaker =
    new WriteResponseMaker[(String, Int)] {
      def apply(channelId: Int, error: (String, Int)): Option[Try[Response]] = Some(MongoDB.WriteError(channelId, error._1, Some(error._2)))
    }

  /**
   * Provides response maker for handler not supporting
   * specific write operation.
   *
   * {{{
   * import acolyte.reactivemongo.WriteResponseMaker
   *
   * val maker = implicitly[WriteResponseMaker[None.type]]
   * val response = maker(1, None)
   * }}}
   */
  implicit def UndefinedWriteResponseMaker = new WriteResponseMaker[None.type] {
    /** @return None */
    def apply(channelId: Int, undefined: None.type): Option[Try[Response]] = None
  }
}
