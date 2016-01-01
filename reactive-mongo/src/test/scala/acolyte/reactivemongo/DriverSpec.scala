package acolyte.reactivemongo

import scala.util.Try
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration

import resource.{ ManagedResource, managed }

import reactivemongo.api.{ MongoDriver, MongoConnection, DefaultDB }
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{
  BSONBoolean,
  BSONDocument,
  BSONDouble,
  BSONInteger,
  BSONString
}
import reactivemongo.core.errors.DetailedDatabaseException
import reactivemongo.api.commands.{ LastError, WriteResult }

object DriverSpec extends org.specs2.mutable.Specification
    with QueryHandlerFixtures with WriteHandlerFixtures
    with ConnectionHandlerFixtures with ResponseMatchers {

  "Acolyte Mongo driver" title

  "Resource management" should {
    "successfully initialize driver from connection handler" in {
      AcolyteDSL.withDriver(_ ⇒ true).
        aka("work with driver") must beTrue.await(5)
    }

    "successfully work with initialized driver" >> new WDriver {
      implicit val d = driver

      "from sync query handler" in {
        AcolyteDSL.withQueryHandler(
          { _: Request ⇒ QueryResponse.empty })(_ ⇒ true).
          aka("work with query handler") must beTrue.await(5)
      }

      "from sync query result" in {
        AcolyteDSL.withQueryResult(QueryResponse(
          BSONDocument("res" -> "ult")))(_ ⇒ true).
          aka("work with query result") must beTrue.await(5)
      }

      "from query handler with future result" in {
        AcolyteDSL.withFlatQueryHandler(
          { _: Request ⇒ QueryResponse.undefined })(_ ⇒ Future(2 + 6)).
          aka("work with query handler") must beEqualTo(8).await(5)
      }

      "from future query result" in {
        AcolyteDSL.withFlatQueryResult(QueryResponse(
          BSONDocument("res" -> "ult")))(_ ⇒ Future(1 + 2)).
          aka("work with query result") must beEqualTo(3).await(5)
      }

      "from sync write handler" in {
        AcolyteDSL.withWriteHandler(
          { (_: WriteOp, _: Request) ⇒ WriteResponse(1) })(_ ⇒ true).
          aka("work with write result") must beTrue.await(5)
      }

      "from sync write result" in {
        AcolyteDSL.withWriteResult(WriteResponse("error"))(_ ⇒ true).
          aka("work with write result") must beTrue.await(5)
      }

      "from write handler with future result" in {
        AcolyteDSL.withFlatWriteHandler(
          { (_: WriteOp, _: Request) ⇒ WriteResponse(1) })(_ ⇒ Future(1 + 6)).
          aka("work with write result") must beEqualTo(7).await(5)
      }

      "from sync future result" in {
        AcolyteDSL.withFlatWriteResult(
          WriteResponse("error"))(_ ⇒ Future(1 + 2)).
          aka("work with write result") must beEqualTo(3).await(5)
      }
    }

    "successfully create connection" >> new WDriver {
      implicit val d = driver

      "from connection handler" in {
        AcolyteDSL.withConnection(chandler1)(_ ⇒ true).
          aka("work with connection") must beTrue.await(5)
      }

      "from initialized driver" in {
        AcolyteDSL.withFlatConnection(chandler1)(_ ⇒ Future.successful(true)).
          aka("work with driver") must beTrue.await(5)
      }
    }

    "successfully select database" >> new WDriver {
      implicit val d = driver

      "from connection handler" in {
        AcolyteDSL.withDB(chandler1)(_ ⇒ true).
          aka("work with DB") must beTrue.await(5)
      }

      "from initialized driver and connection" in {
        AcolyteDSL.withFlatConnection(chandler1) {
          AcolyteDSL.withDB(_)(_ ⇒ true)
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
        AcolyteDSL.withFlatConnection(chandler1) {
          AcolyteDSL.withDB(_)(_ ⇒ true)
        } aka "work with DB" must beTrue.await(5)
      }

      "from initialized driver and connection with sync sync result" in {
        AcolyteDSL.withFlatConnection(chandler1) {
          AcolyteDSL.withFlatDB(_)(_ ⇒ Future(2 + 5))
        } aka "work with DB" must beEqualTo(7).await(5)
      }
    }

    "successfully select DB collection" >> new WDriver {
      implicit val d = driver

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
  }

  "Driver" should {
    "return expected query result" >> new WDriver {
      implicit val d = driver

      "when is successful #1" in {
        awaitRes(AcolyteDSL.withFlatCollection(chandler1, query1.collection) {
          col ⇒ col.find(query1.body.head).
            cursor[BSONDocument]().collect[List]()
        }) aka "query result" must beSuccessfulTry[List[BSONDocument]].like {
          case ValueDocument(("b", BSONInteger(3)) :: Nil) :: Nil ⇒ ok
        }
      }

      "when is successful #2" in {
        awaitRes(AcolyteDSL.withFlatDB(chandler1) { db ⇒
          db(query2.collection).find(query2.body.head).
            cursor[BSONDocument]().collect[List]()
        }) aka ("query result") must beSuccessfulTry[List[BSONDocument]].like {
          case ValueDocument(("d", BSONDouble(4.56d)) :: Nil) ::
            ValueDocument(("ef", BSONString("ghi")) :: Nil) :: Nil ⇒ ok
        }
      }

      "using withQueryResult" >> {
        "for a single document" in {
          awaitRes(AcolyteDSL.withFlatQueryResult(
            BSONDocument("res" -> "ult", "n" -> 3)) { driver ⇒
              AcolyteDSL.withFlatConnection(driver) { con ⇒
                val db = con("anyDb")
                db("anyCol").find(query1.body.head).
                  cursor[BSONDocument]().collect[List]()
              }
            }) aka "query result" must beSuccessfulTry[List[BSONDocument]].
            like {
              case ValueDocument(("res", BSONString("ult")) ::
                ("n", BSONInteger(3)) :: Nil) :: Nil ⇒ ok
            }
        }

        "for a many documents" in {
          awaitRes(AcolyteDSL.withFlatQueryResult(
            List(BSONDocument("doc" -> 1), BSONDocument("doc" -> 2.3d))) { d ⇒
              AcolyteDSL.withFlatCollection(d, "anyCol") {
                _.find(query1.body.head).cursor[BSONDocument]().collect[List]()
              }
            }) aka "query result" must beSuccessfulTry[List[BSONDocument]].
            like {
              case ValueDocument(("doc", BSONInteger(1)) :: Nil) ::
                ValueDocument(("doc", BSONDouble(2.3d)) :: Nil) :: Nil ⇒ ok
            }
        }

        "for an explicit error" in {
          awaitRes(AcolyteDSL.withFlatQueryResult("Error" -> 7) { driver ⇒
            AcolyteDSL.withFlatCollection(driver, query1.collection) {
              _.find(query1.body.head).cursor[BSONDocument]().collect[List]()
            }
          }) aka "query result" must beFailedTry.
            withThrowable[DetailedDatabaseException](".*Error.*code = 7.*")
        }

        "when undefined" in {
          awaitRes(AcolyteDSL.withFlatQueryResult(None) { driver ⇒
            AcolyteDSL.withFlatCollection(driver, query1.collection) {
              _.find(query1.body.head).cursor[BSONDocument]().collect[List]()
            }
          }) aka "query result" must beFailedTry.
            withThrowable[DetailedDatabaseException](".*No response:.*")
        }
      }

      "as error when query handler returns no query result" in {
        awaitRes(AcolyteDSL.withFlatQueryHandler(
          { _: Request ⇒ QueryResponse.empty }) { d ⇒
            AcolyteDSL.withFlatCollection(d, query3.collection) {
              _.find(query3.body.head).cursor[BSONDocument]().collect[List]()
            }
          }) aka "query result" must beSuccessfulTry.like {
          case res if res.isEmpty ⇒ ok
        }
      }

      "as error when connection handler is empty" in {
        awaitRes(AcolyteDSL.withFlatCollection(AcolyteDSL.handle,
          query3.collection) {
            _.find(query3.body.head).cursor[BSONDocument]().collect[List]()
          }) aka "query result" must beFailedTry.
          withThrowable[DetailedDatabaseException](".*No response: .*")
      }

      "as error when query handler is undefined" in {
        lazy val handler = AcolyteDSL.handleWrite(
          { (_: WriteOp, _: Request) ⇒ WriteResponse(1 /* one doc */ ) })

        awaitRes(AcolyteDSL.withFlatConnection(handler) { con ⇒
          val db = con("anyDb")
          db(query3.collection).find(query3.body.head).
            cursor[BSONDocument]().collect[List]()

        }) aka "query result" must beFailedTry.
          withThrowable[DetailedDatabaseException](".*No response: .*")

      }
    }

    "return expected write result" >> new WDriver {
      implicit val d = driver

      "when error is raised without code" in {
        awaitRes(AcolyteDSL.withFlatCollection(
          chandler1, write1._2.collection) { _.remove(write1._2.body.head) }).
          aka("write result") must beFailedTry.
          withThrowable[LastError](".*Error #2.*code = -1.*")
      }

      "when successful" in {
        awaitRes(AcolyteDSL.withFlatDB(chandler1) {
          _(write2._2.collection).insert(write2._2.body.head)
        }) aka "result" must beSuccessfulTry.like {
          case result ⇒
            result.ok aka "ok" must beTrue and (
              result.n aka "updated" must_== 0) and (
                result.inError aka "in-error" must beFalse) and (
              result.errmsg aka "errmsg" must beNone)
        }
      }

      "as error when write handler returns no write result" in {
        awaitRes(AcolyteDSL.withFlatConnection(chandler1) { con ⇒
          val db = con("anyDb")
          val col = db(write3._2.collection)

          col.update(BSONDocument("name" -> "x"), write3._2.body.head)
        }) aka "result" must beFailedTry.
          withThrowable[LastError](".*No response: .*")
      }

      "as error when connection handler is empty" in {
        awaitRes(AcolyteDSL.withFlatWriteHandler({ (_: WriteOp, _: Request) ⇒
          WriteResponse.undefined
        }) { d ⇒
          AcolyteDSL.withFlatCollection(d, query3.collection) {
            _.update(BSONDocument("name" -> "x"), write3._2.body.head)
          }
        }) aka "result" must beFailedTry.
          withThrowable[LastError](".*No response: .*")
      }

      "as error when write handler is undefined" in {
        awaitRes(AcolyteDSL.withFlatQueryResult(BSONDocument("prop" -> "A")) {
          driver ⇒
            AcolyteDSL.withFlatCollection(driver, write3._2.collection) {
              _.update(BSONDocument("name" -> "x"), write3._2.body.head)
            }
        }) aka "result" must beFailedTry.
          withThrowable[LastError](".*No response: .*")
      }

      "using withWriteResult" >> {
        "for success count" in {
          awaitRes(AcolyteDSL.withFlatWriteResult(2 -> true) { driver ⇒
            AcolyteDSL.withFlatConnection(driver) { con ⇒
              val db = con("anyDb")
              val col = db(write1._2.collection)
              col.remove(write1._2.body.head)
            }
          }) aka "write result" must beSuccessfulTry.like {
            case lastError ⇒
              lastError.ok aka "ok" must beTrue and (
                lastError.n aka "updated" must_== 2) and (
                  lastError.inError aka "errored" must beFalse)
          }
        }

        "for explicit error" in {
          awaitRes(AcolyteDSL.withFlatWriteResult("Write err" -> 9) { driver ⇒
            AcolyteDSL.withFlatCollection(driver, write2._2.collection) {
              _.insert(write2._2.body.head)
            }
          }) aka "write result" must beFailedTry.
            withThrowable[LastError](".*Write err.*code = 9.*")
        }

        "when undefined" in {
          awaitRes(AcolyteDSL.withFlatWriteResult(None) { driver ⇒
            AcolyteDSL.withFlatCollection(driver, write3._2.collection) {
              _.update(BSONDocument(), write3._2.body.head)
            }
          }) aka "write result" must beFailedTry.
            withThrowable[LastError](".*No response.*")
        }
      }

      "using withWriteHandler" >> {
        "for insert" in {
          awaitRes(AcolyteDSL.withFlatWriteHandler({
            case InsertRequest("acolyte.col1", ("a", BSONString("val")) ::
              ("b", BSONInteger(2)) :: ("_id", _) :: Nil) ⇒
              WriteResponse.successful(1, false)
          }) { driver ⇒
            AcolyteDSL.withFlatCollection(driver, "col1") {
              _.insert(BSONDocument("a" -> "val", "b" -> 2))
            }
          }) aka "write result" must beSuccessfulTry[WriteResult]
        }

        "for update" in {
          awaitRes(AcolyteDSL.withFlatWriteHandler({
            case UpdateRequest("acolyte.col2",
              ("sel", BSONString("hector")) :: Nil,
              ("filter", BSONString("valC")) :: Nil) ⇒
              WriteResponse(1)
          }) { driver ⇒
            AcolyteDSL.withFlatCollection(driver, "col2") {
              _.update(BSONDocument("sel" -> "hector"), write3._2.body.head)
            }
          }) aka "write result" must beSuccessfulTry[WriteResult]
        }

        "for delete" in {
          awaitRes(AcolyteDSL.withFlatWriteHandler({
            case DeleteRequest("acolyte.col3",
              ("a", BSONString("val")) :: Nil) ⇒
              WriteResponse.successful(2, true)
          }) { driver ⇒
            AcolyteDSL.withFlatCollection(driver, "col3") {
              _.remove(BSONDocument("a" -> "val"))
            }
          }) aka "write result" must beSuccessfulTry[WriteResult]
        }
      }
    }
  }

  // ---

  def awaitRes[T](f: Future[T], tmout: Duration = Duration(5, "seconds")): Try[T] = Try[T](Await.result(f, tmout))
}

sealed trait WDriver extends org.specs2.mutable.After {
  lazy val driver = AcolyteDSL.driver
  def after = driver.close()
}
