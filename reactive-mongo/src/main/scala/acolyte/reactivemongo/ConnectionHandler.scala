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
        override def apply(cid: Int, q: Query) =
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
sealed trait QueryHandler extends ((Int, Query) ⇒ Option[Try[Response]]) {
  /**
   * @param channelId ID of channel
   * @param query Query to respond to
   */
  override def apply(channelId: Int, query: Query): Option[Try[Response]]
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
   * import acolyte.reactivemongo.{ Query, QueryHandler }
   *
   * val handler1: QueryHandler = // Returns a successful empty response
   *   (q: Query) => Some(Seq.empty[BSONDocument])
   *
   * }}}
   */
  implicit def SimpleQueryHandler(f: Query ⇒ QueryResponse): QueryHandler =
    new QueryHandler {
      def apply(chanId: Int, q: Query): Option[Try[Response]] = f(q)(chanId)
    }

  /**
   * Empty query handler, not handling anything.
   */
  lazy val empty = SimpleQueryHandler(_ ⇒ QueryResponse(None))
}
