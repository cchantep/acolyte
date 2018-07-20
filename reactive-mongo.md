---
layout: page
title: Acolyte for ReactiveMongo
subtitle: Acolyte API for ReactiveMongo
---

## Motivation

Wherever in your code you use ReactiveMongo driver, you can pass Acolyte MongoDB driver instead tests.

Then any connection created will be managed by your Acolyte (query & writer) handlers.

## Usage

- 1. Configure connection handler according expected behaviour: which response to which query, which result for which write request.
- 2. Allow the persistence code to be given a `MongoDriver` according environment (e.g. test, dev, ..., prod).
- 3. Provide this testing driver to persistence code during validation.

```scala
// 1. On one side configure the Mongo handler
import acolyte.reactivemongo.{
  AcolyteDSL, ConnectionHandler, QueryResponse, Request, WriteOp, WriteResponse
}

val connectionHandler1: ConnectionHandler = AcolyteDSL.handleQuery {
  req: Request => // returns result according executed query
    QueryResponse.empty

}.withWriteHandler { (op: WriteOp, req: Request) =>
  // returns result according executed write operation
  WriteResponse(1/* = update count */)
}

// 2. In Mongo persistence code, allowing (e.g. cake pattern) 
// to provide driver according environment.
import reactivemongo.api.MongoDriver

trait MongoPersistence {
  def driver: MongoDriver

  def foo = ??? /* Function using driver, whatever is the way it's provided */
}

object ProdPersistence extends MongoPersistence {
  /* e.g. Resolve driver according configuration file */
  def driver: MongoDriver = ???
}

// 3. Finally in unit tests
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import acolyte.reactivemongo.AcolyteDSL

def isOk: Future[Boolean] = AcolyteDSL.withFlatDriver { d =>
  val persistenceWithTestingDriver = new MongoPersistence {
    val driver: MongoDriver = d // provide testing driver
  }

  persistenceWithTestingDriver.foo
}
```

> When result Future is complete, MongoDB resources initialized by Acolyte are released (driver and connections).

For persistence code expecting driver as parameter, resolving testing driver is straightforward.

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import reactivemongo.api.{ MongoConnection, MongoDriver }

import acolyte.reactivemongo.AcolyteDSL,
  AcolyteDSL.{ withConnection, withFlatDriver }
import acolyte.reactivemongo.Request

def yourFunctionUsingMongo(drv: MongoDriver) = "foo"

def yourConnectionHandler = AcolyteDSL.handleQuery { req: Request =>
  acolyte.reactivemongo.QueryResponse( // any query result
    reactivemongo.bson.BSONDocument("foo" -> "bar")
  )
}

val res: Future[String] = withFlatDriver { implicit driver: MongoDriver =>
  withConnection(yourConnectionHandler) { c =>
    val con: MongoConnection = c // configured with `yourConnectionHandler`

    val s: String = yourFunctionUsingMongo(driver)
    // ... dispatch query and write request as you want using pattern matching

    s
  }
}
```

As in previous example, main API object is [Acolyte DSL](https://oss.sonatype.org/service/local/repositories/releases/archive/org/eu/acolyte/reactive-mongo_2.11/{{site.latest_release}}-j7p/reactive-mongo_2.11-{{site.latest_release}}-j7p-javadoc.jar/!/index.html#acolyte.reactivemongo.AcolyteDSL$).

Dependency can be added to SBT project with `"org.eu.acolyte" %% "reactive-mongo" % "{{site.latest_release}}"`, or in a Maven one as following:

```xml
<dependency>
  <groupId>org.eu.acolyte</groupId>
  <artifactId>reactive-mongo_2.11</artifactId>
  <version>{{site.latest_release}}-j7p</version>
