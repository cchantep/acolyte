package acolyte.reactivemongo

import scala.util.Try
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

import reactivemongo.core.errors.DatabaseException

import reactivemongo.api.bson.{
  BSONBoolean,
  BSONDocument,
  BSONDouble,
  BSONInteger,
  BSONString
}

import reactivemongo.api.{ Cursor, DB, MongoConnection, AsyncDriver }
import reactivemongo.acolyte.ActorSystem
import reactivemongo.api.commands.WriteResult

import org.specs2.concurrent.ExecutionEnv

final class DriverSpec extends org.specs2.mutable.Specification
  with org.specs2.specification.AfterAll
  with QueryHandlerFixtures with WriteHandlerFixtures
  with ConnectionHandlerFixtures with ResponseMatchers {

  "Acolyte Mongo driver" title

  import AcolyteDSL.withDriver

  val timeout = 5.seconds
  val driver = AsyncDriver()
  implicit val driverManager = DriverManager.identity(driver)
  implicit def ec: ExecutionEnv =
    ExecutionEnv.fromExecutionContext(ActorSystem(driver).dispatcher)

  "Resource management" should {
    "successfully initialize driver from connection handler" in {
      withDriver(_ => true) must beTrue.await(0, timeout)
    }

    "successfully work with initialized driver" >> {
      "from sync query handler" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withQueryHandler(
            { _: Request => QueryResponse.empty })(_ => true)
        } aka "work with query handler" must beTrue.await(0, timeout)
      }

      "from sync query result" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withQueryResult(QueryResponse(
            BSONDocument("res" → "ult")))(_ => true)
        } aka "work with query result" must beTrue.await(0, timeout)
      }

      "from query handler with future result" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withQueryHandler(
            { _: Request => QueryResponse.undefined })(_ => Future(2 + 6))
        } aka "work with query handler" must beTypedEqualTo(8).await(0, timeout)
      }

      "from future query result" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withQueryResult(QueryResponse(
            BSONDocument("res" → "ult")))(_ => Future(1 + 2))
        } aka "work with query result" must beTypedEqualTo(3).await(0, timeout)
      }

      "from sync write handler" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withWriteHandler(
            { (_: WriteOp, _: Request) => WriteResponse(1) })(_ => true)
        } aka "work with write result" must beTrue.await(0, timeout)
      }

      "from sync write result" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withWriteResult(WriteResponse("error"))(_ => true)
        } aka "work with write result" must beTrue.await(0, timeout)
      }

      "from write handler with future result" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withWriteHandler(
            { (_: WriteOp, _: Request) => WriteResponse(1) })(_ => Future(1 + 6))
        } aka "work with write result" must beTypedEqualTo(7).await(0, timeout)
      }

      "from sync future result" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withWriteResult(
            WriteResponse("error"))(_ => Future(1 + 2))
        } aka "work with write result" must beTypedEqualTo(3).await(0, timeout)
      }
    }

    "successfully create connection" >> {
      "from connection handler" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withConnection(chandler1)(_ => true)
        } aka "work with connection" must beTrue.await(0, timeout)
      }

      "from initialized driver" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withConnection(chandler1)(_ => Future.successful(true))
        } aka "work with driver" must beTrue.await(0, timeout)
      }
    }

    "successfully select database" >> {
      "from connection handler" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withDB(chandler1)(_ => true)
        } aka "work with DB" must beTrue.await(0, timeout)
      }

      "from initialized driver and connection" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withConnection(chandler1) {
            AcolyteDSL.withDB(_)(_ => true)
          }
        } aka "work with DB" must beTrue.await(0, timeout)
      }

      "from initialized connection handler and connection with sync result" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withConnection(chandler1) { con =>
            AcolyteDSL.withDB(con)(_ => true)
          }
        } aka "work with DB" must beTrue.await(0, timeout)
      }

      "from initialized connection handler and connection with future" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withConnection(chandler1) { con =>
            AcolyteDSL.withDB(con)(_ => Future(1 + 2))
          }
        } aka "work with DB" must beTypedEqualTo(3).await(0, timeout)
      }

      "from initialized driver and connection with sync sync result" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withConnection(chandler1) {
            AcolyteDSL.withDB(_)(_ => true)
          }
        } aka "work with DB" must beTrue.await(0, timeout)
      }

      "from initialized driver and connection with sync sync result" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withConnection(chandler1) {
            AcolyteDSL.withDB(_)(_ => Future(2 + 5))
          }
        } aka "work with DB" must beTypedEqualTo(7).await(0, timeout)
      }
    }

    "successfully select DB collection" >> {
      "from connection handler with sync result" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withCollection(chandler1, "colName")(_ => true)
        } aka "work with collection" must beTrue.await(0, timeout)
      }

      "from initialized connection with sync result" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withConnection(chandler1) { con =>
            AcolyteDSL.withCollection(con, "colName")(_ => true)
          }
        } aka "work with collection" must beTrue.await(0, timeout)
      }

      "from resolved DB with sync result" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withDB(chandler1) { db =>
            AcolyteDSL.withCollection(db, "colName")(_ => true)
          }
        } aka "work with collection" must beTrue.await(0, timeout)
      }

      "from connection handler with future result" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withCollection(chandler1, "colName")(
            _ => Future.successful(true))
        } aka "work with collection" must beTrue.await(0, timeout)
      }

      "from initialized connection with future result" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withConnection(chandler1) { con =>
            AcolyteDSL.withCollection(con, "colName")(_ => Future(1 + 3))
          }
        } aka "work with collection" must beTypedEqualTo(4).await(0, timeout)
      }

      "from resolved DB with future result" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withDB(chandler1) { db =>
            AcolyteDSL.withCollection(db, "colName")(_ => Future(2 + 5))
          }
        } aka "work with collection" must beTypedEqualTo(7).await(0, timeout)
      }
    }
  }

  "Driver" should {
    "return expected query result" >> {
      "when is successful #1" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withCollection(chandler1, query1.collection) {
            _.find(query1.body.head, Option.empty[BSONDocument]).
              cursor[BSONDocument]().
              collect[List](-1, Cursor.FailOnError[List[BSONDocument]]())
          }
        } aka "query result" must beLike[List[BSONDocument]] {
          case ValueDocument(("b", BSONInteger(3)) :: Nil) :: Nil => ok
        }.await(0, timeout)
      }

      "when is successful #2" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withDB(chandler1) { db: DB =>
            db(query2.collection).find(
              query2.body.head, Option.empty[BSONDocument]).
              cursor[BSONDocument]().
              collect[List](-1, Cursor.FailOnError[List[BSONDocument]]())
          }
        } aka ("query result") must beLike[List[BSONDocument]] {
          case ValueDocument(("d", BSONDouble(4.56D)) :: Nil) ::
            ValueDocument(("ef", BSONString("ghi")) :: Nil) :: Nil => ok
        }.await(0, timeout)
      }

      "using withQueryResult" >> {
        "for a single document" in {
          withDriver { implicit drv: AsyncDriver =>
            AcolyteDSL.withQueryResult(
              BSONDocument("res" → "ult", "n" → 3)) { con: MongoConnection =>
                con.database("anyDb").flatMap(_("anyCol").
                  find(query1.body.head, Option.empty[BSONDocument]).
                  cursor[BSONDocument]().
                  collect[List](-1, Cursor.FailOnError[List[BSONDocument]]()))
              }
          } aka "query result" must beLike[List[BSONDocument]] {
            case ValueDocument(("res", BSONString("ult")) ::
              ("n", BSONInteger(3)) :: Nil) :: Nil => ok
          }.await(0, timeout)
        }

        "for a many documents" in {
          withDriver { implicit drv: AsyncDriver =>
            AcolyteDSL.withQueryResult(
              List(BSONDocument("doc" → 1), BSONDocument("doc" → 2.3d))) { d =>
                AcolyteDSL.withCollection(d, "anyCol") {
                  _.find(query1.body.head, Option.empty[BSONDocument]).
                    cursor[BSONDocument]().
                    collect[List](-1, Cursor.FailOnError[List[BSONDocument]]())
                }
              }
          } aka "query result" must beLike[List[BSONDocument]] {
            case ValueDocument(("doc", BSONInteger(1)) :: Nil) ::
              ValueDocument(("doc", BSONDouble(2.3d)) :: Nil) :: Nil => ok
          }.await(0, timeout)
        }

        "for an explicit error" in {
          awaitRes(withDriver { implicit drv: AsyncDriver =>
            AcolyteDSL.withQueryResult("Error" → 7) { con: MongoConnection =>
              AcolyteDSL.withCollection(con, query1.collection) {
                _.find(query1.body.head, Option.empty[BSONDocument]).
                  cursor[BSONDocument]().
                  collect[List](-1, Cursor.FailOnError[List[BSONDocument]]())
              }
            }
          }) aka "query result" must beFailedTryWith[DatabaseException]("Error.*code = 7")
        }

        "when undefined" in {
          awaitRes(withDriver { implicit drv: AsyncDriver =>
            AcolyteDSL.withQueryResult(None) { con: MongoConnection =>
              AcolyteDSL.withCollection(con, query1.collection) {
                _.find(query1.body.head, Option.empty[BSONDocument]).
                  cursor[BSONDocument]().
                  collect[List](-1, Cursor.FailOnError[List[BSONDocument]]())
              }
            }
          }) aka "query result" must beFailedTryWith[DatabaseException]("No response:")
        }
      }

      "as error when query handler returns no query result" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withQueryHandler({
            case Request("acolyte.test3", SimpleBody(
              ("filter", BSONString("valC")) :: Nil)) => QueryResponse.empty
          }) { con: MongoConnection =>
            AcolyteDSL.withCollection(con, query3.collection) {
              _.find(query3.body.head, Option.empty[BSONDocument]).
                cursor[BSONDocument]().
                collect[List](-1, Cursor.FailOnError[List[BSONDocument]]())
            }
          }
        }.map(_.isEmpty) aka "query result" must beTrue.await(0, timeout)
      }

      "support query options" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withQueryHandler({
            case Request("acolyte.test3", RequestBody(
              List(("filter", BSONString("valC"))) :: List(
                ("$orderby", ValueDocument(List(("foo", BSONInteger(1))))),
                ("$readPreference", ValueDocument(
                  List(("mode", BSONString("primary")))))
                ) :: Nil)) =>
              QueryResponse(BSONDocument("lorem" → 1.2D))
          }) { con: MongoConnection =>
            AcolyteDSL.withCollection(con, query3.collection) {
              _.find(query3.body.head, Option.empty[BSONDocument]).
                sort(BSONDocument("foo" → 1)).cursor[BSONDocument]().
                collect[List](-1, Cursor.FailOnError[List[BSONDocument]]())
            }
          }
        }.map(_.size) aka "query result" must beTypedEqualTo(1).await(0, timeout)
      }

      "support findAndModify" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withQueryHandler({
            case FindAndModifyRequest("test3", List(("id", BSONInteger(1))),
              List(("title", BSONString("foo"))), opts) =>
              QueryResponse.findAndModify(BSONDocument(opts))
          }) { con: MongoConnection =>
            AcolyteDSL.withCollection(con, query3.collection) {
              _.findAndUpdate(
                BSONDocument("id" → 1),
                BSONDocument("title" → "foo")).map(_.value)
            }
          }
        } aka "query result" must beSome(BSONDocument(
          "bypassDocumentValidation" -> false,
          "upsert" → false, "new" → false)).await(0, timeout)

      }

      "support aggregate" in {
        val expected = List(
          BSONDocument("_id" -> "Foo", "maxAge" -> 20),
          BSONDocument("_id" -> "Bar", "maxAge" -> 54))

        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withQueryHandler({
            case AggregateRequest(
              "test", List(
                ValueDocument(("$match", ValueDocument(
                  ("age", ValueDocument(
                    ("$gt", BSONInteger(10)) :: Nil)) :: Nil)) :: Nil),
                ValueDocument(("$group", ValueDocument(
                  ("_id", BSONString("$lastName")) ::
                    ("maxAge", ValueDocument(
                      ("$max", BSONString("$age")) :: Nil
                      )) :: Nil
                  )) :: Nil),
                ValueDocument(("$sort", ValueDocument(
                  ("_id", BSONInteger(1)) :: Nil
                  )) :: Nil)
                ),
              List(
                ("explain", BSONBoolean(false)),
                ("allowDiskUse", BSONBoolean(false)),
                ("cursor", ValueDocument(
                  ("batchSize", BSONInteger(101)) :: Nil))
                )) =>
              QueryResponse(expected)
          }) { con: MongoConnection =>
            AcolyteDSL.withCollection(con, "test") {
              _.aggregateWith[BSONDocument]() { framework =>
                import framework._

                List(
                  Match(BSONDocument("age" -> BSONDocument(f"$$gt" -> 10))),
                  Group(BSONString(f"$$lastName"))(
                    "maxAge" -> MaxField("age")),
                  Sort(Ascending("_id")))
              }.collect[List]()
            }
          }
        } aka "aggregation result" must beTypedEqualTo(
          expected).awaitFor(timeout)

      }

      "as error when connection handler is empty" in {
        awaitRes(withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withCollection(AcolyteDSL.handle, query3.collection) {
            _.find(query3.body.head, Option.empty[BSONDocument]).
              cursor[BSONDocument]().
              collect[List](-1, Cursor.FailOnError[List[BSONDocument]]())
          }
        }) aka "query result" must beFailedTry.like {
          case err =>
            err.getMessage.indexOf("No response: ") must not(beEqualTo(-1))
        }
      }

      "as error when query handler is undefined" in {
        lazy val handler = AcolyteDSL.handleWrite(
          { (_: WriteOp, _: Request) => WriteResponse(1 /* one doc */ ) })

        awaitRes(withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withConnection(handler) { con: MongoConnection =>
            con.database("anyDb").flatMap(
              _(query3.collection).
                find(query3.body.head, Option.empty[BSONDocument]).
                cursor[BSONDocument]().
                collect[List](-1, Cursor.FailOnError[List[BSONDocument]]()))
          }
        }) aka "query result" must beFailedTry.like {
          case err =>
            err.getMessage.indexOf("No response: ") must not(beEqualTo(-1))
        }
      }
    }

    "return expected write result" >> {
      "when error is raised without code" in {
        awaitRes(withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withCollection(chandler1, write1._2.collection) {
            _.delete.one(write1._2.body.head)
          }
        }) aka "write result" must beFailedTry.like {
          case err => err.getMessage.indexOf("Error #2").
            aka("errmsg") must not(beEqualTo(-1)) and {
              err.getMessage.indexOf("code = -1").
                aka("code") must not(beEqualTo(-1))
            }
        }
      }

      "when successful" in {
        withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withDB(chandler1) {
            _(write2._2.collection).insert.one(write2._2.body.head)
          }
        } aka "result" must beLike[WriteResult] {
          case result => result.n aka "updated" must_=== 0
        }.await(0, timeout)
      }

      "as error when write handler returns no write result" in {
        awaitRes(withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withConnection(chandler1) { con =>
            val db = con.database("anyDb")
            val col = db.map(_(write3._2.collection))

            col.flatMap(_.update.one(
              BSONDocument("name" → "x"),
              write3._2.body.head))
          }
        }) aka "result" must beFailedTry.like {
          case err =>
            err.getMessage.indexOf("No response: ") must not(beEqualTo(-1))
        }
      }

      "as error when connection handler is empty" in {
        awaitRes(withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withWriteHandler({ (_: WriteOp, _: Request) =>
            WriteResponse.undefined
          }) { d =>
            AcolyteDSL.withCollection(d, query3.collection) {
              _.update.one(BSONDocument("name" → "x"), write3._2.body.head)
            }
          }
        }) aka "result" must beFailedTry.like {
          case err =>
            err.getMessage.indexOf("No response: ") must not(beEqualTo(-1))
        }
      }

      "as error when write handler is undefined" in {
        awaitRes(withDriver { implicit drv: AsyncDriver =>
          AcolyteDSL.withQueryResult(BSONDocument("prop" → "A")) { con =>
            AcolyteDSL.withCollection(con, write3._2.collection) {
              _.update.one(BSONDocument("name" → "x"), write3._2.body.head)
            }
          }
        }) aka "result" must beFailedTry.like {
          case err =>
            err.getMessage.indexOf("No response: ") must not(beEqualTo(-1))
        }
      }

      "using withWriteResult" >> {
        "for success count" in {
          withDriver { implicit drv: AsyncDriver =>
            AcolyteDSL.withWriteResult(2 → true) {
              AcolyteDSL.withConnection(_) { con: MongoConnection =>
                val db = con.database("anyDb")
                val col = db.map(_(write1._2.collection))

                col.flatMap(_.delete.one(write1._2.body.head))
              }
            }
          } aka "write result" must beLike[WriteResult] {
            case lastError => lastError.n aka "updated" must_=== 2
          }.await(0, timeout)
        }

        "for explicit error" in {
          awaitRes(withDriver { implicit drv: AsyncDriver =>
            AcolyteDSL.withWriteResult("Write err" → 9) { con =>
              AcolyteDSL.withCollection(con, write2._2.collection) {
                _.insert.one(write2._2.body.head)
              }
            }
          }) aka "write result" must beFailedTry.like {
            case err => err.getMessage.indexOf("Write err").
              aka("errmsg") must not(beEqualTo(-1)) and {
                err.getMessage.indexOf("code = 9") must not(beEqualTo(-1))
              }
          }
        }

        "when undefined" in {
          awaitRes(withDriver { implicit drv: AsyncDriver =>
            AcolyteDSL.withWriteResult(None) { driver =>
              AcolyteDSL.withCollection(driver, write3._2.collection) {
                _.update.one(BSONDocument(), write3._2.body.head)
              }
            }
          }) aka "write result" must beFailedTry.like {
            case err =>
              err.getMessage.indexOf("No response: ") must not(beEqualTo(-1))
          }
        }
      }

      "using withWriteHandler" >> {
        "for insert" in {
          awaitRes(withDriver { implicit drv: AsyncDriver =>
            AcolyteDSL.withWriteHandler({
              case InsertRequest("acolyte.col1", ("a", BSONString("val")) ::
                ("b", BSONInteger(2)) :: _) =>
                WriteResponse.successful(1, false)
            }) { con: MongoConnection =>
              AcolyteDSL.withCollection(con, "col1") {
                _.insert.one(BSONDocument("a" → "val", "b" → 2))
              }
            }
          }) aka "write result" must beSuccessfulTry[WriteResult]
        }

        "for bulk-insert" in {
          withDriver { implicit drv: AsyncDriver =>
            AcolyteDSL.withWriteHandler({
              case (InsertOp, Request("foo.bar", RequestBody(List(
                List(("foo", BSONInteger(1))),
                List(("bar", BSONInteger(2))),
                List(("lorem", BSONInteger(3)))
                )))) => WriteResponse.successful(count = 3)
            }) { con: MongoConnection =>
              for {
                db ← con.database("foo")
                res ← db.collection("bar").insert(ordered = true).many(List(
                  BSONDocument("foo" → 1),
                  BSONDocument("bar" → 2),
                  BSONDocument("lorem" → 3)))
              } yield res.n
            }
          } must beTypedEqualTo(3).awaitFor(timeout)
        }

        "for update" in {
          awaitRes(withDriver { implicit drv: AsyncDriver =>
            AcolyteDSL.withWriteHandler({
              case UpdateRequest("acolyte.col2",
                ("sel", BSONString("hector")) :: Nil,
                ("filter", BSONString("valC")) :: Nil,
                _ /*upsert*/ , _ /*multi*/ ) =>
                WriteResponse(1)

              case UpdateRequest("acolyte.col2", q, u, upsert, multi) =>
                sys.error(s"Unexpected: $q, $u, $upsert, $multi")

            }) { con: MongoConnection =>
              AcolyteDSL.withCollection(con, "col2") {
                _.update.one(
                  BSONDocument("sel" → "hector"), write3._2.body.head)
              }
            }
          }) aka "write result" must beSuccessfulTry[WriteResult]
        }

        "for bulk-update" in {
          withDriver { implicit drv: AsyncDriver =>
            AcolyteDSL.withWriteHandler({
              case (UpdateOp, Request("foo.bar", RequestBody(List(
                List(
                  ("q", ValueDocument(("foo", BSONInteger(1)) :: _)),
                  ("u", ValueDocument(("bar", BSONInteger(2)) :: _)),
                  ("upsert", BSONBoolean(true)),
                  ("multi", BSONBoolean(false))),
                List(
                  ("q", ValueDocument(("foo", BSONInteger(2)) :: _)),
                  ("u", ValueDocument(("lorem", BSONInteger(3)) :: _)),
                  ("upsert", BSONBoolean(false)),
                  ("multi", BSONBoolean(true))))))) => {
                WriteResponse.successful(count = 3)
              }
            }) { con: MongoConnection =>
              for {
                db ← con.database("foo")
                res ← {
                  val coll = db.collection("bar")
                  val upd = coll.update(ordered = false)

                  Future.sequence(Seq(
                    upd.element(
                      q = BSONDocument("foo" → 1),
                      u = BSONDocument("bar" → 2),
                      upsert = true, multi = false),
                    upd.element(
                      q = BSONDocument("foo" → 2),
                      u = BSONDocument("lorem" → 3),
                      upsert = false, multi = true))).flatMap(upd.many(_))
                }
              } yield res.n
            }
          } must beTypedEqualTo(3).await(0, timeout)
        }

        "for delete" in {
          withDriver { implicit drv: AsyncDriver =>
            AcolyteDSL.withWriteHandler({
              case DeleteRequest("acolyte.col3",
                ("a", BSONString("val")) :: Nil) =>
                WriteResponse.successful(2, true)

            }) { con: MongoConnection =>
              AcolyteDSL.withCollection(con, "col3") {
                _.delete.one(BSONDocument("a" → "val")).map(_ => {})
              }
            }
          } aka "write result" must beTypedEqualTo({}).await(0, timeout)
        }
      }
    }
  }

  // ---

  def awaitRes[T](f: Future[T], tmout: Duration = Duration(5, "seconds")): Try[T] = Try[T](Await.result(f, tmout))

  def afterAll() = driver.close(Duration(5, "seconds"))

  import org.specs2.matcher.{ Expectable, Matcher }

  // Workaround for https://github.com/etorreborre/specs2/pull/836
  private def beFailedTryWith[T <: Throwable: scala.reflect.ClassTag](message: String): Matcher[Try[Any]] = new Matcher[Try[Any]] {
    val Cause = implicitly[scala.reflect.ClassTag[T]]
    val re = ("(.|\\s)*" + message + "(.|\\s)*").r

    def apply[S <: Try[Any]](e: Expectable[S]) = result({
      e.value match {
        case scala.util.Failure(Cause(t)) =>
          re.findFirstMatchIn(t.getMessage).nonEmpty

        case _ => false
      }
    }, "fails with expected throwable",
      "is not failing with excepted throwable", e)
  }
}
