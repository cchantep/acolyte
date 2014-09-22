package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }

import reactivemongo.api.{ MongoConnection, MongoDriver }

/** Functions to work with result (provided collection functions). */
trait WithResult { up: WithDriver ⇒

  /**
   * Works with a Mongo driver handling only queries,
   * and returning given `result` for all of them.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * {{{
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * }}}
   * @see [[AcolyteDSL.withDriver]]
   * @see [[AcolyteDSL.handleQuery]]
   */
  def withQueryResult[A, B](result: ⇒ A)(f: MongoDriver ⇒ B)(implicit m: DriverManager[ConnectionHandler], mk: QueryResponseMaker[A], c: ExecutionContext): Future[B] = withDriver(
    AcolyteDSL handleQuery { _: Request ⇒ QueryResponse(result) })(f)

  /**
   * Works with a Mongo driver handling only queries,
   * and returning given `result` for all of them.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * {{{
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * }}}
   * @see [[AcolyteDSL.withFlatDriver]]
   * @see [[AcolyteDSL.handleQuery]]
   */
  def withFlatQueryResult[A, B](result: ⇒ A)(f: MongoDriver ⇒ Future[B])(implicit m: DriverManager[ConnectionHandler], mk: QueryResponseMaker[A], c: ExecutionContext): Future[B] = withFlatDriver(
    AcolyteDSL handleQuery { _: Request ⇒ QueryResponse(result) })(f)

  /**
   * Works with a Mongo driver handling only write operations,
   * and returning given `result` for all of them.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * {{{
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * }}}
   * @see [[AcolyteDSL.withDriver]]
   * @see [[AcolyteDSL.handleWrite]]
   */
  def withWriteResult[A, B](result: ⇒ A)(f: MongoDriver ⇒ B)(implicit m: DriverManager[ConnectionHandler], mk: WriteResponseMaker[A], c: ExecutionContext): Future[B] = withDriver(AcolyteDSL handleWrite { (_: WriteOp, _: Request) ⇒
    WriteResponse(result)
  })(f)

  /**
   * Works with a Mongo driver handling only write operations,
   * and returning given `result` for all of them.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * {{{
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * }}}
   * @see [[AcolyteDSL.withFlatDriver]]
   * @see [[AcolyteDSL.handleWrite]]
   */
  def withFlatWriteResult[A, B](result: ⇒ A)(f: MongoDriver ⇒ Future[B])(implicit m: DriverManager[ConnectionHandler], mk: WriteResponseMaker[A], c: ExecutionContext): Future[B] = withFlatDriver(
    AcolyteDSL handleWrite {
      (_: WriteOp, _: Request) ⇒ WriteResponse(result)
    })(f)
}