</dependency>
```

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.eu.acolyte/reactive-mongo_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.eu.acolyte/reactive-mongo_2.11/)

### Get started

- [API documentation](https://oss.sonatype.org/service/local/repositories/releases/archive/org/eu/acolyte/reactive-mongo_2.11/{{site.latest_release}}-j7p/reactive-mongo_2.11-{{site.latest_release}}-j7p-javadoc.jar/!/index.html)
- [Tutorial](https://github.com/cchantep/acolyte/tree/reactivemongo-tutorial)

### Setup in your project

Driver behaviour is configured using connection handlers, themselves based on query and write handlers, managing respectively MongoDB queries or write operations, and returning appropriate result.

You can start looking at empty/no-op connection handler. With driver configured in this way, there is no query or write handler. So as no response is provided whatever is the command performed, it will raise explicit error `No response: ...` for every request.

```scala
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.MongoConnection
import acolyte.reactivemongo.AcolyteDSL

AcolyteDSL.withDriver { implicit drv: reactivemongo.api.MongoDriver =>
  AcolyteDSL.withConnection(
    AcolyteDSL.handle/*= ConnectionHandler.empty*/) { c =>
    val noOpCon: MongoConnection = c
  }
}
```

Acolyte provides several ways to initialize MongoDB resources (driver, connection, DB and collection) your code could expect.

- `withDriver` and `withFlatDriver`,
- `withConnection` and `withFlatConnection`,
- `withDB` and `withFlatDB`,
- `withCollection` and `withFlatCollection`,
- `withQueryHandler` and `withFlatQueryHandler`,
- `withQueryResult` and `withFlatQueryResult`,
- `withWriteHandler` and `withFlatWriteHandler`,
- `withWriteResult` and `withFlatWriteResult`.

> Naming convention is `withX(...) { a => b }` to use with your MongoDB function which doesn't return `Future` result, and `withFlatX(...) { a => b }` when your MongoDB function does return `Future` (so that result as `Future[T]` is flatten when returned, not having `Future[Future[YourReturnType]]` finally).

```scala
import scala.concurrent.ExecutionContext.Implicits.global

import reactivemongo.api.{ DefaultDB, MongoConnection, MongoDriver }
import reactivemongo.api.collections.bson.BSONCollection

import acolyte.reactivemongo.{ 
  AcolyteDSL, ConnectionHandler, QueryResponse, PreparedResponse,
  Request, WriteResponse, WriteOp
}

// Simple cases
def yourFunctionWorkingWithDriver(drv: MongoDriver) = ???

AcolyteDSL.withDriver { d: MongoDriver =>
  yourFunctionWorkingWithDriver(d)
}

def yourHandler: ConnectionHandler = AcolyteDSL.handleWrite {
  (_: WriteOp, _: Request) => WriteResponse(1) // update count for all
}
  
def yourFunctionWorkingWithConnection(con: MongoConnection) = ???

AcolyteDSL.withDriver { implicit drv: MongoDriver =>
  AcolyteDSL.withConnection(yourHandler) { c: MongoConnection =>
    yourFunctionWorkingWithConnection(c)
  }
}

def yourFunctionWorkingWithDB(db: DefaultDB) = ???

AcolyteDSL.withDriver { implicit drv: MongoDriver =>
  AcolyteDSL.withDB(yourHandler) { db: DefaultDB =>
    yourFunctionWorkingWithDB(db)
  }
}

def yourFunctionWorkingWithCol(col: BSONCollection) = ???

AcolyteDSL.withDriver { implicit drv: MongoDriver =>
  AcolyteDSL.withCollection(yourHandler, "colName") { col =>
    yourFunctionWorkingWithCol(col)
  }
}

AcolyteDSL.withDriver { implicit d: MongoDriver =>
  AcolyteDSL.withQueryHandler({ req: Request => 
    val resp: PreparedResponse = QueryResponse.empty // empty doc list
    resp
  }) { _ => yourFunctionWorkingWithDriver(d) }
}

def queryResultForAll: PreparedResponse = QueryResponse.empty

