package acolyte.reactivemongo

import scala.util.Try

import reactivemongo.io.netty.channel.ChannelId

import reactivemongo.acolyte.Response

/**
 * Creates a write response for given channel ID and result.
 * @tparam T Result type
 */
trait WriteResponseMaker[T] extends ((ChannelId, T) => Option[Try[Response]]) {

  /**
   * @param chanId ID of Mongo channel
   * @param result Optional result to be wrapped into response
   */
  def apply(chanId: ChannelId, result: T): Option[Try[Response]]
}

/** Response maker companion. */
object WriteResponseMaker {

  /** Identity maker for already prepared response. */
  implicit object IdentityWriteResponseMaker
      extends WriteResponseMaker[PreparedResponse] {

    def apply(
        chanId: ChannelId,
        already: PreparedResponse
      ): Option[Try[Response]] = already(chanId)
  }

  /**
   * {{{
   * import acolyte.reactivemongo.WriteResponseMaker
   *
   * val maker = implicitly[WriteResponseMaker[(Int, Boolean)]]
   * }}}
   */
  implicit val successWriteResponseMaker: WriteResponseMaker[(Int, Boolean)] =
    new WriteResponseMaker[(Int, Boolean)] {

      def apply(chanId: ChannelId, up: (Int, Boolean)): Option[Try[Response]] =
        Some(MongoDB.writeSuccess(chanId, up._1, up._2))
    }

  /**
   * {{{
   * import acolyte.reactivemongo.WriteResponseMaker
   *
   * val maker = implicitly[WriteResponseMaker[Int]]
   * }}}
   */
  implicit val successNotUpdatedWriteResponseMaker: WriteResponseMaker[Int] =
    new WriteResponseMaker[Int] {

      def apply(chanId: ChannelId, count: Int): Option[Try[Response]] =
        Some(MongoDB.writeSuccess(chanId, count))
    }

  /**
   * {{{
   * import acolyte.reactivemongo.WriteResponseMaker
   *
   * val maker = implicitly[WriteResponseMaker[Unit]]
   * }}}
   */
  implicit val unitWriteResponseMaker: WriteResponseMaker[Unit] =
    new WriteResponseMaker[Unit] {

      def apply(chanId: ChannelId, effect: Unit): Option[Try[Response]] =
        Some(MongoDB.writeSuccess(chanId, 0, false))
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
  implicit val errorWriteResponseMaker: WriteResponseMaker[String] =
    new WriteResponseMaker[String] {

      def apply(chanId: ChannelId, error: String): Option[Try[Response]] =
        Some(MongoDB.writeError(chanId, error, None))
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
  implicit val errorCodeWriteResponseMaker: WriteResponseMaker[(String, Int)] =
    new WriteResponseMaker[(String, Int)] {

      def apply(
          chanId: ChannelId,
          error: (String, Int)
        ): Option[Try[Response]] =
        Some(MongoDB.writeError(chanId, error._1, Some(error._2)))
    }

  /**
   * Provides response maker for handler not supporting
   * specific write operation.
   *
   * {{{
   * import acolyte.reactivemongo.WriteResponseMaker
   *
   * val maker = implicitly[WriteResponseMaker[None.type]]
   * }}}
   */
  implicit val undefinedWriteResponseMaker: WriteResponseMaker[None.type] =
    new WriteResponseMaker[None.type] {

      /** @return None */
      def apply(
          chanId: ChannelId,
          undefined: None.type
        ): Option[Try[Response]] = None
    }
}
