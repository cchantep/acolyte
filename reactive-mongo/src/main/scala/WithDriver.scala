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
  def withDriver[T](f: MongoDriver ⇒ T)(implicit m: DriverManager, c: ExecutionContext, sf: ScopeFactory[MongoDriver, T]): sf.OuterResult = sf(() ⇒ asyncDriver, m.releaseIfNecessary(_))(f)

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
   * @see [[withDriver]]
   */
  def withConnection[A, B](conParam: ⇒ A)(f: MongoConnection ⇒ B)(implicit d: MongoDriver, m: ConnectionManager[A], c: ExecutionContext, sf: ScopeFactory[MongoConnection, B]): sf.OuterResult = sf(() ⇒ asyncConnection(d, conParam), m.releaseIfNecessary(_))(f)

  // ---

  // TODO: Pass the driver ClassLoader
  private def asyncDriver(implicit m: DriverManager): Future[MongoDriver] =
    try Future.successful(m.open()) catch {
      case cause: Throwable ⇒
        Future.failed[MongoDriver](cause)
    }

  protected def asyncConnection[A](d: MongoDriver, conParam: ⇒ A)(implicit m: ConnectionManager[A]): Future[MongoConnection] = try {
    Future.successful(m.open(d, conParam))
  } catch {
    case cause: Throwable ⇒
      Future.failed[MongoConnection](cause)
  }
}