{
  implicit val shardedDriver = MongoDriver()
  // need to be closed

  AcolyteDSL.withQueryResult(queryResultForAll) { _ =>
    yourFunctionWorkingWithDriver(shardedDriver)
  }

  val writeRes = WriteResponse(1) // update count

  AcolyteDSL.withWriteHandler({ (_: WriteOp, _: Request) => writeRes }) { _ =>
    yourFunctionWorkingWithDriver(shardedDriver)
  }

  val writeResultForAll = WriteResponse.undefined

  AcolyteDSL.withWriteResult(writeResultForAll) { _ =>
    yourFunctionWorkingWithDriver(shardedDriver)
  }
}

// More complexe case
AcolyteDSL.withFlatDriver { implicit d: MongoDriver => // expect a Future
  val handler = AcolyteDSL.handleQuery { req: Request =>
    QueryResponse.empty // any query result
  }

  def yourFunction1WorkingWithConnection(con: MongoConnection) = true
  def yourFunction2WorkingWithConnection(con: MongoConnection) = ???

  AcolyteDSL.withConnection(handler) { c1 =>
    if (yourFunction1WorkingWithConnection(c1))
      yourFunction2WorkingWithConnection(c1)
  }

  def yourFunction3WorkingWithConnection(con: MongoConnection) = ???

  AcolyteDSL.withFlatConnection(handler) { c2 => // expect a Future
    yourFunction3WorkingWithConnection(c2) // return a Future
  }

  def yourFunctionWorkingWithColl(coll: BSONCollection) = ???

  AcolyteDSL.withFlatConnection(handler) { c3 => // expect a Future
    AcolyteDSL.withFlatDB(c3) { db => // expect a Future
      AcolyteDSL.withFlatCollection(db, "colName") { // expect Future
        yourFunctionWorkingWithColl(_) // return a Future
      }
    }
  }
}
```

Many other combinations are possible: see complete [test cases](https://github.com/cchantep/acolyte/blob/master/reactive-mongo/src/test/scala/DriverSpec.scala#L27).

### Configure connection behaviour

At this point we can focus on playing handlers. To handle MongoDB query and to return the kind of result your code should work with, you can do as following.

```scala
import scala.concurrent.ExecutionContext.Implicits.global

import reactivemongo.api.{ MongoConnection, MongoDriver }
import reactivemongo.bson.BSONDocument
import acolyte.reactivemongo.{
  AcolyteDSL, PreparedResponse, QueryResponse, Request
}

def aResponse: PreparedResponse = // any query result
  QueryResponse(BSONDocument("foo" -> 2))

AcolyteDSL.withDriver { implicit driver: MongoDriver =>
  AcolyteDSL.withConnection(
    AcolyteDSL.handleQuery { req: Request => aResponse }) { c =>
    val readOnlyCon: MongoConnection = c
    // work with configured driver
  }
}

// Then when Mongo code is given this driver instead of production one ...
// (see DI or cake pattern) and resolve a BSON collection `col` by this way:
import scala.util.{ Failure, Success }
import reactivemongo.api.collections.bson.BSONCollection

def bar(col: BSONCollection) = col.find(BSONDocument("anyQuery" -> 1)).
  cursor[BSONDocument]().collect[List]().onComplete {
    case Success(res) => ??? // In case of response given by provided handler
    case Failure(err) => ??? // "No response: " if case not handled
  }
```

In the same way, write operations can be responded with appropriate result.

```scala
import scala.concurrent.ExecutionContext.Implicits.global

import reactivemongo.api.{ MongoConnection, MongoDriver }
import acolyte.reactivemongo.{ AcolyteDSL, Request, WriteOp }

AcolyteDSL.withDriver { implicit driver: MongoDriver =>
  AcolyteDSL.withConnection(
    AcolyteDSL handleWrite { (op: WriteOp, req: Request) => aResponse }) { c =>
    val writeOnlyDriver: MongoConnection = c
    // work with configured driver
  }
}

// Then when Mongo code is given this driver instead of production one ...
// (see DI or cake pattern) and resolve a BSON collection `col` by this way:
import scala.util.{ Failure, Success }
import reactivemongo.bson.BSONDocument
import reactivemongo.api.collections.bson.BSONCollection

