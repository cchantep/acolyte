package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }
import reactivemongo.api.{ DefaultDB, MongoConnection, MongoDriver }

/**
 * Functions to work with MongoDB (provided driver functions).
 *
 * @define conParam the connection manager parameter (see [[ConnectionManager]])
 * @define con a previously initialized connection
 * @define f the function applied to initialized Mongo DB
 */
trait WithDB { withDriver: WithDriver ⇒
  /**
   * Works with Mongo database (named "acolyte") resolved using given driver
   * initialized using Acolyte for ReactiveMongo
   * (should not be used with other driver instances).
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @param conParam $conParam
   * @param f $f
   *
   * {{{
   * import reactivemongo.api.DB
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * // handler: ConnectionHandler
   * val s: Future[String] = AcolyteDSL withDB(handler) { db =>
   *   val d: DB = db
   *   "Result"
   * }
   * }}}
   * @see AcolyteDSL.withConnection
   */
  def withDB[A, B](conParam: ⇒ A)(f: DefaultDB ⇒ B)(
    implicit
    d: MongoDriver,
    m: ConnectionManager[A],
    ec: ExecutionContext,
    compose: ComposeWithCompletion[B]): compose.Outer =
    compose(Future(m.open(d, conParam)).
      flatMap(_.database("acolyte")), f) { db ⇒
      m.releaseIfNecessary(db.connection); ()
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
   * import reactivemongo.api.DB
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * // handler: ConnectionHandler
   * val s: Future[String] = AcolyteDSL withConnection(handler) { con =>
   *   AcolyteDSL withDB(con) { db =>
   *     val d: DefaultDB = db
   *     "Result"
   *   }
   * }
   * }}}
   * @see AcolyteDSL.withConnection
   */
  def withDB[T](con: ⇒ MongoConnection)(f: DefaultDB ⇒ T)(
    implicit
    ec: ExecutionContext,
    compose: ComposeWithCompletion[T]): compose.Outer =
    compose(con.database("acolyte"), f)(_ ⇒ {})

}
