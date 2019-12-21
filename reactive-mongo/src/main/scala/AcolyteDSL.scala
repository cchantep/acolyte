package acolyte.reactivemongo

/** Acolyte DSL for ReactiveMongo. */
object AcolyteDSL extends WithDriver
  with WithDB with WithCollection with WithHandler with WithResult {

  /**
   * Creates an empty connection handler.
   *
   * {{{
   * import reactivemongo.api.AsyncDriver
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * def foo(implicit d: AsyncDriver) = AcolyteDSL.handle
   * }}}
   * @see [[withDriver]]
   */
  def handle: ConnectionHandler = ConnectionHandler.empty

  /**
   * Creates a connection handler with given query handler,
   * but no write handler.
   *
   * {{{
   * import scala.concurrent.ExecutionContext
   *
   * import reactivemongo.api.AsyncDriver
   * import acolyte.reactivemongo.AcolyteDSL.{
   *   withConnection, withDriver, handleQuery
   * }
   * import acolyte.reactivemongo.{ PreparedResponse, Request }
   *
   * def aResponse: PreparedResponse = ???
   *
   * def foo(implicit ec: ExecutionContext) =
   *   withDriver { implicit d =>
   *     withConnection(handleQuery { req: Request => aResponse }) { con =>
   *       // work with connection (e.g. call you function using Mongo)
   *       "Value"
   *     }
   *   }
   * }}}
   *
   * @see [[ConnectionHandler.withWriteHandler]]
   */
  def handleQuery[T](handler: T)(implicit f: T ⇒ QueryHandler): ConnectionHandler = ConnectionHandler(handler)

  /**
   * Creates a connection handler with given write handler,
   * but no query handler.
   *
   * {{{
   * import scala.concurrent.ExecutionContext
   * import reactivemongo.api.AsyncDriver
   *
   * import acolyte.reactivemongo.AcolyteDSL.{
   *   withConnection, withDriver, handleWrite
   * }
   * import acolyte.reactivemongo.{ Request, WriteOp, PreparedResponse }
   *
   * def aResponse: PreparedResponse = ???
   *
   * def foo(implicit ec: ExecutionContext) =
   *   withDriver { implicit d: AsyncDriver =>
   *     withConnection(handleWrite {
   *       (op: WriteOp, req: Request) => aResponse }) { con =>
   *       // work with connection (e.g. call you function using Mongo)
   *       "Value"
   *     }
   *   }
   * }}}
   *
   * @see [[ConnectionHandler.withQueryHandler]]
   */
  def handleWrite[T](handler: T)(implicit f: T ⇒ WriteHandler): ConnectionHandler = ConnectionHandler(writeHandler = handler)

}