def foo(col: BSONCollection) = 
  col.insert(BSONDocument("prop" -> "value")).onComplete {
    case Success(res) => ??? // In case or response given by provided handler
    case Failure(err) => ??? // "No response: " if case not handled
  }
```

Obviously connection handler can manage both queries and write operations:

```scala
import scala.concurrent.ExecutionContext.Implicits.global

import acolyte.reactivemongo.{
  AcolyteDSL, QueryResponse, Request, WriteOp, WriteResponse
}

val completeHandler = AcolyteDSL.handleQuery { req: Request => 
  // First define query handling
  QueryResponse.undefined // any query result
} withWriteHandler { (op: WriteOp, req: Request) =>
  // Then define write handling
  WriteResponse.failed("Simulated error") // any write result
}

import reactivemongo.api.{ DefaultDB, MongoDriver }

AcolyteDSL.withDriver { implicit drv: MongoDriver =>
  AcolyteDSL.withDB(completeHandler) { db: DefaultDB =>
    // work with configured driver
  }
}
```

### Request patterns

Pattern matching can be used in handler to dispatch result accordingly.

```scala
import reactivemongo.bson.{ BSONInteger, BSONString }

import acolyte.reactivemongo.{
  CountRequest, QueryHandler, QueryResponse, Request, InClause, Property,
  RequestBody, SimpleBody, ValueDocument, ValueList, &
}

val queryHandler = QueryHandler { queryRequest =>
  queryRequest match {
    case Request("a-mongo-db.a-col-name", _) => 
      // Any request on collection "a-mongo-db.a-col-name"
      QueryResponse.undefined // result A

    case Request(colNameOfAnyOther, _)  => // Any request
      QueryResponse.undefined // result B 

    case Request(colName, SimpleBody((k1, v1) :: (k2, v2) :: Nil)) => 
      // Any request with exactly 2 BSON properties
      QueryResponse.undefined // result C

    case Request("db.col", SimpleBody(("email", BSONString(v)) :: _)) =>
      // Request on db.col starting with email string property
      QueryResponse.undefined // result D

    case Request("db.col", SimpleBody(("name", BSONString("eman")) :: _)) =>
      // Request on db.col starting with an "name" string property,
      // whose value is "eman"
      QueryResponse.undefined // result E

    case Request(_, SimpleBody(("age", ValueDocument(
      ("$gt", BSONInteger(minAge)) :: Nil)) :: _)) =>
      // Request on any collection, with an "age" document as property,
      // itself with exactly one integer "$gt" property
      // e.g. `{ 'age': { '$gt', 10 } }`
      QueryResponse.undefined // result F

    case Request("db.col", SimpleBody(~(Property("email"), BSONString(e)))) =>
      // Request on db.col with an "email" string property,
      // anywhere in properties (possible with others which are ignored there)
      QueryResponse.undefined // result G

    case Request("db.col", SimpleBody(
      ~(Property("name"), BSONString("eman")))) =>
      // Request on db.col with an "name" string property with "eman" as value,
      // anywhere in properties (possibly with others which are ignored there).
      QueryResponse.undefined // result H

    case Request(colName, SimpleBody(
      ~(Property("age"), BSONInteger(age)) &
      ~(Property("email"), BSONString(v)))) =>
      // Request on any collection, with an "age" integer property
      // and an "email" string property, possibly not in this order.
      QueryResponse.undefined // result I

    case Request(colName, SimpleBody(
      ~(Property("age"), ValueDocument(
        ~(Property("$gt"), BSONInteger(minAge)))) &
      ~(Property("email"), BSONString(email)))) =>
      // Request on any collection, with an "age" property with itself
      // a operator property "$gt" having an integer value, and an "email" 
      // property (at the same level as age), without order constraint.
      QueryResponse.undefined // result J

    case CountRequest(colName, ("email", BSONString("em@il.net")) :: Nil) =>
      // Matching on count query
      QueryResponse.count(10) // result K

    case CountRequest(_, ("property", InClause(
      BSONString("A") :: BSONString("B") :: Nil)) :: Nil) => {
      // matches count with selector on 'property' using $in operator
      QueryResponse.count(11) // result L
    }

    case Request("col1", SimpleBody(("$in", ValueList(
      bsonA :: bsonB :: _)) :: Nil)) =>
      // Matching BSONArray using with $in operator
      QueryResponse.undefined // result M

    case Request(_, RequestBody(List(("sel", BSONString("hector"))) ::
      List(("updated", BSONString("property"))) :: Nil)) ⇒ 
      // Matches a request with multiple document in body 
      // (e.g. update with selector)
      QueryResponse.undefined // result N

  }
}
```

Acolyte also provides extractors for inner clauses.

- `ValueList(List[(String, BSONValue)](_))` to match with `[...]`.
- `InClause(List[(String, BSONValue)](_))` to match with `{ '$in': [...] }`.
- `NotInClause(List[(String, BSONValue)](_))` to match with `{ '$nin': [...] }`.

Pattern matching using rich syntax `~(..., ...)` requires [scalac plugin](/scalac-plugin/).
Without this plugin, such parametrized extractor need to be declared as stable identifier before `match` block:

```scala
import reactivemongo.bson.BSONString
import acolyte.reactivemongo.{ Property, Request, SimpleBody }

