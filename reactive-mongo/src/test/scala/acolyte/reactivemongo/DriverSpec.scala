package acolyte.reactivemongo

import scala.util.Try
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration

import resource.{ ManagedResource, managed }

import reactivemongo.api.{ MongoDriver, MongoConnection, DefaultDB }
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{
  BSONBoolean,
  BSONDocument,
  BSONDouble,
  BSONInteger,
  BSONString
}
import reactivemongo.core.errors.DetailedDatabaseException
import reactivemongo.core.commands.LastError

object DriverSpec extends org.specs2.mutable.Specification
    with QueryHandlerFixtures with WriteHandlerFixtures
    with ConnectionHandlerFixtures with ResponseMatchers {

  "Acolyte Mongo driver" title

  "Acolyte driver" should {
    "successfully create connection" in {
      connect().acquireAndGet(identity).
        aka("connection") must not(throwA[Throwable])
    }

    "successfully select database" in {
      db("any-mockup-name").acquireAndGet(identity).
        aka("database") must not(throwA[Throwable])
    }

    "successfully select DB collection" in {
      collection("mock-collection").acquireAndGet(identity).
        aka("collection") must not(throwA[Throwable])
    }

    "return expected query result" >> {
      "when is successful #1" in withCol(query1.collection) { col ⇒
        awaitRes(col.find(query1.body).cursor[BSONDocument].toList()).
          aka("query result") must beSuccessfulTry[List[BSONDocument]].like {
            case ValueDocument(("b", BSONInteger(3)) :: Nil) :: Nil ⇒ ok
          }
      }

      "when is successful #2" in withCol(query2.collection) { col ⇒
        awaitRes(col.find(query2.body).cursor[BSONDocument].toList()).
          aka("query result") must beSuccessfulTry[List[BSONDocument]].like {
            case ValueDocument(("d", BSONDouble(4.56d)) :: Nil) ::
              ValueDocument(("ef", BSONString("ghi")) :: Nil) :: Nil ⇒ ok
          }
      }

      "as error when query handler returns no query result" in withCol(
        query3.collection) { col ⇒
          awaitRes(col.find(query3.body).cursor[BSONDocument].toList()).
            aka("query result") must beFailedTry.
            withThrowable[DetailedDatabaseException](".*No response: .*")
        }

      "as error when connection handler is empty" in withCol(query3.collection,
        collection(_, db("test-db",
          connect(managed(AcolyteDSL driver AcolyteDSL.handle))))) { col ⇒

          awaitRes(col.find(query3.body).cursor[BSONDocument].toList()).
            aka("query result") must beFailedTry.
            withThrowable[DetailedDatabaseException](".*No response: .*")
        }

      "as error when query handler is undefined" in withCol(query3.collection,
        collection(_, db("test-db",
          connect(managed(AcolyteDSL driver AcolyteDSL.handleWrite(
            { (_: WriteOp, _: Request) ⇒ WriteResponse(1 /* one doc */ ) }
          )))))) { col ⇒

          awaitRes(col.find(query3.body).cursor[BSONDocument].toList()).
            aka("query result") must beFailedTry.
            withThrowable[DetailedDatabaseException](".*No response: .*")

        }
    }

    "return expected write result" >> {
      "when error is raised without code" in withCol(write1._2.collection) {
        col ⇒
          awaitRes(col.remove(write1._2.body)).
            aka("write result") must beFailedTry.
            withThrowable[LastError](".*Error #2.*code = -1.*")
      }

      "when successful" in withCol(write2._2.collection) { col ⇒
        awaitRes(col.insert(write2._2.body)).
          aka("result") must beSuccessfulTry.like {
            case lastError ⇒
              lastError.elements.toList aka "body" must beLike {
                case ("ok", BSONInteger(1)) ::
                  ("updatedExisting", BSONBoolean(false)) ::
                  ("n", BSONInteger(0)) :: Nil ⇒ ok
              } and (lastError.ok aka "ok" must beTrue) and (
                lastError.n aka "updated" must_== 0) and (
                  lastError.inError aka "in-error" must beFalse) and (
                    lastError.err aka "err" must beNone) and (
                      lastError.errMsg aka "errmsg" must beNone)
          }
      }

      "as error when write handler returns no write result" in withCol(
        write3._2.collection) { col ⇒
          awaitRes(col.update(BSONDocument("name" -> "x"), write3._2.body)).
            aka("result") must beFailedTry.withThrowable[LastError](
              ".*No response: .*")
        }

      "as error when connection handler is empty" in withCol(query3.collection,
        collection(_, db("test-db",
          connect(managed(AcolyteDSL driver AcolyteDSL.handle))))) { col ⇒

          awaitRes(col.update(BSONDocument("name" -> "x"), write3._2.body)).
            aka("result") must beFailedTry.withThrowable[LastError](
              ".*No response: .*")
        }

      "as error when write handler is undefined" in withCol(query3.collection,
        collection(_, db("test-db",
          connect(managed(AcolyteDSL driver AcolyteDSL.handleQuery(
            { _: Request ⇒
              QueryResponse(BSONDocument("prop" -> "A"))
            })))))) { col ⇒

          awaitRes(col.update(BSONDocument("name" -> "x"), write3._2.body)).
            aka("result") must beFailedTry.withThrowable[LastError](
              ".*No response: .*")

        }
    }
  }

  // ---

  val driver: ManagedResource[MongoDriver] =
    managed(AcolyteDSL driver chandler1)

  def connect(d: ManagedResource[MongoDriver] = driver): ManagedResource[MongoConnection] = driver.flatMap(d ⇒ managed(d.connection(List("localhost"))))

  def db(n: String, con: ManagedResource[MongoConnection] = connect()): ManagedResource[DefaultDB] = con.map(_(n))

  def collection(n: String, d: ⇒ ManagedResource[DefaultDB] = db("test-db")): ManagedResource[BSONCollection] = d.map(_(n))

  def withCol[T](n: String, col: String ⇒ ManagedResource[BSONCollection] = collection(_))(f: BSONCollection ⇒ T): T = col(n).acquireAndGet(f)

  def awaitRes[T](f: Future[T], tmout: Duration = Duration(5, "seconds")): Try[T] = Try[T](Await.result(f, tmout))
}
