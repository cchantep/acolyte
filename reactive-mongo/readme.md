# Acolyte for ReactiveMongo

Acolyte API for ReactiveMongo.

## Motivation

Wherever in your code you use ReactiveMongo driver, you can pass Acolyte Mongo driver instead tests.

Then any connection created will be managed by your Acolyte (query & writer) handlers.

## Usage

- 1. Configure connection handler according expected behaviour: which response to which query, which result for which write request.
- 2. Allow the persistence code to be given a `MongoDriver` according environment (e.g. test, dev, ..., prod).
- 3. Provide this testing driver to persistence code during validation.

```scala
// 1. On one side configure the Mongo handler
import acolyte.reactivemongo.AcolyteDSL

val connectionHandler = AcolyteDSL handleQuery {
    // returns result according executed query
  } withWriteHandler {
    // returns result according executed write operation
  }

// 2. In Mongo persistence code, allowing (e.g. cake pattern) 
// to provide driver according environment.
import reactivemongo.api.MongoDriver

trait MongoPersistence {
  def driver: MongoDriver

  def foo = /* Function using driver, whatever is the way it's provided */
}

object ProdPersistence extends MongoPersistence {
  def driver = /* e.g. Resolve driver according configuration file */
}

// 3. Finally in unit tests
import scala.concurrent.Future
import acolyte.reactivemongo.AcolyteDSL

def isOk: Future[Boolean] = AcolyteDSL.withFlatDriver { d =>
  val persistenceWithTestingDriver = new MongoPersistence {
    val driver: MongoDriver = d // provide testing driver
  }

  persistenceWithTestingDriver.foo
}
```

> When result Future is complete, Mongo resources initialized by Acolyte are released (driver and connections).

For persistence code expecting driver as parameter, resolving testing driver is straightforward.

```scala
import reactivemongo.api.MongoConnection
import acolyte.reactivemongo.AcolyteDSL.withConnection

val res: Future[String] = withConnection(yourConnectionHandler) { c =>
  val con: MongoConnection = c // configured with `yourConnectionHandler`

  val s: String = yourFunctionUsingMongo(driver)
  // ... dispatch query and write request as you want using pattern matching

  s
}
```

As in previous example, main API object is [AcolyteDSL](https://github.com/cchantep/acolyte/blob/master/reactive-mongo/src/main/scala/acolyte/reactivemongo/AcolyteDSL.scala).

Dependency can be added to SBT project with `"org.eu.acolyte" %% "reactive-mongo" % "1.0.37"`, or in a Maven one as following:

```xml
<dependency>
  <groupId>org.eu.acolyte</groupId>
  <artifactId>reactive-mongo_2.11</artifactId>
  <version>1.0.37-j7p</version>
</dependency>
```

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.eu.acolyte/reactive-mongo_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.eu.acolyte/reactive-mongo_2.11/)

### Get started

