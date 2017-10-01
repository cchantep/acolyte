package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }

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
   * @see [[AcolyteDSL.withDriver]]
   * @see [[AcolyteDSL.handleQuery]]
   * @see [[AcolyteDSL.withQueryHandler]]
   */
  def withQueryResult[A, B](result: ⇒ A)(f: MongoConnection ⇒ B)(implicit d: MongoDriver, m: ConnectionManager[ConnectionHandler], mk: QueryResponseMaker[A], c: ExecutionContext): Future[B] = withQueryHandler({ _: Request ⇒ QueryResponse(result) })(f)

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
  def withWriteResult[A, B](result: ⇒ A)(f: MongoConnection ⇒ B)(implicit d: MongoDriver, m: ConnectionManager[ConnectionHandler], mk: WriteResponseMaker[A], c: ExecutionContext): Future[B] = withWriteHandler(
    { (_: WriteOp, _: Request) ⇒ WriteResponse(result) }
  )(f)
}
