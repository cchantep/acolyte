package acolyte.reactivemongo

import scala.util.Try

import reactivemongo.core.protocol.Response

/** Connection handler. */
sealed trait ConnectionHandler { self ⇒

  /** Query handler */
  def queryHandler: QueryHandler

  /** Write handler */
  def writeHandler: WriteHandler

  /**
   * Creates a copy of this connection handler,
   * with given query `handler` appended.
   *
   * @param handler Query handler
   */
  def withQueryHandler[T](handler: T)(implicit f: T ⇒ QueryHandler) =
    ConnectionHandler(new QueryHandler {
      def apply(cid: Int, q: Request) =
        self.queryHandler(cid, q).orElse(f(handler)(cid, q))
    }, writeHandler)

  /**
   * Creates a copy of this connection handler,
   * with given write `handler` appended.
   *
   * @param handler Write handler
   */
  def withWriteHandler[T](handler: T)(implicit f: T ⇒ WriteHandler) =
    ConnectionHandler(queryHandler, new WriteHandler {
      def apply(cid: Int, op: WriteOp, w: Request) =
        self.writeHandler(cid, op, w).orElse(f(handler)(cid, op, w))
    })
}

/** Companion object for connection handler. */
object ConnectionHandler {
  /**
   * Creates connection handler using given query and write handlers.
   *
   * @param q Query handler
   * @param w Write handler
   *
   * {{{
   * import acolyte.reactivemongo.ConnectionHandler
   *
   * ConnectionHandler(myQueryHandler, myWriteHandler)
   * }}}
   */
  def apply[A, B](queryHandler: A = QueryHandler.empty, writeHandler: B = WriteHandler.empty)(implicit f: A ⇒ QueryHandler, g: B ⇒ WriteHandler): ConnectionHandler = {
    val q = queryHandler; val w = writeHandler
    new ConnectionHandler {
      val queryHandler = f(q)
      val writeHandler = g(w)
    }
  }

  /**
   * Empty connection handler, not handling any query or write request.
   */
  lazy val empty: ConnectionHandler = apply()
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
   *   (q: Request) => QueryResponse(Seq.empty[BSONDocument])
   *
   * }}}
   */
  implicit def apply(f: Request ⇒ PreparedResponse): QueryHandler = new QueryHandler {
    def apply(chanId: Int, q: Request): Option[Try[Response]] = f(q)(chanId)
  }

  /**
   * Empty query handler, not handling any request.
   */
  lazy val empty = apply(_ ⇒ QueryResponse(None))
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

/** Write handler companion. */
object WriteHandler {
  import scala.language.implicitConversions

  /**
   * Creates a write handler from given function `f`.
   *
   * @param f Handling function, with arguments channel ID and write request.
   *
   * {{{
   * import acolyte.reactivemongo.{ Request, WriteHandler, WriteOp }
   *
   * val handler1: WriteHandler = // Returns a successful empty response
   *   (w: (WriteOp, Request)) => WriteResponse(false)
   *
   * val handler2 = WriteHandler { (op: WriteOp,
   * }}}
   */
  implicit def apply(f: (WriteOp, Request) ⇒ PreparedResponse): WriteHandler = new WriteHandler {
    def apply(chanId: Int, op: WriteOp, w: Request): Option[Try[Response]] = f(op, w)(chanId)
  }

  /**
   * Empty query handler, not handling any request.
   */
  lazy val empty = apply((_, _) ⇒ WriteResponse(None))
}
