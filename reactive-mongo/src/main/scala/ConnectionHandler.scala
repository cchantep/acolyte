package acolyte.reactivemongo

import scala.util.Try

import reactivemongo.io.netty.channel.ChannelId
import reactivemongo.core.protocol.Response

import reactivemongo.acolyte.Response

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
  final def withQueryHandler[T](handler: T)(implicit f: T ⇒ QueryHandler) =
    ConnectionHandler(new QueryHandler {
      def apply(chanId: ChannelId, q: Request) =
        self.queryHandler(chanId, q).orElse(f(handler)(chanId, q))
    }, writeHandler)

  /**
   * Creates a copy of this connection handler,
   * with given write `handler` appended.
   *
   * @param handler Write handler
   */
  final def withWriteHandler[T](handler: T)(implicit f: T ⇒ WriteHandler) =
    ConnectionHandler(queryHandler, new WriteHandler {
      def apply(chanId: ChannelId, op: WriteOp, w: Request) =
        self.writeHandler(chanId, op, w).orElse(f(handler)(chanId, op, w))
    })

  /**
   * Returns a new connection handler that first try this one,
   * or else if it doesn't match, use the other one.
   *
   * {{{
   * import acolyte.reactivemongo.ConnectionHandler
   *
   * def connectionHandler3(h1: ConnectionHandler, h2: ConnectionHandler) =
   *   h1 orElse h2
   * }}}
   */
  final def orElse(other: ConnectionHandler): ConnectionHandler =
    new ConnectionHandler {
      val queryHandler = self.queryHandler orElse other.queryHandler
      val writeHandler = self.writeHandler orElse other.writeHandler
    }
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
   * import acolyte.reactivemongo.{
   *   ConnectionHandler, QueryHandler, WriteHandler
   * }
   *
   * def foo(myQueryHandler: QueryHandler, myWriteHandler: WriteHandler) =
   *   ConnectionHandler(myQueryHandler, myWriteHandler)
   * }}}
   */
  def apply[A, B](queryHandler: A = QueryHandler.empty, writeHandler: B = WriteHandler.empty)(implicit f: A ⇒ QueryHandler, g: B ⇒ WriteHandler): ConnectionHandler = {
    val q = queryHandler
    val w = writeHandler

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
sealed trait QueryHandler extends ((ChannelId, Request) ⇒ Option[Try[Response]]) {
  self ⇒

  /**
   * @param chanId ID of channel
   * @param query Query to respond to
   */
  override def apply(chanId: ChannelId, query: Request): Option[Try[Response]]

  /**
   * Returns a new query handler that first try this one,
   * or else if it doesn't match, use the other one.
   *
   * {{{
   * import acolyte.reactivemongo.QueryHandler
   *
   * def queryHandler3(h1: QueryHandler, h2: QueryHandler) = h1 orElse h2
   * }}}
   */
  final def orElse(other: QueryHandler): QueryHandler = new QueryHandler {
    def apply(chanId: ChannelId, query: Request): Option[Try[Response]] =
      self(chanId, query).orElse(other(chanId, query))
  }
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
   * import reactivemongo.api.bson.BSONDocument
   * import acolyte.reactivemongo.{ Request, QueryHandler, QueryResponse }
   *
   * val handler1: QueryHandler = // Returns a successful empty response
   *   (q: Request) => QueryResponse(Seq.empty[BSONDocument])
   *
   * }}}
   */
  implicit def apply(f: Request ⇒ PreparedResponse): QueryHandler =
    new QueryHandler {
      def apply(chanId: ChannelId, q: Request): Option[Try[Response]] = f(q)(chanId)
    }

  /**
   * Empty query handler, not handling any request.
   */
  lazy val empty = apply(_ ⇒ QueryResponse(None))
}

/** Write handler. */
sealed trait WriteHandler
  extends ((ChannelId, WriteOp, Request) ⇒ Option[Try[Response]]) { self ⇒

  /**
   * @param chanId ID of channel
   * @param op Write operator
   * @param req Write request
   */
  override def apply(chanId: ChannelId, op: WriteOp, req: Request): Option[Try[Response]]

  /**
   * Returns a new write handler that first try this one,
   * or else if it doesn't match, use the other one.
   *
   * {{{
   * import acolyte.reactivemongo.WriteHandler
   *
   * def writeHandler3(h1: WriteHandler, h2: WriteHandler) = h1 orElse h2
   * }}}
   */
  final def orElse(other: WriteHandler): WriteHandler = new WriteHandler {
    def apply(chanId: ChannelId, op: WriteOp, req: Request): Option[Try[Response]] = self(chanId, op, req).orElse(other(chanId, op, req))
  }
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
   * import acolyte.reactivemongo.{
   *   Request, WriteHandler, WriteOp, WriteResponse
   * }
   *
   * val handler: WriteHandler = WriteHandler {
   *   // Returns a successful for 1 doc
   *   (_: WriteOp, _: Request) => WriteResponse(1, false)
   * }
   * }}}
   */
  implicit def apply(f: (WriteOp, Request) ⇒ PreparedResponse): WriteHandler = new WriteHandler {
    def apply(chanId: ChannelId, op: WriteOp, w: Request): Option[Try[Response]] = f(op, w)(chanId)
  }

  /**
   * Empty query handler, not handling any request.
   */
  lazy val empty = apply((_, _) ⇒ WriteResponse(None))
}
