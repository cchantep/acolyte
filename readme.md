# Acolyte/ReactiveMongo tutorial

This tutorial is about how to use [Acolyte](http://acolyte.eu.org/reactive-mongo.html) and [specs2](http://etorreborre.github.io/specs2/) to test project using [ReactiveMongo](http://reactivemongo.org/).

[![Build Status](https://secure.travis-ci.org/cchantep/acolyte.png?branch=reactivemongo-tutorial)](http://travis-ci.org/cchantep/acolyte)

## Requirements

In order to test this tutorial, you will need:

- [GIT client](http://git-scm.com/downloads), to get sources,
- Java 1.6+,
- [SBT](http://www.scala-sbt.org/), to build it.

## For the impatient: go to sources

If you want to look at tutorial sources -- right now --, you can clone this project:

```shell
# git clone git@github.com:cchantep/acolyte.git --branch reactivemongo-tutorial
```

Then to run tests using Acolyte against provided Anorm use cases:

```shell
# cd acolyte
# sbt test
```

## Tutorial scenario

This documentation will describe an example where Acolyte is useful.

Consider you want to manage user information defined in Mongo as following.

```json
{
  'name': 'a_username', 
  'password': 'a_password',
  'description': 'Optional descriptive text',
  'roles': [ 'role1', 'role2' ]
}
```

In order to test code using ReactiveMongo to access this collection, BSON fixtures thereafter can be used.

```json
[
  { 'name':'administrator', 'password':'pass1', 'roles':['admin'] },
  { 'name':'user2', 'password':'pass2', 'description': 'User #2',
    'roles':['editor','reviewer'] },
  { 'name':'user3', 'password':'pass3', 'description': 'Third user' }
]
```

Using Acolyte, it can be encoded so that tests/specifications can be executed without database or complex fixtures management:

```scala
import reactivemongo.bson.{ BSONArray, BSONDocument }
import acolyte.reactivemongo.QueryResponse

val userFixtures = QueryResponse.successful(
  BSONDocument("name" -> "administrator", "password" -> "pass1", 
    "roles" -> BSONArray("admin")),
  BSONDocument("name" -> "user2", "password" -> "pass2",
    "description" -> "User #2", "roles" -> BSONArray("editor", "reviewer")),
  BSONDocument("name" -> "user3", "password" -> "pass3",
    "description" -> "Third user"))
```

### Checking read access

If we write a function [all](./src/main/scala/Persistence.scala#L35) (`Persistence.all`) which returns information for all users, executable specifications can be written ([PersistenceSpec.scala](./src/test/scala/PersistenceSpec.scala#L20)).

```scala
"List all user information" should {
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
}
```

Edge cases, such as a missing `name` property can also be tested, to be sure persistence code is properly handling it.

```scala
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
```

### Checking write access

Cases where Mongo layer uses both read and write operations can also be validated with this approach.

Consider a function [save](./src/main/scala/Persistence.scala#L43) (`Persistence.save(UserInfo)`) implemented as following.

```scala
val selector = BSONDocument("name" -> user.name)
for {
  exists ← c.find(selector).cursor[BSONDocument].collect[List]()
  _ ← exists.headOption.fold[Future[LastError]](
    /* not already exists so create: */ c.save(user))(
    /* already exists so update: */ _ ⇒ c.update(selector, user))

} yield UserInfo(user.name, user.description)
```

Then fixtures to validate `save` can be [described with Acolyte](./src/main/scala/PersistenceSpec.scala#L74).

```scala
import acolyte.reactivemongo.{
  InsertOp, QueryResponse, Request, SimpleBody, UpdateOp, WriteOp, WriteResponse
}

val withFixtures = handleQuery { r: Request ⇒
  r match {
    // If query on "users" collection (in acolyte DB) 
    // with selector "name" -> "user2" returns some 
    // { "name": "user2", "password": "pass2", ... },
    // otherwise returns no result (QueryResponse.empty)

    case Request("acolyte.users",
      SimpleBody(("name", BSONString("user2")) :: Nil)) ⇒
        QueryResponse(BSONDocument("name" -> "user2", "password" -> "pass2",
          "description" -> "User #2",
          "roles" -> BSONArray("editor", "reviewer")))

    case q ⇒ QueryResponse.empty // No matching result
  }
} withWriteHandler { (op: WriteOp, r: Request) ⇒
  (op, r) match {
    case (InsertOp, Request("acolyte.users",
     SimpleBody(("name", BSONString("administrator")) :: _))) ⇒
       // if insert on "users" collection with a document starting with
       // "name" -> "administrator", then...
       WriteResponse( /* update count: */ 1, /* updated existing: */ false)

    case (UpdateOp, Request("acolyte.users",
      SimpleBody(("name", BSONString("user2")) :: _))) ⇒
        // if update on "users" collection with a document starting with
        // "name" -> "user2", then ...
        WriteResponse( /* update count: */ 1, /* updated existing: */ true)

    case _ ⇒ WriteResponse.undefined
  }
}
```

Thus isolated tests can be written for `save` function.

```scala
"Saving user" should {
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
}
```

Such write function can also be validated against erroneous cases.

```scala
// In Acolyte Mongo fixtures
  /*C*/ case (InsertOp, Request("acolyte.users",
    SimpleBody(("name", BSONString("user3")) :: _))) ⇒
      // if update on "users" collection with a document starting with
      // "name" -> "user3", then ...
      WriteResponse.failed("Unexpected error", 123 /* error code */ )

// ... then specification can be
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
```

### Other Mongo operations

If we want to count user matching a specified role, it can be implemented using `Count` command.


```scala
import reactivemongo.core.commands.Count

def countRole(role: String)(implicit db: DB, x: ExecutionContext): Future[Int] =
  db.command(Count("users", Some(BSONDocument(
    "roles" -> BSONDocument("$in" -> BSONArray(role))))))
  // db.users.count({ "roles": { "$in": [role] } })
```

Fixtures to unit test such function can be encoded as following.

```scala
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
```

Then executable specifications can be written.

```scala
"Counting role members" in {
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
    withFlatDB(withFixtures) { implicit db =>
      Persistence.countRole("test")
    } aka "count not existing role" must beEqualTo(0).await(5)
  }
}
```

*That's all Folks*, no more complication to write Mongo tests with Acolyte. Feel free to give feedback about this tutorial.