package acolyte.reactivemongo

import scala.util.control.NonFatal

import scala.concurrent.{ ExecutionContext, Future }

import reactivemongo.api.{ AsyncDriver, MongoConnection }

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
  def driver(implicit m: DriverManager): AsyncDriver = m.open()

  // TODO: Pass the driver ClassLoader
  private def asyncDriver(implicit m: DriverManager): Future[AsyncDriver] =
    try Future.successful(m.open())
    catch {
      case NonFatal(cause) =>
        Future.failed[AsyncDriver](cause)
    }

  /**
   * Works with MongoDB driver configured with Acolyte handlers.
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @param f $f
   *
   * {{{
   * import scala.concurrent.{ ExecutionContext, Future }
   * import reactivemongo.api.AsyncDriver
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * // handler: ConnectionHandler
   * def s(implicit ec: ExecutionContext): Future[String] =
   *   AcolyteDSL.withDriver { (_: AsyncDriver) =>
   *     "Result"
   *   }
   * }}}
   */
  def withDriver[T](
      f: AsyncDriver => T
    )(implicit
      m: DriverManager,
      ec: ExecutionContext,
      compose: ComposeWithCompletion[T]
    ): compose.Outer = {
    compose(asyncDriver, f) { driver => m.releaseIfNecessary(driver); () }
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
   * import scala.concurrent.{ ExecutionContext, Future }
   *
   * import reactivemongo.api.{ MongoConnection, AsyncDriver }
   * import acolyte.reactivemongo.{ AcolyteDSL, ConnectionHandler }
   *
   * // handler: ConnectionHandler
   * def s(handler: ConnectionHandler)(
   *   implicit ec: ExecutionContext, d: AsyncDriver): Future[String] =
   *   AcolyteDSL.withConnection(handler) { (_: MongoConnection) =>
   *     "Result"
   *   }
   * }}}
   */
  def withConnection[A, B](
      conParam: => A
    )(f: MongoConnection => B
    )(implicit
      d: AsyncDriver,
      m: ConnectionManager[A],
      ec: ExecutionContext,
      compose: ComposeWithCompletion[B]
    ): compose.Outer =
    compose(Future(m.open(d, conParam)), f) { con =>
      m.releaseIfNecessary(con); ()
    }
}
