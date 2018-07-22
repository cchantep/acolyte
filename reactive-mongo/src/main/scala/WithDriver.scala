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
      case cause: Throwable ⇒
        Future.failed[MongoDriver](cause)
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
   */
  def withDriver[T](f: MongoDriver ⇒ T)(implicit m: DriverManager, ec: ExecutionContext, compose: ComposeWithCompletion[T]): compose.Outer = {
    compose(asyncDriver, f) { driver ⇒
      m.releaseIfNecessary(driver); ()
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
   */
  def withConnection[A, B](conParam: ⇒ A)(f: MongoConnection ⇒ B)(
    implicit
    d: MongoDriver,
    m: ConnectionManager[A],
    ec: ExecutionContext,
    compose: ComposeWithCompletion[B]): compose.Outer =
    compose(Future(m.open(d, conParam)), f) { con ⇒
      m.releaseIfNecessary(con); ()
    }
}
