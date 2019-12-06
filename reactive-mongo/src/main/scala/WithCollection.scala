package acolyte.reactivemongo

import scala.concurrent.ExecutionContext

import reactivemongo.api.{ DB, MongoConnection, MongoDriver }
import reactivemongo.api.bson.collection.BSONCollection

/**
 * Functions to work with a Mongo collection (provided DB functions).
 *
 * @define conParam Connection manager parameter (see [[ConnectionManager]])
 * @define nameParam the name of the collection
 */
trait WithCollection { self: WithDB ⇒
  /**
   * Works with specified collection from MongoDB "acolyte"
   * resolved using given driver initialized with Acolyte for ReactiveMongo
   * (should not be used with other driver instances).
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @param conParam $conParam
   * @param name $nameParam
   * @param f Function applied to resolved Mongo collection
   *
   * {{{
   * import scala.concurrent.{ ExecutionContext, Future }
   * import reactivemongo.api.{ Collection, MongoDriver }
   * import acolyte.reactivemongo.{ AcolyteDSL, ConnectionManager }
   *
   * // handler: ConnectionHandler
   * def s[T: ConnectionManager](handler: => T)(
   *   implicit ec: ExecutionContext, d: MongoDriver): Future[String] =
   *   AcolyteDSL.withCollection(handler, "colName") { col =>
   *     "Result"
   *   }
   * }}}
   * @see AcolyteDSL.withDB[A,B]
   */
  def withCollection[A, B](conParam: ⇒ A, name: String)(f: BSONCollection ⇒ B)(
    implicit
    d: MongoDriver,
    m: ConnectionManager[A],
    ec: ExecutionContext,
    compose: ComposeWithCompletion[B]): compose.Outer =
    withDB(conParam) { db ⇒ f(db.collection(name)) }

  /**
   * Works with specified collection from MongoDB "acolyte"
   * resolved using given connection.
   *
   * @param con Previously initialized connection
   * @param name $nameParam
   * @param f Function applied to resolved Mongo collection
   *
   * {{{
   * import scala.concurrent.{ ExecutionContext, Future }
   * import reactivemongo.api.{ Collection, MongoDriver }
   * import acolyte.reactivemongo.{ AcolyteDSL, ConnectionHandler }
   *
   * // handler: ConnectionHandler
   * def s(handler: ConnectionHandler)(
   *   implicit ec: ExecutionContext, d: MongoDriver): Future[String] =
   *   AcolyteDSL.withConnection(handler) { con =>
   *     AcolyteDSL.withCollection(con, "colName") { col =>
   *       "Result"
   *     }
   *   }
   * }}}
   * @see WithDriver.withDB[T]
   */
  def withCollection[T](con: ⇒ MongoConnection, name: String)(
    f: BSONCollection ⇒ T)(
    implicit
    ec: ExecutionContext,
    compose: ComposeWithCompletion[T]): compose.Outer =
    withDB(con) { db ⇒ f(db.collection(name)) }

  /**
   * Works with specified collection from MongoDB "acolyte"
   * resolved using given Mongo DB.
   *
   * @param db Previously resolved Mongo DB
   * @param name $nameParam
   * @param f Function applied to resolved Mongo collection
   *
   * {{{
   * import scala.concurrent.{ ExecutionContext, Future }
   * import reactivemongo.api.{ Collection, MongoDriver }
   * import acolyte.reactivemongo.{ AcolyteDSL, ConnectionHandler }
   *
   * def s(handler: ConnectionHandler)(
   *   implicit ec: ExecutionContext, d: MongoDriver): Future[String] =
   *   AcolyteDSL.withDB(handler) { db =>
   *     AcolyteDSL.withCollection(db, "colName") { col =>
   *       "Result"
   *     }
   *   }
   * }}}
   */
  def withCollection[T](
    db: DB,
    name: String)(
    f: BSONCollection ⇒ T): T = f(db.collection(name))

}
