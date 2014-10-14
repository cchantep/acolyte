# Acolyte/ReactiveMongo tutorial

This tutorial is about how to use [Acolyte](http://cchantep.github.io/acolyte/) and [specs2](http://etorreborre.github.io/specs2/) to test project using [ReactiveMongo](http://reactivemongo.org/).

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

From there, executable form specifications can be written ([PersistenceSpec.scala](./src/test/scala/PersistenceSpec.scala)):

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

*That's all Folks*, no more complication to write persistence tests with Acolyte. Feel free to give feedback about this tutorial.