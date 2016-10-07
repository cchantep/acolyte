package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }

import reactivemongo.api.{ MongoConnection, MongoDriver }

/**
 * Functions to work with driver.
 *
 * @define f the function applied to initialized driver
 * @define conParam the connection manager parameter (see [[ConnectionManager]])
 */
trait WithDriver {
  /**
   * Returns unmanaged driver.
   * You will have to close it by yourself.
   */
  def driver(implicit m: DriverManager): MongoDriver = m.open

  // TODO: Pass the driver ClassLoader
  private def asyncDriver(implicit m: DriverManager): Future[MongoDriver] =
    try Future.successful(m.open()) catch {
      case cause: Throwable ⇒ Future.failed[MongoDriver](cause)
    }

  /**
   * Works with MongoDB driver configured with Acolyte handlers.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @param f $f
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
  def withDriver[T](f: MongoDriver ⇒ T)(implicit m: DriverManager, c: ExecutionContext): Future[T] = asyncDriver.map { driver ⇒
    try f(driver) catch {
      case cause: Throwable ⇒ throw cause
    } finally m.releaseIfNecessary(driver)
  }

  /**
   * Works with MongoDB driver configured with Acolyte handlers.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @param f $f (returning a future)
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
  def withFlatDriver[T](f: MongoDriver ⇒ Future[T])(implicit m: DriverManager, c: ExecutionContext): Future[T] = asyncDriver.flatMap { driver ⇒
    f(driver).andThen {
      case _ ⇒ m.releaseIfNecessary(driver)
    }
  }

  /**
   * Works with connection with options appropriate for a driver
   * initialized using Acolyte for ReactiveMongo
   * (should not be used with other driver instances).
   * Connection is closed after the result `Future` is completed.
   *
   * @param conParam $conParam
   * @param f $f
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
      res ← Future(f(con)).andThen { case _ ⇒ m.releaseIfNecessary(con) }
    } yield res

  /**
   * Works with connection with options appropriate for a driver
   * initialized using Acolyte for ReactiveMongo
   * (should not be used with other driver instances).
   *
   * @param conParam $conParam
   * @param f $f
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
    res ← f(con).andThen { case _ ⇒ m.releaseIfNecessary(con) }
  } yield res
}
