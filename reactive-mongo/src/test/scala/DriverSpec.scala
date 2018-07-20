package acolyte.reactivemongo

import scala.util.Try
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

import reactivemongo.core.errors.DetailedDatabaseException

import reactivemongo.bson.{ BSONDocument, BSONDouble, BSONInteger, BSONString }

import reactivemongo.api.{ DefaultDB, MongoConnection, MongoDriver }
import reactivemongo.api.commands.WriteResult

import org.specs2.concurrent.ExecutionEnv

class DriverSpec(implicit ee: ExecutionEnv)
  extends org.specs2.mutable.Specification
  with org.specs2.specification.AfterAll
  with QueryHandlerFixtures with WriteHandlerFixtures
  with ConnectionHandlerFixtures with ResponseMatchers {

  "Acolyte Mongo driver" title

  sequential // TODO: Remove

  import AcolyteDSL.withFlatDriver

  val timeout = 5.seconds
  val driver = MongoDriver()
  implicit val driverManager = DriverManager.identity(driver)

  "Resource management" should {
    "successfully initialize driver from connection handler" in {
      AcolyteDSL.withDriver(_ ⇒ true) must beTrue.await(0, timeout)
    }

    "successfully work with initialized driver" >> {
      "from sync query handler" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withQueryHandler(
            { _: Request ⇒ QueryResponse.empty })(_ ⇒ true)
        } aka "work with query handler" must beTrue.await(0, timeout)
      }

      "from sync query result" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withQueryResult(QueryResponse(
            BSONDocument("res" → "ult")))(_ ⇒ true)
        } aka "work with query result" must beTrue.await(0, timeout)
      }

      "from query handler with future result" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatQueryHandler(
            { _: Request ⇒ QueryResponse.undefined })(_ ⇒ Future(2 + 6))
        } aka "work with query handler" must beEqualTo(8).await(0, timeout)
      }

      "from future query result" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatQueryResult(QueryResponse(
            BSONDocument("res" → "ult")))(_ ⇒ Future(1 + 2))
        } aka "work with query result" must beEqualTo(3).await(0, timeout)
      }

      "from sync write handler" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withWriteHandler(
            { (_: WriteOp, _: Request) ⇒ WriteResponse(1) })(_ ⇒ true)
        } aka "work with write result" must beTrue.await(0, timeout)
      }

      "from sync write result" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withWriteResult(WriteResponse("error"))(_ ⇒ true)
        } aka "work with write result" must beTrue.await(0, timeout)
      }

      "from write handler with future result" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatWriteHandler(
            { (_: WriteOp, _: Request) ⇒ WriteResponse(1) })(_ ⇒ Future(1 + 6))
        } aka "work with write result" must beEqualTo(7).await(0, timeout)
      }

      "from sync future result" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatWriteResult(
            WriteResponse("error"))(_ ⇒ Future(1 + 2))
        } aka "work with write result" must beEqualTo(3).await(0, timeout)
      }
    }

    "successfully create connection" >> {
      "from connection handler" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withConnection(chandler1)(_ ⇒ true)
        } aka "work with connection" must beTrue.await(0, timeout)
      }

      "from initialized driver" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatConnection(chandler1)(_ ⇒ Future.successful(true))
        } aka "work with driver" must beTrue.await(0, timeout)
      }
    }

    "successfully select database" >> {
      "from connection handler" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withDB(chandler1)(_ ⇒ true)
        } aka "work with DB" must beTrue.await(0, timeout)
      }

      "from initialized driver and connection" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatConnection(chandler1) {
            AcolyteDSL.withDB(_)(_ ⇒ true)
          }
        } aka "work with DB" must beTrue.await(0, timeout)
      }

      "from initialized connection handler and connection with sync result" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatConnection(chandler1) { con ⇒
            AcolyteDSL.withDB(con)(_ ⇒ true)
          }
        } aka "work with DB" must beTrue.await(0, timeout)
      }

      "from initialized connection handler and connection with future" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatConnection(chandler1) { con ⇒
            AcolyteDSL.withFlatDB(con)(_ ⇒ Future(1 + 2))
          }
        } aka "work with DB" must beEqualTo(3).await(0, timeout)
      }

      "from initialized driver and connection with sync sync result" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatConnection(chandler1) {
            AcolyteDSL.withDB(_)(_ ⇒ true)
          }
        } aka "work with DB" must beTrue.await(0, timeout)
      }

      "from initialized driver and connection with sync sync result" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatConnection(chandler1) {
            AcolyteDSL.withFlatDB(_)(_ ⇒ Future(2 + 5))
          }
        } aka "work with DB" must beEqualTo(7).await(0, timeout)
      }
    }

    "successfully select DB collection" >> {
      "from connection handler with sync result" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withCollection(chandler1, "colName")(_ ⇒ true)
        } aka "work with collection" must beTrue.await(0, timeout)
      }

      "from initialized connection with sync result" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatConnection(chandler1) { con ⇒
            AcolyteDSL.withCollection(con, "colName")(_ ⇒ true)
          }
        } aka "work with collection" must beTrue.await(0, timeout)
      }

      "from resolved DB with sync result" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatDB(chandler1) { db ⇒
            AcolyteDSL.withCollection(db, "colName")(_ ⇒ true)
          }
        } aka "work with collection" must beTrue.await(0, timeout)
      }

      "from connection handler with future result" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatCollection(chandler1, "colName")(
            _ ⇒ Future.successful(true))
        } aka "work with collection" must beTrue.await(0, timeout)
      }

      "from initialized connection with future result" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatConnection(chandler1) { con ⇒
            AcolyteDSL.withFlatCollection(con, "colName")(_ ⇒ Future(1 + 3))
          }
        } aka "work with collection" must beEqualTo(4).await(0, timeout)
      }

      "from resolved DB with future result" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatDB(chandler1) { db ⇒
            AcolyteDSL.withFlatCollection(db, "colName")(_ ⇒ Future(2 + 5))
          }
        } aka "work with collection" must beEqualTo(7).await(0, timeout)
      }
    }
  }

  "Driver" should {
    "return expected query result" >> {
      "when is successful #1" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatCollection(chandler1, query1.collection) {
            _.find(query1.body.head).cursor[BSONDocument]().collect[List]()
          }
        } aka "query result" must beLike[List[BSONDocument]] {
          case ValueDocument(("b", BSONInteger(3)) :: Nil) :: Nil ⇒ ok
        }.await(0, timeout)
      }

      "when is successful #2" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatDB(chandler1) { db: DefaultDB ⇒
            db(query2.collection).find(query2.body.head).
              cursor[BSONDocument]().collect[List]()
          }
        } aka ("query result") must beLike[List[BSONDocument]] {
          case ValueDocument(("d", BSONDouble(4.56d)) :: Nil) ::
            ValueDocument(("ef", BSONString("ghi")) :: Nil) :: Nil ⇒ ok
        }.await(0, timeout)
      }

      "using withQueryResult" >> {
        "for a single document" in {
          withFlatDriver { implicit drv: MongoDriver ⇒
            AcolyteDSL.withFlatQueryResult(
              BSONDocument("res" → "ult", "n" → 3)) { con: MongoConnection ⇒
                con.database("anyDb").flatMap(_("anyCol").
                  find(query1.body.head).cursor[BSONDocument]().collect[List]())
              }
          } aka "query result" must beLike[List[BSONDocument]] {
            case ValueDocument(("res", BSONString("ult")) ::
              ("n", BSONInteger(3)) :: Nil) :: Nil ⇒ ok
          }.await(0, timeout)
        }

        "for a many documents" in {
          withFlatDriver { implicit drv: MongoDriver ⇒
            AcolyteDSL.withFlatQueryResult(
              List(BSONDocument("doc" → 1), BSONDocument("doc" → 2.3d))) { d ⇒
                AcolyteDSL.withFlatCollection(d, "anyCol") {
                  _.find(query1.body.head).
                    cursor[BSONDocument]().collect[List]()
                }
              }
          } aka "query result" must beLike[List[BSONDocument]] {
            case ValueDocument(("doc", BSONInteger(1)) :: Nil) ::
              ValueDocument(("doc", BSONDouble(2.3d)) :: Nil) :: Nil ⇒ ok
          }.await(0, timeout)
        }

        "for an explicit error" in {
          awaitRes(withFlatDriver { implicit drv: MongoDriver ⇒
            AcolyteDSL.withFlatQueryResult("Error" → 7) { con: MongoConnection ⇒
              AcolyteDSL.withFlatCollection(con, query1.collection) {
                _.find(query1.body.head).cursor[BSONDocument]().collect[List]()
              }
            }
          }) aka "query result" must beFailedTry.
            withThrowable[DetailedDatabaseException](".*Error.*code = 7.*")
        }

        "when undefined" in {
          awaitRes(withFlatDriver { implicit drv: MongoDriver ⇒
            AcolyteDSL.withFlatQueryResult(None) { con: MongoConnection ⇒
              AcolyteDSL.withFlatCollection(con, query1.collection) {
                _.find(query1.body.head).cursor[BSONDocument]().collect[List]()
              }
            }
          }) aka "query result" must beFailedTry.
            withThrowable[DetailedDatabaseException](".*No response:.*")
        }
      }

      "as error when query handler returns no query result" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatQueryHandler({
            case Request("acolyte.test3", SimpleBody(
              ("filter", BSONString("valC")) :: Nil)) ⇒ QueryResponse.empty
          }) { con: MongoConnection ⇒
            AcolyteDSL.withFlatCollection(con, query3.collection) {
              _.find(query3.body.head).
                cursor[BSONDocument]().collect[List]()
            }
          }
        }.map(_.isEmpty) aka "query result" must beTrue.await(0, timeout)
      }

      "support query options" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatQueryHandler({
            case Request("acolyte.test3", RequestBody(
              List(("filter", BSONString("valC"))) :: List(
                ("$orderby", ValueDocument(List(("foo", BSONInteger(1))))),
                ("$readPreference", ValueDocument(
                  List(("mode", BSONString("primary")))))
                ) :: Nil)) ⇒
              QueryResponse(BSONDocument("lorem" → 1.2D))
          }) { con: MongoConnection ⇒
            AcolyteDSL.withFlatCollection(con, query3.collection) {
              _.find(query3.body.head).sort(BSONDocument("foo" → 1)).
                cursor[BSONDocument]().collect[List]()
            }
          }
        }.map(_.size) aka "query result" must beEqualTo(1).await(0, timeout)
      }

      "support findAndModify" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatQueryHandler({
            case FindAndModifyRequest("test3", List(("id", BSONInteger(1))),
              List(("title", BSONString("foo"))), opts) ⇒
              QueryResponse.findAndModify(BSONDocument(opts))
          }) { con: MongoConnection ⇒
            AcolyteDSL.withFlatCollection(con, query3.collection) {
              _.findAndUpdate(
                BSONDocument("id" → 1),
                BSONDocument("title" → "foo")).map(_.value)
            }
          }
        } aka "query result" must beSome(BSONDocument(
          "bypassDocumentValidation" -> false,
          "writeConcern" -> BSONDocument("w" -> 1),
          "upsert" → false, "new" → false)).await(0, timeout)
      }

      "as error when connection handler is empty" in {
        awaitRes(withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatCollection(AcolyteDSL.handle, query3.collection) {
            _.find(query3.body.head).cursor[BSONDocument]().collect[List]()
          }
        }) aka "query result" must beFailedTry.like {
          case err ⇒
            err.getMessage.indexOf("No response: ") must not(beEqualTo(-1))
        }
      }

      "as error when query handler is undefined" in {
        lazy val handler = AcolyteDSL.handleWrite(
          { (_: WriteOp, _: Request) ⇒ WriteResponse(1 /* one doc */ ) })

        awaitRes(withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatConnection(handler) { con: MongoConnection ⇒
            con.database("anyDb").flatMap(
              _(query3.collection).find(query3.body.head).
                cursor[BSONDocument]().collect[List]())
          }
        }) aka "query result" must beFailedTry.like {
          case err ⇒
            err.getMessage.indexOf("No response: ") must not(beEqualTo(-1))
        }
      }
    }

    "return expected write result" >> {
      "when error is raised without code" in {
        awaitRes(withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatCollection(
            chandler1, write1._2.collection) { _.remove(write1._2.body.head) }
        }) aka "write result" must beFailedTry.like {
          case err ⇒ err.getMessage.indexOf("=Error #2").
            aka("errmsg") must not(beEqualTo(-1)) and {
              err.getMessage.indexOf("code=-1").
                aka("code") must not(beEqualTo(-1))
            }
        }
      }

      "when successful" in {
        withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatDB(chandler1) {
            _(write2._2.collection).insert(write2._2.body.head)
          }
        } aka "result" must beLike[WriteResult] {
          case result ⇒ result.ok aka "ok" must beTrue and (
            result.n aka "updated" must_== 0)
        }.await(0, timeout)
      }

      "as error when write handler returns no write result" in {
        awaitRes(withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatConnection(chandler1) { con ⇒
            val db = con.database("anyDb")
            val col = db.map(_(write3._2.collection))

            col.flatMap(_.update(
              BSONDocument("name" → "x"),
              write3._2.body.head))
          }
        }) aka "result" must beFailedTry.like {
          case err ⇒
            err.getMessage.indexOf("=No response: ") must not(beEqualTo(-1))
        }
      }

      "as error when connection handler is empty" in {
        awaitRes(withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatWriteHandler({ (_: WriteOp, _: Request) ⇒
            WriteResponse.undefined
          }) { d ⇒
            AcolyteDSL.withFlatCollection(d, query3.collection) {
              _.update(BSONDocument("name" → "x"), write3._2.body.head)
            }
          }
        }) aka "result" must beFailedTry.like {
          case err ⇒
            err.getMessage.indexOf("=No response: ") must not(beEqualTo(-1))
        }
      }

      "as error when write handler is undefined" in {
        awaitRes(withFlatDriver { implicit drv: MongoDriver ⇒
          AcolyteDSL.withFlatQueryResult(BSONDocument("prop" → "A")) { con ⇒
            AcolyteDSL.withFlatCollection(con, write3._2.collection) {
              _.update(BSONDocument("name" → "x"), write3._2.body.head)
            }
          }
        }) aka "result" must beFailedTry.like {
          case err ⇒
            err.getMessage.indexOf("=No response: ") must not(beEqualTo(-1))
        }
      }

      "using withWriteResult" >> {
        "for success count" in {
          withFlatDriver { implicit drv: MongoDriver ⇒
            AcolyteDSL.withFlatWriteResult(2 → true) {
              AcolyteDSL.withFlatConnection(_) { con: MongoConnection ⇒
                val db = con.database("anyDb")
                val col = db.map(_(write1._2.collection))
                col.flatMap(_.remove(write1._2.body.head))
              }
            }
          } aka "write result" must beLike[WriteResult] {
            case lastError ⇒ lastError.ok aka "ok" must beTrue and (
              lastError.n aka "updated" must_== 2)
          }.await(0, timeout)
        }

        "for explicit error" in {
          awaitRes(withFlatDriver { implicit drv: MongoDriver ⇒
            AcolyteDSL.withFlatWriteResult("Write err" → 9) { con ⇒
              AcolyteDSL.withFlatCollection(con, write2._2.collection) {
                _.insert(write2._2.body.head)
              }
            }
          }) aka "write result" must beFailedTry.like {
            case err ⇒ err.getMessage.indexOf("Write err").
              aka("errmsg") must not(beEqualTo(-1)) and {
                err.getMessage.indexOf("code=9") must not(beEqualTo(-1))
              }
          }
        }

        "when undefined" in {
          awaitRes(withFlatDriver { implicit drv: MongoDriver ⇒
            AcolyteDSL.withFlatWriteResult(None) { driver ⇒
              AcolyteDSL.withFlatCollection(driver, write3._2.collection) {
                _.update(BSONDocument(), write3._2.body.head)
              }
            }
          }) aka "write result" must beFailedTry.like {
            case err ⇒
              err.getMessage.indexOf("=No response: ") must not(beEqualTo(-1))
          }
        }
      }

      "using withWriteHandler" >> {
        "for insert" in {
          awaitRes(withFlatDriver { implicit drv: MongoDriver ⇒
            AcolyteDSL.withFlatWriteHandler({
              case InsertRequest("acolyte.col1", ("a", BSONString("val")) ::
                ("b", BSONInteger(2)) :: _) ⇒
                WriteResponse.successful(1, false)
            }) { con: MongoConnection ⇒
              AcolyteDSL.withFlatCollection(con, "col1") {
                _.insert(BSONDocument("a" → "val", "b" → 2))
              }
            }
          }) aka "write result" must beSuccessfulTry[WriteResult]
        }

        "for bulk-insert" in {
          withFlatDriver { implicit drv: MongoDriver ⇒
            AcolyteDSL.withFlatWriteHandler({
              case (InsertOp, Request("foo.bar", RequestBody(List(
                List(("foo", BSONInteger(1))),
                List(("bar", BSONInteger(2))),
                List(("lorem", BSONInteger(3)))
                )))) ⇒ WriteResponse.successful(count = 3)
            }) { con: MongoConnection ⇒
              for {
                db ← con.database("foo")
                res ← db.collection("bar").insert(ordered = true).many(Stream(
                  BSONDocument("foo" → 1),
                  BSONDocument("bar" → 2),
                  BSONDocument("lorem" → 3))) // TODO: test bulk update
              } yield res.ok
            }
          } must beTrue.await(0, timeout)
        }

        "for update" in {
          awaitRes(withFlatDriver { implicit drv: MongoDriver ⇒
            AcolyteDSL.withFlatWriteHandler({
              case UpdateRequest("acolyte.col2",
                ("sel", BSONString("hector")) :: Nil,
                ("filter", BSONString("valC")) :: Nil) ⇒
                WriteResponse(1)

              case UpdateRequest("acolyte.col2", x, y) ⇒
                sys.error(s"Foo: $x, $y")
            }) { con: MongoConnection ⇒
              AcolyteDSL.withFlatCollection(con, "col2") {
                _.update(BSONDocument("sel" → "hector"), write3._2.body.head)
              }
            }
          }) aka "write result" must beSuccessfulTry[WriteResult]
        }

        "for delete" in {
          withFlatDriver { implicit drv: MongoDriver ⇒
            AcolyteDSL.withFlatWriteHandler({
              case DeleteRequest("acolyte.col3",
                ("a", BSONString("val")) :: Nil) ⇒
                WriteResponse.successful(2, true)

            }) { con: MongoConnection ⇒
              AcolyteDSL.withFlatCollection(con, "col3") {
                _.remove(BSONDocument("a" → "val")).map(_ ⇒ {})
              }
            }
          } aka "write result" must beEqualTo({}).await(0, timeout)
        }
      }
    }
  }

  // ---

  def awaitRes[T](f: Future[T], tmout: Duration = Duration(5, "seconds")): Try[T] = Try[T](Await.result(f, tmout))

  def afterAll() = driver.close(Duration(10, "seconds"))
}
