package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }

import reactivemongo.api.{ MongoConnection, MongoDriver }

/** Functions to work with handler (provided driver functions). */
trait WithHandler { up: WithDriver ⇒

  /**
   * Works with a Mongo driver handling only queries,
   * using given query `handler`.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * {{{
   * import reactivemongo.api.MongoDriver
   * import acolyte.reactivemongo.{ AcolyteDSL, Request }
   *
   * AcolyteDSL.withQueryHandler({ req: Request ⇒ aResponse }) { d =>
   *   val driver: MongoDriver = d
   *   "aResult"
   * }
   * }}}
   *
   * @see [[AcolyteDSL.withDriver]]
   * @see [[AcolyteDSL.handleQuery]]
   * @see [[AcolyteDSL.withQueryResult]]
   */
  def withQueryHandler[T](handler: Request ⇒ PreparedResponse)(f: MongoDriver ⇒ T)(implicit m: DriverManager[ConnectionHandler], c: ExecutionContext): Future[T] = withDriver(AcolyteDSL handleQuery QueryHandler(handler))(f)

  /**
   * Works with a Mongo driver handling only queries,
   * using given query `handler`.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * {{{
   * import reactivemongo.api.MongoDriver
   * import acolyte.reactivemongo.{ AcolyteDSL, Request }
   *
   * AcolyteDSL.withFlatQueryHandler({ req: Request ⇒ aResponse }) { d =>
   *   val driver: MongoDriver = d
   *   Future(1+2)
   * }
   * }}}
   *
   * @see [[AcolyteDSL.withFlatDriver]]
   * @see [[AcolyteDSL.handleQuery]]
   * @see [[AcolyteDSL.withQueryResult]]
   */
  def withFlatQueryHandler[T](handler: Request ⇒ PreparedResponse)(f: MongoDriver ⇒ Future[T])(implicit m: DriverManager[ConnectionHandler], c: ExecutionContext): Future[T] = withFlatDriver(AcolyteDSL handleQuery QueryHandler(handler))(f)

  /**
   * Works with a Mongo driver handling only write operations,
   * using given write `handler`.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * {{{
   * import reactivemongo.api.MongoDriver
   * import acolyte.reactivemongo.{ AcolyteDSL, Request, WriteOp }
   *
   * AcolyteDSL.withWriteHandler({ cmd: (WriteOp, Request) ⇒ aResp }) { d =>
   *   val driver: MongoDriver = d
   *   "aResult"
   * }
   * }}}
   *
   * @see [[AcolyteDSL.withDriver]]
   * @see [[AcolyteDSL.handleWrite]]
   * @see [[AcolyteDSL.withWriteResult]
   */
  def withWriteHandler[T](handler: (WriteOp, Request) ⇒ PreparedResponse)(f: MongoDriver ⇒ T)(implicit m: DriverManager[ConnectionHandler], c: ExecutionContext): Future[T] = withDriver(AcolyteDSL handleWrite handler)(f)

  /**
   * Works with a Mongo driver handling only write operations,
   * using given write `handler`.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * {{{
   * import reactivemongo.api.MongoDriver
   * import acolyte.reactivemongo.{ AcolyteDSL, Request, WriteOp }
   *
   * AcolyteDSL.withWriteHandler({ cmd: (WriteOp, Request) ⇒ aResp }) { d =>
   *   val driver: MongoDriver = d
   *   Future(1+2)
   * }
   * }}}
   *
   * @see [[AcolyteDSL.withFlatDriver]]
   * @see [[AcolyteDSL.handleWrite]]
   * @see [[AcolyteDSL.withFlatWriteResult]
   */
  def withFlatWriteHandler[T](handler: (WriteOp, Request) ⇒ PreparedResponse)(f: MongoDriver ⇒ Future[T])(implicit m: DriverManager[ConnectionHandler], c: ExecutionContext): Future[T] = withFlatDriver(AcolyteDSL handleWrite handler)(f)

}
