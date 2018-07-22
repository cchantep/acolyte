package acolyte.reactivemongo

import scala.concurrent.ExecutionContext

import reactivemongo.api.{ MongoConnection, MongoDriver }

/** Functions to work with result (provided collection functions). */
trait WithResult { withHandler: WithHandler ⇒

  /**
   * Works with a MongoDB driver handling only queries,
   * and returning given `result` for all of them.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @param result Query result
   *
   * @see [[AcolyteDSL.withFlatDriver]]
   * @see [[AcolyteDSL.handleQuery]]
   */
  def withQueryResult[A, B](result: ⇒ A)(f: MongoConnection ⇒ B)(
    implicit
    d: MongoDriver,
    m: ConnectionManager[ConnectionHandler],
    mk: QueryResponseMaker[A],
    ec: ExecutionContext,
    compose: ComposeWithCompletion[B]): compose.Outer =
    withQueryHandler[B]({ _: Request ⇒ QueryResponse(result) })(f)

  /**
   * Works with a MongoDB driver handling only write operations,
   * and returning given `result` for all of them.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @see [[AcolyteDSL.withDriver]]
   * @see [[AcolyteDSL.handleWrite]]
   * @see [[AcolyteDSL.withWriteHandler]]
   */
  def withWriteResult[A, B](result: ⇒ A)(f: MongoConnection ⇒ B)(
    implicit
    d: MongoDriver,
    m: ConnectionManager[ConnectionHandler],
    mk: WriteResponseMaker[A],
    ec: ExecutionContext,
    compose: ComposeWithCompletion[B]): compose.Outer =
    withWriteHandler[B]({ (_: WriteOp, _: Request) ⇒ WriteResponse(result) })(f)
}
