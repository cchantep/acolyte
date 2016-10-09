package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{ DB, MongoConnection, MongoDriver }

/** Functions to work with a Mongo collection (provided DB functions). */
trait WithCollection { withDB: WithDB ⇒
  /**
   * Works with specified collection from MongoDB "acolyte"
   * resolved using given driver initialized with Acolyte for ReactiveMongo
   * (should not be used with other driver instances).
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @param conParam Connection manager parameter (see [[ConnectionManager]])
   * @param name Collection name
   * @param f Function applied to resolved Mongo collection
   *
   * {{{
   * import reactivemongo.api.Collection
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * // handler: ConnectionHandler
   * val s: Future[String] =
   *   AcolyteDSL.withCollection(handler, "colName") { col =>
   *     "Result"
   *   }
   * }}}
   * @see AcolyteDSL.withFlatDB[A,B]
   */
  def withCollection[A, B](conParam: ⇒ A, name: String)(f: BSONCollection ⇒ B)(implicit d: MongoDriver, m: ConnectionManager[A], c: ExecutionContext): Future[B] = withFlatDB(conParam) { db ⇒ Future(f(db(name))) }

  /**
   * Works with specified collection from MongoDB "acolyte"
   * resolved using given connection.
   *
   * @param con Previously initialized connection
   * @param name Collection name
   * @param f Function applied to resolved Mongo collection
   *
   * {{{
   * import reactivemongo.api.Collection
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * // handler: ConnectionHandler
   * val s: Future[String] = AcolyteDSL.withFlatConnection(handler) { con =>
   *   AcolyteDSL.withCollection(con, "colName") { col =>
   *     "Result"
   *   }
   * }
   * }}}
   * @see WithDriver.withFlatDB[T]
   */
  def withCollection[T](con: ⇒ MongoConnection, name: String)(f: BSONCollection ⇒ T)(implicit c: ExecutionContext): Future[T] = withFlatDB(con) { db ⇒ Future(f(db(name))) }

  /**
   * Works with specified collection from MongoDB "acolyte"
   * resolved using given Mongo DB.
   *
   * @param db Previously resolved Mongo DB
   * @param name Collection name
   * @param f Function applied to resolved Mongo collection
   *
   * {{{
   * import reactivemongo.api.Collection
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * // handler: ConnectionHandler
   * val s: Future[String] = AcolyteDSL.withFlatDB(handler) { db =>
   *   AcolyteDSL.withCollection(db, "colName") { col =>
   *     "Result"
   *   }
   * }
   * }}}
   */
  def withCollection[T](db: DB, name: String)(f: BSONCollection ⇒ T)(implicit c: ExecutionContext): Future[T] = Future(f(db(name)))

  /**
   * Works with specified collection from MongoDB "acolyte"
   * resolved using given driver initialized with Acolyte for ReactiveMongo
   * (should not be used with other driver instances).
   * Driver and associated resources are released
   * after the function `f` the result `Future` is completed.
   *
   * @param conParam Connection manager parameter (see [[ConnectionManager]])
   * @param name Collection name
   * @param f Function applied to resolved Mongo collection
   *
   * {{{
   * import reactivemongo.api.Collection
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * // handler: ConnectionHandler
   * val i: Future[Int] =
   *   AcolyteDSL.withFlatCollection(handler, "colName") { col =>
   *     Future(1 + 2)
   *   }
   * }}}
   * @see AcolyteDSL.withFlatDB[A,B]
   */
  def withFlatCollection[A, B](conParam: ⇒ A, name: String)(f: BSONCollection ⇒ Future[B])(implicit d: MongoDriver, m: ConnectionManager[A], c: ExecutionContext): Future[B] = withFlatDB(conParam) { db ⇒ f(db(name)) }

  /**
   * Works with specified collection from MongoDB "acolyte"
   * resolved using given connection.
   *
   * @param con Previously initialized connection
   * @param name Collection name
   * @param f Function applied to resolved Mongo collection
   *
   * {{{
   * import reactivemongo.api.Collection
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * // handler: ConnectionHandler
   * val i: Future[Int] = AcolyteDSL.withFlatConnection(handler) { con =>
   *   AcolyteDSL.withFlatCollection(con, "colName") { col =>
   *     Future(1 + 2)
   *   }
   * }
   * }}}
   * @see WithDriver.withFlatDB[T]
   */
  def withFlatCollection[T](con: ⇒ MongoConnection, name: String)(f: BSONCollection ⇒ Future[T])(implicit c: ExecutionContext): Future[T] = withFlatDB(con) { db ⇒ f(db(name)) }

  /**
   * Works with specified collection from MongoDB "acolyte"
   * resolved using given Mongo DB.
   *
   * @param db Previously resolved Mongo DB
   * @param name Collection name
   * @param f Function applied to resolved Mongo collection
   *
   * {{{
   * import reactivemongo.api.Collection
   * import acolyte.reactivemongo.AcolyteDSL
   *
   * // handler: ConnectionHandler
   * val i: Future[Int] = AcolyteDSL.withFlatDB(handler) { db =>
   *   AcolyteDSL.withFlatCollection(db, "colName") { col =>
   *     Future(1 + 2)
   *   }
   * }
   * }}}
   */
  def withFlatCollection[T](db: DB, name: String)(f: BSONCollection ⇒ Future[T])(implicit c: ExecutionContext): Future[T] = f(db(name))

}
