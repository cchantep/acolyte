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

  "Resource management" should {
    "successfully initialize driver" >> {
      "from connection handler" in {
        AcolyteDSL.withDriver(chandler1)(_ ⇒ true).
          aka("work with driver") must beTrue.await(5)
      }

      "from sync query result" in {
        AcolyteDSL.withQueryResult(QueryResponse(
          BSONDocument("res" -> "ult")))(_ ⇒ true).
          aka("work with query result") must beTrue.await(5)
      }

      "from future query result" in {
        AcolyteDSL.withFlatQueryResult(QueryResponse(
          BSONDocument("res" -> "ult")))(_ ⇒ Future(1 + 2)).
          aka("work with query result") must beEqualTo(3).await(5)
      }

      "from sync write result" in {
        AcolyteDSL.withWriteResult(WriteResponse("error"))(_ ⇒ true).
          aka("work with write result") must beTrue.await(5)
      }

      "from sync future result" in {
        AcolyteDSL.withFlatWriteResult(
          WriteResponse("error"))(_ ⇒ Future(1 + 2)).
          aka("work with write result") must beEqualTo(3).await(5)
      }
    }

    "successfully create connection" >> {
      "from connection handler" in {
        AcolyteDSL.withConnection(chandler1)(_ ⇒ true).
          aka("work with connection") must beTrue.await(5)
      }

      "from initialized driver" in {
        AcolyteDSL.withFlatDriver(chandler1) { drv ⇒
          AcolyteDSL.withConnection(drv)(_ ⇒ true)
        } aka "work with driver" must beTrue.await(5)
      }
    }

    "successfully select database" >> {
      "from connection handler" in {
        AcolyteDSL.withDB(chandler1)(_ ⇒ true).
          aka("work with DB") must beTrue.await(5)
      }

      "from initialized driver and connection" in {
        AcolyteDSL.withFlatDriver(chandler1) { drv ⇒
          AcolyteDSL.withFlatConnection(drv) { con ⇒
            AcolyteDSL.withDB(drv)(_ ⇒ true)
          }
        } aka "work with DB" must beTrue.await(5)
      }

      "from initialized connection handler and connection with sync result" in {
        AcolyteDSL.withFlatConnection(chandler1) { con ⇒
          AcolyteDSL.withDB(con)(_ ⇒ true)
        } aka "work with DB" must beTrue.await(5)
      }

      "from initialized connection handler and connection with future" in {
        AcolyteDSL.withFlatConnection(chandler1) { con ⇒
          AcolyteDSL.withFlatDB(con)(_ ⇒ Future(1 + 2))
        } aka "work with DB" must beEqualTo(3).await(5)
      }

      "from initialized driver and connection with sync sync result" in {
        AcolyteDSL.withFlatDriver(chandler1) { drv ⇒
          AcolyteDSL.withFlatConnection(drv) { con ⇒
            AcolyteDSL.withDB(con)(_ ⇒ true)
          }
        } aka "work with DB" must beTrue.await(5)
      }

      "from initialized driver and connection with sync sync result" in {
        AcolyteDSL.withFlatDriver(chandler1) { drv ⇒
          AcolyteDSL.withFlatConnection(drv) { con ⇒
            AcolyteDSL.withFlatDB(con)(_ ⇒ Future(2 + 5))
          }
        } aka "work with DB" must beEqualTo(7).await(5)
      }
    }

    "successfully select DB collection" >> {
      "from connection handler with sync result" in {
        AcolyteDSL.withCollection(chandler1, "colName")(_ ⇒ true).
          aka("work with collection") must beTrue.await(5)
      }

      "from initialized connection with sync result" in {
        AcolyteDSL.withFlatConnection(chandler1) { con ⇒
          AcolyteDSL.withCollection(con, "colName")(_ ⇒ true)
        } aka "work with collection" must beTrue.await(5)
      }

      "from resolved DB with sync result" in {
        AcolyteDSL.withFlatDB(chandler1) { db ⇒
          AcolyteDSL.withCollection(db, "colName")(_ ⇒ true)
        } aka "work with collection" must beTrue.await(5)
      }

      "from connection handler with future result" in {
        AcolyteDSL.withFlatCollection(chandler1, "colName")(
          _ ⇒ Future.successful(true)).
          aka("work with collection") must beTrue.await(5)
      }

      "from initialized connection with future result" in {
        AcolyteDSL.withFlatConnection(chandler1) { con ⇒
          AcolyteDSL.withFlatCollection(con, "colName")(_ ⇒ Future(1 + 3))
        } aka "work with collection" must beEqualTo(4).await(5)
      }

      "from resolved DB with future result" in {
        AcolyteDSL.withFlatDB(chandler1) { db ⇒
          AcolyteDSL.withFlatCollection(db, "colName")(_ ⇒ Future(2 + 5))
        } aka "work with collection" must beEqualTo(7).await(5)
      }
    }

    //"return expected query result" >> {
    /*
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
       */

    // ___here
    /*
      "using withQueryResult for a single document" in AcolyteDSL.
        withQueryResult(BSONDocument("res" -> "ult", "n" -> 3)) { driver ⇒
          AcolyteDSL.withCollection(driver, "test-col") { col ⇒
            awaitRes(col.find(query1.body).cursor[BSONDocument].toList()).
              aka("query result") must beSuccessfulTry[List[BSONDocument]].
              like {
                case ValueDocument(("res", BSONString("ult")) ::
                  ("n", BSONInteger(3)) :: Nil) :: Nil ⇒ ok
              }
          }
        }.await(5)
       */

    /*
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
       */
    //}

    /*
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
     */
  }

  // ---

  def awaitRes[T](f: Future[T], tmout: Duration = Duration(5, "seconds")): Try[T] = Try[T](Await.result(f, tmout))
}
