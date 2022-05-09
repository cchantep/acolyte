package acolyte.reactivemongo

import scala.util.{ Failure, Success, Try }

import reactivemongo.io.netty.channel.ChannelId
import reactivemongo.api.bson.{ BSONDocument, BSONDocumentWriter }

import reactivemongo.acolyte.Response

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
object QueryResponseMaker extends LowPrioQueryResponseMaker {
  /** Identity maker for already prepared response. */
  implicit object IdentityQueryResponseMaker
    extends QueryResponseMaker[PreparedResponse] {

    def apply(chanId: ChannelId, already: PreparedResponse): Option[Try[Response]] = already(chanId)
  }

  /**
   * {{{
   * import reactivemongo.api.bson.BSONDocument
   * import acolyte.reactivemongo.QueryResponseMaker
   *
   * val maker = implicitly[QueryResponseMaker[Traversable[BSONDocument]]]
   * }}}
   */
  implicit def traversableQueryResponseMaker[T <: Traversable[BSONDocument]] =
    new QueryResponseMaker[T] {
      def apply(chanId: ChannelId, result: T): Option[Try[Response]] =
        Some(MongoDB.querySuccess(chanId, result))
    }

  /**
   * {{{
   * import reactivemongo.api.bson.BSONDocument
   * import acolyte.reactivemongo.QueryResponseMaker
   *
   * val maker = implicitly[QueryResponseMaker[BSONDocument]]
   * }}}
   */
  implicit def singleQueryResponseMaker = new QueryResponseMaker[BSONDocument] {
    def apply(chanId: ChannelId, result: BSONDocument): Option[Try[Response]] = Some(MongoDB.querySuccess(chanId, Seq(result)))
  }

  /**
   * {{{
   * import acolyte.reactivemongo.QueryResponseMaker
   *
   * case class MyCaseClass(name: String)
   *
   * import reactivemongo.api.bson.BSONDocumentWriter
   *
   * implicit def writer: BSONDocumentWriter[MyCaseClass] = ???
   *
   * val maker = implicitly[QueryResponseMaker[MyCaseClass]]
   * }}}
   */
  implicit def writableSingleQueryResponseMaker[T](
    implicit
    w: BSONDocumentWriter[T]): QueryResponseMaker[T] =
    new QueryResponseMaker[T] {

      def apply(chanId: ChannelId, result: T): Option[Try[Response]] =
        w.writeTry(result) match {
          case Failure(cause) =>
            Some(Failure(cause))

          case Success(value) =>
            singleQueryResponseMaker(chanId, value)
        }
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
  implicit def errorQueryResponseMaker = new QueryResponseMaker[String] {
    def apply(chanId: ChannelId, error: String): Option[Try[Response]] =
      Some(MongoDB.queryError(chanId, error))
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
  implicit def errorCodeQueryResponseMaker = new QueryResponseMaker[(String, Int)] {
    def apply(chanId: ChannelId, error: (String, Int)): Option[Try[Response]] =
      Some(MongoDB.queryError(chanId, error._1, Some(error._2)))
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
  implicit def undefinedQueryResponseMaker = new QueryResponseMaker[None.type] {
    /** @return None */
    def apply(chanId: ChannelId, undefined: None.type): Option[Try[Response]] = None
  }

  def firstBatchMaker =
    new QueryResponseMaker[(Long, String, Seq[BSONDocument])] {
      def apply(chanId: ChannelId, data: (Long, String, Seq[BSONDocument])): Option[Try[Response]] = Some(MongoDB.firstBatch(chanId, data._1, data._2, data._3))
    }

}

sealed trait LowPrioQueryResponseMaker { _: QueryResponseMaker.type =>
  /**
   * {{{
   * import acolyte.reactivemongo.QueryResponseMaker
   *
   * case class MyCaseClass(name: String)
   *
   * import reactivemongo.api.bson.BSONDocumentWriter
   *
   * implicit def writer: BSONDocumentWriter[MyCaseClass] = ???
   *
   * val maker = implicitly[QueryResponseMaker[Seq[MyCaseClass]]]
   * }}}
   */
  implicit def writableTraversableQueryResponseMaker[T[X] <: Traversable[X], U](implicit w: BSONDocumentWriter[U]) = new QueryResponseMaker[T[U]] {
    @annotation.tailrec
    def documents(
      in: Seq[U], out: Seq[BSONDocument]): Try[Seq[BSONDocument]] =
      in.headOption match {
        case Some(value) => w.writeTry(value) match {
          case Success(doc) =>
            documents(in.tail, doc +: out)

          case Failure(cause) =>
            Failure(cause)
        }

        case _ =>
          Success(out.reverse)
      }

    def apply(chanId: ChannelId, result: T[U]) =
      documents(result.toSeq, Seq.empty) match {
        case Success(docs) =>
          traversableQueryResponseMaker(chanId, docs)

        case Failure(cause) =>
          Some(Failure(cause))
      }
  }
}
