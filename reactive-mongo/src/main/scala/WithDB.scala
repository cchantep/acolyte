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
   * @see withFlatDB[A,B]
   */
  def withDB[A, B](conParam: ⇒ A)(f: DefaultDB ⇒ B)(implicit d: MongoDriver, m: ConnectionManager[A], c: ExecutionContext): Future[B] =
    withConnection(conParam) { _.database("acolyte").map(f) }

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
   * @see withFlatDB[T]
   */
  def withDB[T](con: ⇒ MongoConnection)(f: DefaultDB ⇒ T)(implicit c: ExecutionContext): Future[T] = con.database("acolyte").map(f)

  /**
   * Works with Mongo database (named "acolyte") resolved using given driver
   * initialized using Acolyte for ReactiveMongo
   * (should not be used with other driver instances).
   *
   * @param conParam $conParam
   * @param f $f
   *
   * {{{
   * import reactivemongo.api.DB
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * // handler: ConnectionHandler
   * val s: Future[String] = AcolyteDSL withFlatDB(handler) { db =>
   *   val d: DefaultDB = db
   *   Future.successful("Result")
   * }
   * }}}
   * @see withDB[A,B]
   */
  def withFlatDB[A, B](conParam: ⇒ A)(f: DefaultDB ⇒ Future[B])(implicit d: MongoDriver, m: ConnectionManager[A], c: ExecutionContext): Future[B] =
    withConnection(conParam) { _.database("acolyte").flatMap(f) }

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
   *     Future.successful("Result")
   *   }
   * }
   * }}}
   * @see AcolyteDSL.withConnection
   * @see withFlatDB[T]
   */
  def withFlatDB[T](con: ⇒ MongoConnection)(f: DefaultDB ⇒ Future[T])(implicit c: ExecutionContext): Future[T] = con.database("acolyte").flatMap(f)

}
