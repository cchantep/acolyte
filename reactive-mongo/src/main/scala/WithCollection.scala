package acolyte.reactivemongo

import scala.concurrent.{ ExecutionContext, Future }
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{ DB, MongoConnection, MongoDriver }

/** Functions to work with a Mongo collection (provided DB functions). */
trait WithCollection { self: WithDB ⇒
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
   * @see AcolyteDSL.withDB[A,B]
   */
  def withCollection[A, B](conParam: ⇒ A, name: String)(f: BSONCollection ⇒ B)(
    implicit
    d: MongoDriver,
    m: ConnectionManager[A],
    ec: ExecutionContext,
    compose: ComposeWithCompletion[B]): compose.Outer =
    compose((for {
      con ← Future(m.open(d, conParam))
      db ← con.database("acolyte")
    } yield db.collection(name)), f) { coll ⇒
      m.releaseIfNecessary(coll.db.connection); ()
    }

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
   * @see WithDriver.withDB[T]
   */
  def withCollection[T](con: ⇒ MongoConnection, name: String)(
    f: BSONCollection ⇒ T)(
    implicit
    ec: ExecutionContext,
    compose: ComposeWithCompletion[T]): compose.Outer =
    compose(con.database("acolyte").map(_.collection(name)), f)(_ ⇒ {})

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
   * val s: Future[String] = AcolyteDSL.withDB(handler) { db =>
   *   AcolyteDSL.withCollection(db, "colName") { col =>
   *     "Result"
   *   }
   * }
   * }}}
   */
  def withCollection[T](db: DB, name: String)(
    f: BSONCollection ⇒ T)(
    implicit
    ec: ExecutionContext,
    compose: ComposeWithCompletion[T]): compose.Outer =
    compose(Future(db.collection(name)), f)(_ ⇒ {})

}
