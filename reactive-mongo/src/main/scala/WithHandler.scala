package acolyte.reactivemongo

import scala.concurrent.ExecutionContext

import reactivemongo.api.{ AsyncDriver, MongoConnection }

/** Functions to work with handler (provided driver functions). */
trait WithHandler { up: WithDriver =>

  /**
   * Works with a MongoDB driver handling only queries,
   * using given query `handler`.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @param handler Query handler
   *
   * {{{
   * import scala.concurrent.ExecutionContext
   *
   * import reactivemongo.api.{ AsyncDriver, MongoConnection }
   * import acolyte.reactivemongo.{ AcolyteDSL, PreparedResponse, Request }
   *
   * def aResponse: PreparedResponse = ???
   *
   * def foo(implicit ec: ExecutionContext, d: AsyncDriver) =
   *   AcolyteDSL.withQueryHandler({ (_: Request) => aResponse }) {
   *     (_: MongoConnection) => "aResult"
   *   }
   * }}}
   *
   * @see [[AcolyteDSL.withDriver]]
   * @see [[AcolyteDSL.handleQuery]]
   * @see [[AcolyteDSL.withQueryResult]]
   */
  def withQueryHandler[T](
      handler: Request => PreparedResponse
    )(f: MongoConnection => T
    )(implicit
      d: AsyncDriver,
      m: ConnectionManager[ConnectionHandler],
      ec: ExecutionContext,
      compose: ComposeWithCompletion[T]
    ): compose.Outer =
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
   * import scala.concurrent.ExecutionContext
   *
   * import reactivemongo.api.AsyncDriver
   * import acolyte.reactivemongo.{
   *   AcolyteDSL, PreparedResponse, Request, WriteOp
   * }
   *
   * def aResp: PreparedResponse = ???
   *
   * def foo(implicit ec: ExecutionContext, d: AsyncDriver) =
   *   AcolyteDSL.withWriteHandler({ (_: WriteOp, _: Request) => aResp }) { _ =>
   *     "aResult"
   *   }
   * }}}
   *
   * @see [[AcolyteDSL.withDriver]]
   * @see [[AcolyteDSL.handleWrite]]
   * @see [[AcolyteDSL.withWriteResult]]
   */
  def withWriteHandler[T](
      handler: (WriteOp, Request) => PreparedResponse
    )(f: MongoConnection => T
    )(implicit
      d: AsyncDriver,
      m: ConnectionManager[ConnectionHandler],
      ec: ExecutionContext,
      compose: ComposeWithCompletion[T]
    ): compose.Outer =
    withConnection(AcolyteDSL handleWrite WriteHandler(handler))(f)
}
