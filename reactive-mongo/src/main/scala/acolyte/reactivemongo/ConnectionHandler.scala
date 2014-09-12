package acolyte.reactivemongo

import scala.util.Try

import reactivemongo.core.protocol.Response

/** Connection handler. */
sealed trait ConnectionHandler { self ⇒

  /** Query handler */
  def queryHandler: QueryHandler

  /**
   * Creates a copy of this connection handler,
   * with given query `handler` appended.
   *
   * @param handler Query handler
   */
  def withQueryHandler[T](handler: T)(implicit f: T ⇒ QueryHandler) = {
    new ConnectionHandler {
      override val queryHandler = new QueryHandler {
        override def apply(cid: Int, q: Request) =
          self.queryHandler(cid, q).orElse(f(handler)(cid, q))
      }
    }
  }
}

/** Companion object for connection handler. */
object ConnectionHandler {

  /**
   * @param handler Query handler
   *
   * {{{
   * import acolyte.reactivemongo.ConnectionHandler
   *
   * ConnectionHandler(myQueryHandler)
   * }}}
   */
  def apply[A](handler: A)(implicit f: A ⇒ QueryHandler): ConnectionHandler =
    new ConnectionHandler {
      override val queryHandler = f(handler)
    }

  /**
   * Empty connection handler, not handling any query or write request.
   */
  lazy val empty: ConnectionHandler = apply(QueryHandler.empty)
}

/** Query handler. */
sealed trait QueryHandler extends ((Int, Request) ⇒ Option[Try[Response]]) {
  /**
   * @param channelId ID of channel
   * @param query Query to respond to
   */
  override def apply(channelId: Int, query: Request): Option[Try[Response]]
}

/** Query handler companion. */
object QueryHandler {
  import scala.language.implicitConversions

  /**
   * Creates a query handler from given function `f`.
   *
   * @param f Handling function, with arguments channel ID and query.
   *
   * {{{
   * import reactivemongo.bson.BSONDocument
   * import acolyte.reactivemongo.{ Request, QueryHandler }
   *
   * val handler1: QueryHandler = // Returns a successful empty response
   *   (q: Request) => Some(Seq.empty[BSONDocument])
   *
   * }}}
   */
  implicit def SimpleQueryHandler(f: Request ⇒ QueryResponse): QueryHandler =
    new QueryHandler {
      def apply(chanId: Int, q: Request): Option[Try[Response]] = f(q)(chanId)
    }

  /**
   * Empty query handler, not handling any request.
   */
  lazy val empty = SimpleQueryHandler(_ ⇒ QueryResponse(None))
}

/** Write handler. */
sealed trait WriteHandler
    extends ((Int, WriteOp, Request) ⇒ Option[Try[Response]]) {

  /**
   * @param channelId ID of channel
   * @param op Write operator
   * @param req Write request
   */
  override def apply(channelId: Int, op: WriteOp, req: Request): Option[Try[Response]]

}
