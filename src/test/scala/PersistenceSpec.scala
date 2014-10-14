import scala.util.Try

import scala.concurrent.{ Await, Future }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

import reactivemongo.bson.{ BSONDocument, BSONArray }

import acolyte.reactivemongo.{ QueryHandler, QueryResponse, Request }
import acolyte.reactivemongo.AcolyteDSL.{
  handleQuery,
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
      val withFixtures = handleQuery(
        QueryHandler { r: Request ⇒ userFixtures })

      withFlatCollection(withFixtures, "users") { implicit col ⇒
        Persistence.all
      } aka "user information" must contain(exactly(
        // what should be selected and extracted
        UserInfo("administrator"),
        UserInfo("user2", Some("User #2")),
        UserInfo("user3", Some("Third user"))
      )).await
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

  def awaitRes[T](f: Future[T], tmout: Duration = Duration(5, "seconds")): Try[T] = Try[T](Await.result(f, tmout))
}
