package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }

import reactivemongo.api.{ MongoConnection, MongoDriver }

/** Functions to work with result (provided collection functions). */
trait WithResult { withHandler: WithHandler ⇒

  /**
   * Works with a Mongo driver handling only queries,
   * and returning given `result` for all of them.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @see [[AcolyteDSL.withDriver]]
   * @see [[AcolyteDSL.handleQuery]]
   * @see [[AcolyteDSL.withQueryHandler]]
   */
  def withQueryResult[A, B](result: ⇒ A)(f: MongoDriver ⇒ B)(implicit m: DriverManager[ConnectionHandler], mk: QueryResponseMaker[A], c: ExecutionContext): Future[B] = withQueryHandler({ _: Request ⇒ QueryResponse(result) })(f)

  /**
   * Works with a Mongo driver handling only queries,
   * and returning given `result` for all of them.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @see [[AcolyteDSL.withFlatDriver]]
   * @see [[AcolyteDSL.handleQuery]]
   */
  def withFlatQueryResult[A, B](result: ⇒ A)(f: MongoDriver ⇒ Future[B])(implicit m: DriverManager[ConnectionHandler], mk: QueryResponseMaker[A], c: ExecutionContext): Future[B] =
    withFlatQueryHandler({ _: Request ⇒ QueryResponse(result) })(f)

  /**
   * Works with a Mongo driver handling only write operations,
   * and returning given `result` for all of them.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @see [[AcolyteDSL.withDriver]]
   * @see [[AcolyteDSL.handleWrite]]
   * @see [[AcolyteDSL.withWriteHandler]]
   */
  def withWriteResult[A, B](result: ⇒ A)(f: MongoDriver ⇒ B)(implicit m: DriverManager[ConnectionHandler], mk: WriteResponseMaker[A], c: ExecutionContext): Future[B] = withWriteHandler(
    { (_: WriteOp, _: Request) ⇒ WriteResponse(result) })(f)

  /**
   * Works with a Mongo driver handling only write operations,
   * and returning given `result` for all of them.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @see [[AcolyteDSL.withFlatDriver]]
   * @see [[AcolyteDSL.handleWrite]]
   */
  def withFlatWriteResult[A, B](result: ⇒ A)(f: MongoDriver ⇒ Future[B])(implicit m: DriverManager[ConnectionHandler], mk: WriteResponseMaker[A], c: ExecutionContext): Future[B] = withFlatWriteHandler(
    { (_: WriteOp, _: Request) ⇒ WriteResponse(result) })(f)
}