// With scalac plugin
def test1(request: Request) = request match {
  case Request("db.col", SimpleBody(
    ~(Property("email"), BSONString(e)))) => ??? // result
  // ...
}

// Without
val EmailXtr = Property("email")
// has declare email extractor before, as stable identifier

def test2(request: Request) = request match {
  case Request("db.col", SimpleBody(EmailXtr(BSONString(e)))) => 
    ??? // result
  // ...
}
```

In case of write operation, handler is given the write operator along with the request itself, so dispatch can be based on this information (and combine with pattern matching on request content).

```scala
import acolyte.reactivemongo.{ 
  DeleteOp, InsertOp, Request, UpdateOp, WriteHandler, WriteResponse
}

val handler1 = WriteHandler { (op, wreq) =>
  (op, wreq) match {
    case (DeleteOp, Request("a-mongo-db.a-col-name", _)) => 
      WriteResponse(1) // result delete

    case (InsertOp, _) => WriteResponse.undefined // result insert
    case (UpdateOp, _) =>
      WriteResponse.failed("Simulated error, code = ", 12) // result update
  }
}
```

There is also convenient extractor for write operations.

```
import acolyte.reactivemongo.{
  DeleteRequest, 
  InsertRequest, 
  UpdateRequest,
  WriteHandler,
  WriteResponse
}

val handler2 = WriteHandler { (op, req) =>
  case InsertRequest("colname", ("prop1", BSONString("val")) :: _) => 
    WriteResponse(1) // update count

  case UpdateRequest("colname", 
    ("sel", BSONString("ector")) :: Nil, 
    ("prop1", BSONString("val")) :: _) => 
    WriteResponse(2)

  case DeleteRequest("colname", ("sel", BSONString("ector")) :: _) => 
    WriteResponse.failed("Simulated error")
}
```

> In case of insert operation, the `_id` property is added to original document, so it must be taken in account if pattern matching over properties of saved document.

### Result creation for queries

MongoDB result to be returned by query handler, can be created as following:

```scala
import reactivemongo.bson.BSONDocument
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
import acolyte.reactivemongo.QueryResponse

val undefined1 = QueryResponse(None)
val undefined2 = QueryResponse.undefined
```

### Result creation for write operation

MongoDB result to be returned by write handler, can be created as following:

```scala
import acolyte.reactivemongo.{ WriteResponse, PreparedResponse }

val error3: PreparedResponse = WriteResponse.failed("Error #1")
val error4 = WriteResponse("Error #1") // equivalent
val error5 = WriteResponse.failed("Error #2", 1/* code */)
val error6 = WriteResponse("Error #2" -> 1/* code */) // equivalent

