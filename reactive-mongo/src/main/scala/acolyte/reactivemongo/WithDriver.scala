package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }

import reactivemongo.api.{ MongoConnection, MongoDriver }

/** Functions to work with driver. */
trait WithDriver {
  /**
   * Returns unmanaged driver.
   * You will have to close it by yourself.
   */
  def driver[T](implicit m: DriverManager): MongoDriver = m.open

  /**
   * Works with Mongo driver configured with Acolyte handlers.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @param f Function applied to initialized driver
   *
   * {{{
   * // handler: ConnectionHandler
   * val s: Future[String] = withDriver { d =>
   *   val initedDriver: MongoDriver = d
   *   "Result"
   * }
   * }}}
   * @see [[withFlatDriver]]
   */
  def withDriver[T](f: MongoDriver ⇒ T)(implicit m: DriverManager, c: ExecutionContext): Future[T] = for {
    driver ← Future(m.open)
    result ← {
      val res = Future(f(driver))
      res.onComplete(_ ⇒ m.releaseIfNecessary(driver))
      res
    }
  } yield result

  /**
   * Works with Mongo driver configured with Acolyte handlers.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @param f Function applied to initialized driver (returning a future)
   *
   * {{{
   * // handler: ConnectionHandler
   * val s: Future[String] = withFlatDriver { d =>
   *   val initedDriver: MongoDriver = d
   *   Future.successful("Result")
   * }
   * }}}
   * @see [[withDriver]]
   */
  def withFlatDriver[T](f: MongoDriver ⇒ Future[T])(implicit m: DriverManager, c: ExecutionContext): Future[T] = for {
    driver ← Future(m.open)
    result ← {
      val res = f(driver)
      res.onComplete(_ ⇒ m.releaseIfNecessary(driver))
      res
    }
  } yield result

  /**
   * Works with connection with options appropriate for a driver
   * initialized using Acolyte for ReactiveMongo
   * (should not be used with other driver instances).
   * Connection is closed after the result `Future` is completed.
   *
   * @param conParam Connection manager parameter (see [[ConnectionManager]])
   * @param f Function applied to initialized connection
   *
   * {{{
   * import reactivemongo.api.MongoConnection
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * // handler: ConnectionHandler
   * val s: Future[String] = AcolyteDSL.withConnection(handler) { con =>
   *   val c: MongoConnection = con
   *   "Result"
   * }
   * }}}
   * @see [[withFlatDriver]]
   * @see [[withFlatConnection]]
   */
  def withConnection[A, B](conParam: ⇒ A)(f: MongoConnection ⇒ B)(implicit d: MongoDriver, m: ConnectionManager[A], c: ExecutionContext): Future[B] =
    for {
      con ← Future(m.open(d, conParam))
      res ← {
        val result = Future(f(con))
        result.onComplete(_ ⇒ con.close())
        result
      }
    } yield res

  /**
   * Works with connection with options appropriate for a driver
   * initialized using Acolyte for ReactiveMongo
   * (should not be used with other driver instances).
   *
   * @param conParam Connection manager parameter (see [[ConnectionManager]])
   * @param f Function applied to initialized connection
   *
   * {{{
   * import reactivemongo.api.MongoConnection
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * // handler: ConnectionHandler
   * val s: Future[String] = AcolyteDSL.withFlatConnection(handler) { con =>
   *   val c: MongoConnection = con
   *   Future.successful("Result")
   * }
   * }}}
   * @see [[withFlatDriver]]
   * @see [[withConnection]]
   */
  def withFlatConnection[A, B](conParam: ⇒ A)(f: MongoConnection ⇒ Future[B])(implicit d: MongoDriver, m: ConnectionManager[A], c: ExecutionContext): Future[B] = for {
    con ← Future(m.open(d, conParam))
    res ← {
      val result = f(con)
      result.onComplete(_ ⇒ con.close())
      result
    }
  } yield res
}
