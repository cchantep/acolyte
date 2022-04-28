package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }

import reactivemongo.api.{ DB, MongoConnection, AsyncDriver }
import reactivemongo.api.acolyte.AcolyteDB

/**
 * Functions to work with MongoDB (provided driver functions).
 *
 * @define conParam the connection manager parameter (see [[ConnectionManager]])
 * @define con a previously initialized connection
 * @define f the function applied to initialized Mongo DB
 * @define nameParam the name of database
 */
trait WithDB { withDriver: WithDriver ⇒
  /**
   * Works with a Mongo database (named "acolyte") resolved using given driver
   * initialized using Acolyte for ReactiveMongo
   * (should not be used with other driver instances).
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @param conParam $conParam
   * @param f $f
   *
   * {{{
   * import scala.concurrent.{ ExecutionContext, Future }
   *
   * import reactivemongo.api.{ DB, AsyncDriver }
   * import acolyte.reactivemongo.{ AcolyteDSL, ConnectionHandler }
   *
   * def s(handler: ConnectionHandler)(
   *   implicit ec: ExecutionContext, d: AsyncDriver): Future[String] =
   *   AcolyteDSL.withDB(handler) { db =>
   *     val d: DB = db
   *     "Result"
   *   }
   * }}}
   * @see AcolyteDSL.withConnection
   */
  def withDB[A, B](conParam: ⇒ A)(f: DB ⇒ B)(
    implicit
    d: AsyncDriver,
    m: ConnectionManager[A],
    ec: ExecutionContext,
    compose: ComposeWithCompletion[B]): compose.Outer =
    withDB[A, B](conParam, "acolyte")(f)

  /**
   * Works with a Mongo database resolved using given driver
   * initialized using Acolyte for ReactiveMongo
   * (should not be used with other driver instances).
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @param conParam $conParam
   * @param name $nameParam
   * @param f $f
   *
   * {{{
   * import scala.concurrent.{ ExecutionContext, Future }
   *
   * import reactivemongo.api.{ DB, AsyncDriver }
   * import acolyte.reactivemongo.{ AcolyteDSL, ConnectionHandler }
   *
   * def s(handler: ConnectionHandler)(
   *   implicit ec: ExecutionContext, d: AsyncDriver): Future[String] =
   *   AcolyteDSL.withDB(handler, "my_db") { db =>
   *     val d: DB = db
   *     "Result"
   *   }
   * }}}
   * @see AcolyteDSL.withConnection
   */
  def withDB[A, B](conParam: ⇒ A, name: String)(f: DB ⇒ B)(
    implicit
    d: AsyncDriver,
    m: ConnectionManager[A],
    ec: ExecutionContext,
    compose: ComposeWithCompletion[B]): compose.Outer = {
    def database = Future.fromTry[DB](scala.util.Try {
      val connection = m.open(d, conParam)

      AcolyteDB(connection, name)
    })

    compose(database, f) { db =>
      m.releaseIfNecessary(db.connection); ()
    }
  }

  /**
   * Works with Mongo database (named "acolyte") resolved using given driver
   * initialized using Acolyte for ReactiveMongo
   * (should not be used with other driver instances).
   *
   * @param con $con
   * @param f $f
   *
   * {{{
   * import scala.concurrent.{ ExecutionContext, Future }
   *
   * import reactivemongo.api.{ DB, AsyncDriver }
   * import acolyte.reactivemongo.{ AcolyteDSL, ConnectionHandler }
   *
   * def s(handler: ConnectionHandler)(
   *   implicit ec: ExecutionContext, d: AsyncDriver) =
   *   AcolyteDSL.withConnection(handler) { con =>
   *     AcolyteDSL.withDB(con) { db =>
   *       val d: DB = db
   *       "Result"
   *     }
   *   }
   * }}}
   * @see AcolyteDSL.withConnection
   */
  def withDB[T](con: ⇒ MongoConnection)(
    f: DB ⇒ T)(
    implicit
    ec: ExecutionContext,
    compose: ComposeWithCompletion[T]): compose.Outer =
    withDB[T](con, "acolyte")(f)

  /**
   * Works with a Mongo database resolved using given driver
   * initialized using Acolyte for ReactiveMongo
   * (should not be used with other driver instances).
   *
   * @param con $con
   * @param name $nameParam
   * @param f $f
   *
   * {{{
   * import scala.concurrent.{ ExecutionContext, Future }
   *
   * import reactivemongo.api.{ DB, AsyncDriver }
   * import acolyte.reactivemongo.{ AcolyteDSL, ConnectionHandler }
   *
   * // handler: ConnectionHandler
   * def s(handler: ConnectionHandler)(
   *   implicit ec: ExecutionContext, d: AsyncDriver): Future[String] =
   *   AcolyteDSL.withConnection(handler) { con =>
   *     AcolyteDSL.withDB(con, "my_db") { db =>
   *       val d: DB = db
   *       "Result"
   *     }
   *   }
   * }}}
   * @see AcolyteDSL.withConnection
   */
  def withDB[T](con: ⇒ MongoConnection, name: String)(
    f: DB ⇒ T)(
    implicit
    ec: ExecutionContext,
    compose: ComposeWithCompletion[T]): compose.Outer =
    compose(con.database(name), f)(_ ⇒ {})

}