val success7 = WriteResponse(1/* update count */ -> true/* updatedExisting */)
val success8 = WriteResponse.successful(1, true) // equivalent
val success9 = WriteResponse() // = WriteResponse.successful(0, false)
```

When a handler supports some write cases, but not other, it can return an undefined response, to let the chance other handlers would manage it.

```scala
import acolyte.reactivemongo.WriteResponse

val undefined3 = WriteResponse(None)
val undefined4 = WriteResponse.undefined
```

## Integration

Acolyte for ReactiveMongo can be used with various test and persistence frameworks.

### Specs2

It can be used with [specs2](http://etorreborre.github.io/specs2/) to write executable specification for function accessing persistence.

```scala
import reactivemongo.bson.BSONDocument
import acolyte.reactivemongo.{ AcolyteDSL, QueryResponse }

import org.specs2.concurrent.ExecutionEnv

class MySpec1(implicit ee: ExecutionEnv)
  extends org.specs2.mutable.Specification {

  implicit def driverProvider: reactivemongo.api.MongoDriver = ???

  "Mongo persistence" should {
    "properly work with query result" in {
      def res = QueryResponse(BSONDocument("foo" -> 1))

      AcolyteDSL.withQueryResult(res) { driver =>
        // code executing query with driver,
        // and parsing result as expected
      } aka "result" must beEqualTo(???).
        await // as ReactiveMongo is async and returns Future
    }
  }

  // ...
}
```

In order to use same driver across several example, a custom `After` trait can be used.

```scala
import acolyte.reactivemongo.AcolyteDSL

sealed trait WithDriver extends org.specs2.mutable.After {
  implicit lazy val driver = AcolyteDSL.driver
  def after = driver.close()
}

class MySpec2 extends org.specs2.mutable.Specification {
  "Foo" should {
    "Bar" >> new WithDriver {
      implicit val d = driver

      // many examples...
    }
  }
}
```

To make all Acolyte handlers in a specification share the same driver, it's possible to benefit from specs2 global tear down.

```scala
import org.specs2.specification.core.Fragments
import org.specs2.mutable.Specification

import acolyte.reactivemongo.AcolyteDSL

sealed trait WithDriver { specs: Specification =>
  implicit lazy val driver = AcolyteDSL.driver
  override def map(fs: => Fragments) = fs ^ step(driver.close())
}

class MySpec3 extends Specification with WithDriver {
  // `driver` available for all examples
}
```

### Play Framework

Acolyte can be used with the ReactiveMongo plugin for Play Framework, with instances of Play `ReactiveMongoApi` managed with Acolyte Handlers.

```scala
import scala.concurrent.ExecutionContext.Implicits.global

import reactivemongo.api.{ MongoConnection, MongoDriver }
import play.modules.reactivemongo.ReactiveMongoApi
import acolyte.reactivemongo.{ AcolyteDSL, PlayReactiveMongoDSL }

def codeBasedOnPlayReactiveMongo(api: ReactiveMongoApi) = ???

AcolyteDSL.withFlatDriver { implicit drv: MongoDriver =>
  AcolyteDSL.withConnection(connectionHandler1) { con: MongoConnection =>
    val mongo: ReactiveMongoApi = PlayReactiveMongoDSL.mongoApi(drv, con)

    codeBasedOnPlayReactiveMongo(mongo)
  }
}
```

*See online [API documentation](https://oss.sonatype.org/service/local/repositories/releases/archive/org/eu/acolyte/play-reactive-mongo_2.11/{{site.latest_release}}-j7p/play-reactive-mongo_2.11-{{site.latest_release}}-j7p-javadoc.jar/!/index.html#package)*

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

```ocaml
testOptions in Test += Tests.Cleanup(cl => {
  val c = cl.loadClass("your.pkg.Shared$")
  type M = { def closeDriver(): Unit }
  val m: M = c.getField("MODULE$").get(null).asInstanceOf[M]
  m.closeDriver()
})
```