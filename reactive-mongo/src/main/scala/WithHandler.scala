package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }

import reactivemongo.api.{ MongoConnection, MongoDriver }

/** Functions to work with handler (provided driver functions). */
trait WithHandler { up: WithDriver ⇒

  /**
   * Works with a MongoDB driver handling only queries,
   * using given query `handler`.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @param handler Query handler
   *
   * {{{
   * import reactivemongo.api.MongoConnection
   * import acolyte.reactivemongo.{ AcolyteDSL, Request }
   *
   * AcolyteDSL.withQueryHandler({ req: Request ⇒ aResponse }) { d =>
   *   val con: MongoConnection = d
   *   "aResult"
   * }
   * }}}
   *
   * @see [[AcolyteDSL.withDriver]]
   * @see [[AcolyteDSL.handleQuery]]
   * @see [[AcolyteDSL.withQueryResult]]
   */
  def withQueryHandler[T](handler: Request ⇒ PreparedResponse)(f: MongoConnection ⇒ T)(implicit d: MongoDriver, m: ConnectionManager[ConnectionHandler], c: ExecutionContext): Future[T] =
    withConnection(AcolyteDSL handleQuery QueryHandler(handler))(f)

  /**
   * Works with a MongoDB driver handling only write operations,
   * using given write `handler`.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @param handler Writer handler
   *
   * {{{
   * import reactivemongo.api.MongoConnection
   * import acolyte.reactivemongo.{ AcolyteDSL, Request, WriteOp }
   *
   * AcolyteDSL.withWriteHandler({ cmd: (WriteOp, Request) ⇒ aResp }) { d =>
   *   val con: MongoConnection = d
   *   "aResult"
   * }
   * }}}
   *
   * @see [[AcolyteDSL.withDriver]]
   * @see [[AcolyteDSL.handleWrite]]
   * @see [[AcolyteDSL.withWriteResult]]
   */
  def withWriteHandler[T](handler: (WriteOp, Request) ⇒ PreparedResponse)(f: MongoConnection ⇒ T)(implicit d: MongoDriver, m: ConnectionManager[ConnectionHandler], c: ExecutionContext): Future[T] =
    withConnection(AcolyteDSL handleWrite handler)(f)

}
