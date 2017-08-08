import scala.util.{ Failure, Try }

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration

import reactivemongo.bson.{ BSONArray, BSONDocument, BSONString }
import reactivemongo.api.commands.CommandError

import org.specs2.mutable.Specification
import org.specs2.concurrent.ExecutionEnv

import acolyte.reactivemongo.{
  CountRequest,
  QueryHandler,
  QueryResponse,
  InClause,
  InsertOp,
  Request,
  SimpleBody,
  UpdateOp,
  WriteOp,
  WriteResponse
}
import acolyte.reactivemongo.AcolyteDSL, AcolyteDSL.{
  handleQuery,
  withFlatDB,
  withFlatQueryResult,
  withFlatCollection,
  withFlatConnection
}

/** Manage a single Mongo driver for all (isolated) Acolyte handlers. */
sealed trait AcolyteDriver
    extends org.specs2.specification.AfterAll { specs: Specification ⇒

  implicit val driver = AcolyteDSL.driver

  def afterAll() = driver.close()
}

/** Persitence executable specification (tests). */
class PersistenceSpec(
  implicit ee: ExecutionEnv) extends Specification with AcolyteDriver {

  "Persistence" title

  val timeout = Duration(5, "seconds")

  "List all user information" should {
    /* Mongo fixtures:
     [
       { 'name':'administrator', 'password':'pass1', 'roles':['admin'] },
       { 'name':'user2', 'password':'pass2', 'description': 'User #2',
         'roles':['editor','reviewer'] },
       { 'name':'user3', 'password':'pass3', 'description': 'Third user' }
     ]
     */
    val userFixtures = QueryResponse.successful(
      BSONDocument("name" -> "administrator", "password" -> "pass1",
        "roles" -> BSONArray("admin")),
      BSONDocument("name" -> "user2", "password" -> "pass2",
        "description" -> "User #2", "roles" -> BSONArray("editor", "reviewer")),
      BSONDocument("name" -> "user3", "password" -> "pass3",
        "description" -> "Third user"))

    "be successfully found" in {
      val withFixtures = handleQuery { r: Request ⇒ userFixtures }

      withFlatCollection(withFixtures, "users") { implicit col ⇒
        Persistence.all
      } aka "user information" must contain(exactly(
        // what should be selected and extracted
        UserInfo("administrator"),
        UserInfo("user2", Some("User #2")),
        UserInfo("user3", Some("Third user"))
      )).await(0, timeout)
    }

    "fail on missing 'name' property" in {
      // Check error case is handled properly by persistence code
      awaitRes(
        withFlatQueryResult(BSONDocument("description" -> "no name")) { drv ⇒
          withFlatConnection(drv) { con ⇒
            val db = con("anyDbName")
            implicit val col = db("users")
            Persistence.all
          }
        }) aka "user information" must beFailedTry.
        withThrowable[RuntimeException]("Missing 'name' property")
    }
  }

  "Saving user" should {
    val withFixtures = handleQuery { r: Request ⇒
      r match {
        // If query on "users" collection (in acolyte DB) 
        // with selector "name" -> "user2" returns some 
        // { "name": "user2", "password": "pass2", ... },
        // otherwise returns no result (QueryResponse.empty)

        /*B*/ case Request("acolyte.users",
          SimpleBody(("name", BSONString("user2")) :: Nil)) ⇒
          QueryResponse(BSONDocument("name" -> "user2", "password" -> "pass2",
            "description" -> "User #2",
            "roles" -> BSONArray("editor", "reviewer")))

        case q ⇒ QueryResponse.empty // No matching result
      }
    } withWriteHandler { (op: WriteOp, r: Request) ⇒
      (op, r) match {
        /*A*/ case (InsertOp, Request("acolyte.users",
          SimpleBody(("name", BSONString("administrator")) :: _))) ⇒
          // if insert on "users" collection with a document starting with
          // "name" -> "administrator", then...
          WriteResponse( /* update count: */ 1, /* updated existing: */ false)

        /*B*/ case (UpdateOp, Request("acolyte.users",
          SimpleBody(("name", BSONString("user2")) :: _))) ⇒
          // if update on "users" collection with a document starting with
          // "name" -> "user2", then ...
          WriteResponse( /* update count: */ 1, /* updated existing: */ true)

        /*C*/ case (InsertOp, Request("acolyte.users",
          SimpleBody(("name", BSONString("user3")) :: _))) ⇒
          // if update on "users" collection with a document starting with
          // "name" -> "user3", then ...
          WriteResponse.failed("Unexpected error", 123 /* error code */ )

        case q ⇒ println(s"q = $q"); WriteResponse.undefined
      }
    }

    "create 'administrator' (case A)" in {
      withFlatCollection(withFixtures, "users") { implicit col ⇒
        Persistence.save(User("administrator", "pass1", None, List("admin")))
      } aka "save administrator" must beEqualTo(UserInfo("administrator")).
        await(0, timeout)
    }

    "update 'user2' (case B)" in {
      withFlatCollection(withFixtures, "users") { implicit col ⇒
        Persistence.save(User("user2", "pass2", Some("User #2"),
          roles = List("editor", "reviewer")))
      } aka "save user2" must beEqualTo(UserInfo("user2", Some("User #2"))).
        await(0, timeout)
    }

    "update 'user3' (case C)" in {
      awaitRes(withFlatCollection(withFixtures, "users") { implicit col ⇒
        Persistence.save(User("user3", "pass3", Some("Third user"), Nil))
      }) aka "save user3" must beLike {
        case Failure(err @ CommandError.Code(123)) ⇒ 
          err.getMessage.indexOf("Unexpected error") must not(beEqualTo(-1))
      }
    }
  }

  "Counting role members" >> {
    val withFixtures = handleQuery { r: Request ⇒
      r match {
        case CountRequest(_,
          ("roles", InClause(BSONString("administrator") :: Nil)) :: Nil) ⇒
          // if count on "users" with role "administrator", return 1
          QueryResponse.count(1)

        case CountRequest(_,
          ("roles", InClause(BSONString("user") :: Nil)) :: Nil) ⇒
          // if count on "users" with role "user", return 2
          QueryResponse.count(2)

        case CountRequest(_, _) ⇒ QueryResponse.count(0) // otherwise count = 0

        case Request(_, SimpleBody(q)) => sys.error(s"q = $q")
      }
    }

    "find one administrator" in {
      withFlatCollection(withFixtures, "roles") { implicit db ⇒
        Persistence.countRole("administrator")
      } aka "count administrators" must beEqualTo(1).await(0, timeout)
    }

    "find two users" in {
      withFlatCollection(withFixtures, "roles") { implicit db ⇒
        Persistence.countRole("user")
      } aka "count users" must beEqualTo(2).await(0, timeout)
    }

    "not find not existing role" in {
      withFlatCollection(withFixtures, "roles") { implicit db ⇒
        Persistence.countRole("test")
      } aka "count not existing role" must beEqualTo(0).await(0, timeout)
    }
  }

  def awaitRes[T](f: Future[T], tmout: Duration = timeout): Try[T] = Try[T](Await.result(f, tmout))
}