- [API documentation](http://http://acolyte.eu.org/reactivemongo-scaladoc)
- [Tutorial](https://github.com/cchantep/acolyte/tree/reactivemongo-tutorial)

### Setup driver

Driver behaviour is configured using connection handlers, themselves based on query and write handlers, managing respectively Mongo queries or write operations, and returning appropriate result.

You can start looking at empty/no-op connection handler. With driver configured in this way, there is no query or write handler. So as no response is provided whatever is the command performed, it will raise explicit error `No response: ...` for every request.

```scala
import reactivemongo.api.MongoConnection
import acolyte.reactivemongo.AcolyteDSL

AcolyteDSL.withConnection(AcolyteDSL handle/*ConnectionHandler.empty*/) { c =>
  val noOpCon: MongoConnection = c
}
```

Acolyte provides several ways to initialize Mongo resources (driver, connection, DB and collection) your code could expect.

- `withDriver` and `withFlatDriver`,
- `withConnection` and `withFlatConnection`,
- `withDB` and `withFlatDB`,
- `withCollection` and `withFlatCollection`,
- `withQueryHandler` and `withFlatQueryHandler`,
- `withQueryResult` and `withFlatQueryResult`,
- `withWriteHandler` and `withFlatWriteHandler`,
- `withWriteResult` and `withFlatWriteResult`.

> Naming convention is `withX(...) { a => b }` to use with your Mongo function which doesn't return `Future` result, and `withFlatX(...) { a => b }` when your Mongo function does return `Future` (so that result as `Future[T]` is flatten when returned, not having `Future[Future[YourReturnType]]` finally).

```scala
import reactivemongo.api.{ MongoConnection, MongoDriver }
import reactivemongo.bson.BSONDocument
import acolyte.reactivemongo.{ 
  AcolyteDSL, QueryResponse, PreparedResponse, Request, WriteOp 
}

// Simple cases
AcolyteDSL.withDriver { d =>
  yourFunctionWorkingWithDriver(d)
}

AcolyteDSL.withConnection(yourHandler) { c =>
  yourFunctionWorkingWithConnection(c)
}

AcolyteDSL.withDB(yourHandler) { db =>
  yourFunctionWorkingWithDB(db)
}

AcolyteDSL.withCollection(yourHandler, "colName") { col =>
  yourFunctionWorkingWithCol(col)
}

AcolyteDSL.withQueryHandler({ req: Request => 
  val resp: PreparedResponse = QueryResponse.empty // empty doc list
  resp
}) { d => yourFunctionWorkingWithDriver(d) }

AcolyteDSL.withQueryResult(queryResultForAll) { d =>
  yourFunctionWorkingWithDriver(d)
}

AcolyteDSL.withWriteHandler({ cmd: (WriteOp, Request) => aResp }) { d =>
  yourFunctionWorkingWithDriver(d)
}

AcolyteDSL.withWriteResult(writeResultForAll) { d =>
  yourFunctionWorkingWithDriver(d)
}

// More complexe case
AcolyteDSL.withFlatDriver(yourHandler) { d => // expect a Future
  AcolyteDSL.withConnection(d) { c1 =>
    if (yourFunction1WorkingWithConnection(c1))
      yourFunction2WorkingWithConnection(c1)
  }

  AcolyteDSL.withFlatConnection(d) { c2 => // expect a Future
    yourFunction3WorkingWithConnection(c2) // return a Future
  }

  AcolyteDSL.withFlatConnection(d) { c3 => // expect a Future
    AcolyteDSL.withFlatDB(c3) { db => // expect a Future
      AcolyteDSL.withFlatCollection(db, "colName") { // expect Future
        yourFunction4WorkingWithDB(c3) // return a Future
      }
    }
  }
}
```

Many other combinations are possible: see complete [test cases](https://github.com/cchantep/acolyte/blob/master/reactive-mongo/src/test/scala/acolyte/reactivemongo/DriverSpec.scala#L27).

### Configure connection behaviour

At this point we can focus on playing handlers. To handle Mongo query and to return the kind of result your code should work with, you can do as following.

```scala
import reactivemongo.api.MongoConnection
import acolyte.reactivemongo.{ AcolyteDSL, Request }

AcolyteDSL.withDriver { implicit driver =>
  AcolyteDSL.withConnection(
    AcolyteDSL handleQuery { req: Request => aResponse }) { c =>
    val readOnlyCon: MongoConnection = c
    // work with configured driver
  }
}

// Then when Mongo code is given this driver instead of production one ...
// (see DI or cake pattern) and resolve a BSON collection `col` by this way:

col.find(BSONDocument("anyQuery" -> 1).cursor[BSONDocument].toList().
  onComplete {
    case Success(res) => ??? // In case of response given by provided handler
    case Failure(err) => ??? // "No response: " if case not handled
  }
```

In the same way, write operations can be responded with appropriate result.

```scala
import reactivemongo.api.MongoConnection
import acolyte.reactivemongo.{ AcolyteDSL, Request, WriteOp }

AcolyteDSL.withDriver { implicit driver =>
  AcolyteDSL.withConnection(
    AcolyteDSL handleWrite { (op: WriteOp, req: Request) => aResponse }) { c =>
    val writeOnlyDriver: MongoConnection = c
    // work with configured driver
  }
}

// Then when Mongo code is given this driver instead of production one ...
// (see DI or cake pattern) and resolve a BSON collection `col` by this way:

col.insert(BSONDocument("prop" -> "value")).onComplete {
  case Success(res) => ??? // In case or response given by provided handler
  case Failure(err) => ??? // "No response: " if case not handled
}
```

Obviously connection handler can manage both queries and write operations:

```scala
import acolyte.reactivemongo.{ AcolyteDSL, Request, WriteOp }

val completeHandler = 
  AcolyteDSL handleQuery { req: Request => 
    // First define query handling
    aQueryResponse
  } withWriteHandler { (op: WriteOp, req: Request) =>
    // Then define write handling
    aWriteResponse
  }

AcolyteDSL.withDriver(completeHandler) { d =>
  // work with configured driver
}
```

### Request patterns

Pattern matching can be used in handler to dispatch result accordingly.

```scala
import reactivemongo.bson.{ BSONInteger, BSONString }

import acolyte.reactivemongo.{
  CollectionName, QueryHandler, Request, Property, SimpleBody, &
}

val queryHandler = QueryHandler { queryRequest =>
  queryRequest match {
    case Request("a-mongo-db.a-col-name", _) => 
      // Any request on collection "a-mongo-db.a-col-name"
      resultA

    case Request(colNameOfAnyOther, _)  => resultB // Any request

    case Request(colName, SimpleBody((k1, v1) :: (k2, v2) :: Nil)) => 
      // Any request with exactly 2 BSON properties
      resultC

    case Request("db.col", SimpleBody(("email", BSONString(v)) :: _)) =>
      // Request on db.col starting with email string property
      resultD

    case Request("db.col", SimpleBody(("name", BSONString("eman")) :: _)) =>
      // Request on db.col starting with an "name" string property,
      // whose value is "eman"
      resultE

    case Request(_, SimpleBody(("age": ValueDocument(
      ("$gt", BSONInteger(minAge)) :: Nil)))) =>
      // Request on any collection, with an "age" document as property,
      // itself with exactly one integer "$gt" property
      // e.g. `{ 'age': { '$gt', 10 } }`
      resultF

    case Request("db.col", SimpleBody(~(Property("email"), BSONString(e)))) =>
      // Request on db.col with an "email" string property,
      // anywhere in properties (possible with others which are ignored there)
      resultG

    case Request("db.col", SimpleBody(
      ~(Property("name"), BSONString("eman")))) =>
      // Request on db.col with an "name" string property with "eman" as value,
      // anywhere in properties (possibly with others which are ignored there).
      resultH

    case Request(colName, SimpleBody(
      ~(Property("age"), BSONInteger(age)) &
      ~(Property("email"), BSONString(v)))) =>
      // Request on any collection, with an "age" integer property
      // and an "email" string property, possibly not in this order.
      resultI

    case Request(colName, SimpleBody(
      ~(Property("age"), ValueDocument(
        ~(Property("$gt"), BSONInteger(minAge)))) &
      ~(Property("email"), BSONString(email)))) =>
      // Request on any collection, with an "age" property with itself
      // a operator property "$gt" having an integer value, and an "email" 
      // property (at the same level as age), without order constraint.
      resultJ

    case CountRequest(colName, ("email", "em@il.net") :: Nil) =>
      // Matching on count query
      resultK

    case CountRequest(_, ("property", InClause(
      BSONString("A") :: BSONString("B") :: Nil)) :: Nil) =>
      resultL // matches count with selector on 'property' using $in operator

    case Request("col1", SimpleBody(("$in", ValueList(bsonA, bsonB)) :: Nil)) =>
      // Matching BSONArray using with $in operator
      resultM

    case Request(_, RequestBody(List(("sel", BSONString("hector"))) ::
      List(("updated", BSONString("property"))) :: Nil)) ⇒ 
      // Matches a request with multiple document in body 
      // (e.g. update with selector)
      resultN

  }
}
```

Acolyte also provides extractors for inner clauses.

- `ValueList(List[(String, BSONValue)](_))` to match with `[...]`.
- `InClause(List[(String, BSONValue)](_))` to match with `{ '$in': [...] }`.
- `NotInClause(List[(String, BSONValue)](_))` to match with `{ '$nin': [...] }`.

Pattern matching using rich syntax `~(..., ...)` requires [scalac plugin](../scalac-plugin/readme.html).
Without this plugin, such parameterized extractor need to be declared as stable identifier before `match` block:

```scala
// With scalac plugin
request match {
  case Request("db.col", SimpleBody(
    ~(Property("email"), BSONString(e)))) => result
  // ...
}

// Without
val EmailXtr = Property("email")
// has declare email extractor before, as stable identifier

request match {
  case Request("db.col", SimpleBody(~(EmailXtr, BSONString(e)))) => result
  // ...
}
```

In case of write operation, handler is given the write operator along with the request itself, so dispatch can be based on this information (and combine with pattern matching on request content).

```scala
import acolyte.reactivemongo.{ WriteHandler, DeleteOp, InsertOp, UpdateOp }

val handler = WriteHandler { (op, wreq) =>
  (op, wreq) match {
    case (DeleteOp, Request("a-mongo-db.a-col-name", _)) => resultDelete
    case (InsertOp, _) => resultInsert
    case (UpdateOp, _) => resultUpdate
  }
}
```

There is also convenient extractor for write operations.

```
import acolyte.reactivemongo.{
  WriteHandler, 
  DeleteRequest, 
  InsertRequest, 
  UpdateRequest 
}

val handler = WriteHandler { (op, req) =>
  case InsertRequest("colname", ("prop1", BSONString("val")) :: _) => ???
  case UpdateRequest("colname", 
    ("sel", BSONString("ector")) :: Nil, 
    ("prop1", BSONString("val")) :: _) => ???
  case DeleteRequest("colname", ("sel", BSONString("ector")) :: _) => ???
}
```

> In case of insert operation, the `_id` property is added to original document, so it must be taken in account if pattern matching over properties of saved document.

### Result creation for queries

Mongo result to be returned by query handler, can be created as following:

```scala
import reactivemongo.bson.BSONDocument
import reactivemongo.core.protocol.Response 
import acolyte.reactivemongo.{ QueryResponse, PreparedResponse }

val error1: PreparedResponse = QueryResponse.failed("Error #1")
val error2 = QueryResponse("Error #1") // equivalent

val success1 = QueryResponse(BSONDocument("name" -> "singleResult"))
val success2 = QueryResponse.successful(BSONDocument("name" -> "singleResult"))

val success3 = QueryResponse(Seq(
  BSONDocument("name" -> "singleResult"), BSONDocument("price" -> 1.2D)))

val success4 = QueryResponse.successful(
  BSONDocument("name" -> "singleResult"), BSONDocument("price" -> 1.2D))

val success5 = QueryResponse.empty // successful empty response
val success6 = QueryResponse(List.empty[BSONDocument]) // equivalent

val countResponse = QueryResponse.count(4) // response to Mongo Count
```

When a handler supports some query cases, but not other, it can return an undefined response, to let the chance other handlers would manage it.

```scala
val undefined1 = QueryResponse(None)
val undefined2 = QueryResponse.undefined
```

### Result creation for write operation

Mongo result to be returned by write handler, can be created as following:

```scala
import reactivemongo.core.protocol.Response 
import acolyte.reactivemongo.{ WriteResponse, PreparedResponse }

val error1: PreparedResponse = WriteResponse.failed("Error #1")
val error2 = WriteResponse("Error #1") // equivalent
val error3 = WriteResponse.failed("Error #2", 1/* code */)
val error4 = WriteResponse("Error #2" -> 1/* code */) // equivalent

val success1 = WriteResponse(1/* update count */ -> true/* updatedExisting */)
val success2 = WriteResponse.successful(1, true) // equivalent
val success3 = WriteResponse() // = WriteResponse.successful(0, false)
```

When a handler supports some write cases, but not other, it can return an undefined response, to let the chance other handlers would manage it.

```scala
val undefined1 = WriteResponse(None)
val undefined2 = WriteResponse.undefined
```

## Integration

Acolyte for ReactiveMongo can be used with various test and persistence frameworks.

### Specs2

It can be used with [specs2](http://etorreborre.github.io/specs2/) to write executable specification for function accessing persistence.

```scala
import reactivemongo.bson.BSONDocument
import acolyte.reactivemongo.QueryResponse

object MySpec extends org.specs2.mutable.Specification {
  "Mongo persistence" should {
    "properly work with query result" in {
      withQueryResult(QueryResponse(BSONDocument(???))) { driver =>
        // code executing query with driver,
        // and parsing result as expected
      } aka "result" must beEqualTo(???).
        await(5) // as ReactiveMongo is async and returns Future
    }
  }

  // ...
}
```

In order to use same driver accross several example, a custom `After` trait can be used.

```scala
sealed trait WithDriver extends org.specs2.mutable.After {
  implicit lazy val driver = AcolyteDSL.driver
  def after = driver.close()
}

object MySpec extends org.specs2.mutable.Specification {
  "Foo" should {
    "Bar" >> new WithDriver {
      implicit val d = driver

      // many examples...
    }
  }
}
```

To make all Acolyte handlers in a specification share the same driver, it's possible to benefit from specs2 global teardown.

```scala
import org.specs2.specification.{ Fragments, Step }
import org.specs2.mutable.Specification

sealed trait WithDriver { specs: Specification =>
  implicit lazy val driver = AcolyteDSL.driver
  override def map(fs: => Fragments) = fs ^ Step(driver.close())
}

object MySpec extends Specification with WithDriver {
  // `driver` available for all examples
}
```

### SBT

Using SBT, a single driver/handler pool can be used for all tests, configuring `testOptions` with `Tests.Cleanup`.

First in test sources, define the shared driver.

```scala
package your.pkg

object Shared {
  lazy val driver = acolyte.reactivemongo.AcolyteDSL.driver
  def closeDriver = driver.close()
}
```

Then in SBT settings, this driver can be closed after testing.

```scala
testOptions in Test += Tests.Cleanup(cl => {
  val c = cl.loadClass("your.pkg.Shared$")
  type M = { def closeDriver(): Unit }
  val m: M = c.getField("MODULE$").get(null).asInstanceOf[M]
  m.closeDriver()
})
```

## Build

This module can be built from these sources using SBT (0.12.2+), 
from top directory (Acolyte base directory): 

```
# sbt 
> project reactive-mongo
> publish
```

## Test

```
# sbt 
> project reactive-mongo
> test
```

[![Build Status](https://secure.travis-ci.org/cchantep/acolyte.png?branch=master)](http://travis-ci.org/cchantep/acolyte)
