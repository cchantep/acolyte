import scala.util.{ Failure, Try }

import scala.concurrent.{ Await, Future }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

import reactivemongo.bson.{ BSONArray, BSONDocument, BSONString }
import reactivemongo.core.commands.LastError

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
import acolyte.reactivemongo.AcolyteDSL.{
  handleQuery,
  withFlatDB,
  withFlatQueryResult,
  withFlatCollection,
  withFlatConnection
}

object PersistenceSpec extends org.specs2.mutable.Specification {
  "Persistence" title

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
      )).await(5)
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

        case _ ⇒ WriteResponse.undefined
      }
    }

    "create 'administrator' (case A)" in {
      withFlatCollection(withFixtures, "users") { implicit col ⇒
        Persistence.save(User("administrator", "pass1", None, List("admin")))
      } aka "save administrator" must beEqualTo(UserInfo("administrator")).
        await(5)
    }

    "update 'user2' (case B)" in {
      withFlatCollection(withFixtures, "users") { implicit col ⇒
        Persistence.save(User("user2", "pass2", Some("User #2"),
          roles = List("editor", "reviewer")))
      } aka "save user2" must beEqualTo(UserInfo("user2", Some("User #2"))).
        await(5)
    }

    "update 'user3' (case C)" in {
      awaitRes(withFlatCollection(withFixtures, "users") { implicit col ⇒
        Persistence.save(User("user3", "pass3", Some("Third user"), Nil))
      }) aka "save user3" must beLike {
        case Failure(LastError(_, msg, code, _, _, _, _)) ⇒
          msg aka "message" must beSome.which(
            _ must contain("Unexpected error")) and (
              code aka "code" must beSome(123))
      }
    }
  }

  "Counting role members" in {
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
      }
    }

    "find one administrator" in {
      withFlatDB(withFixtures) { implicit db ⇒
        Persistence.countRole("administrator")
      } aka "count administrators" must beEqualTo(1).await(5)
    }

    "find two users" in {
      withFlatDB(withFixtures) { implicit db ⇒
        Persistence.countRole("user")
      } aka "count users" must beEqualTo(2).await(5)
    }

    "not find not existing role" in {
      withFlatDB(withFixtures) { implicit db ⇒
        Persistence.countRole("test")
      } aka "count not existing role" must beEqualTo(0).await(5)
    }
  }

  def awaitRes[T](f: Future[T], tmout: Duration = Duration(5, "seconds")): Try[T] = Try[T](Await.result(f, tmout))
}
