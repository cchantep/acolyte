# Acolyte for ReactiveMongo

Acolyte API for ReactiveMongo.

## Motivation

Wherever in your code you use ReactiveMongo driver, you can pass Acolyte MongoDB driver instead tests.

Then any connection created will be managed by your Acolyte (query & writer) handlers.

## Usage

- 1. Configure connection handler according expected behaviour: which response to which query, which result for which write request.
- 2. Allow the persistence code to be given a `MongoDriver` according environment (e.g. test, dev, ..., prod).
- 3. Provide this testing driver to persistence code during validation.

```scala
import scala.concurrent.{ ExecutionContext, Future }

// 1. On one side configure the MongoDB handler
import acolyte.reactivemongo.{ AcolyteDSL, Request, WriteOp }

val connectionHandler = AcolyteDSL.handleQuery { _: Request =>
    // returns result according executed query
    ???
  } withWriteHandler { (_: WriteOp, _: Request) =>
    // returns result according executed write operation
    ???
  }

// 2. In MongoDB persistence code, allowing (e.g. cake pattern) 
// to provide driver according environment.
import reactivemongo.api.MongoDriver

trait MongoPersistence {
  def driver: MongoDriver

  /* Function using driver, whatever is the way it's provided */
  def foo: Future[Boolean] = ???
}

object ProdPersistence extends MongoPersistence {
  def driver: MongoDriver = ???
  /* e.g. Resolve driver according configuration file */
}

// 3. Finally in unit tests
import scala.concurrent.Future
import acolyte.reactivemongo.AcolyteDSL

def isOk(implicit ec: ExecutionContext): Future[Boolean] =
  AcolyteDSL.withDriver { d =>
    val persistenceWithTestingDriver = new MongoPersistence {
      val driver: MongoDriver = d // provide testing driver
    }

    persistenceWithTestingDriver.foo
  }
```

> When result Future is complete, MongoDB resources initialized by Acolyte are released (driver and connections).

For persistence code expecting driver as parameter, resolving testing driver is straightforward.

```scala
import scala.concurrent.{ ExecutionContext, Future }

import reactivemongo.api.{ MongoConnection, MongoDriver }

import acolyte.reactivemongo.ConnectionHandler
import acolyte.reactivemongo.AcolyteDSL.withConnection

def yourConnectionHandler: ConnectionHandler = ???

def yourFunctionUsingMongo(c: MongoConnection): String = ???

def res(implicit ec: ExecutionContext, d: MongoDriver): Future[String] =
  withConnection(yourConnectionHandler) { c =>
    val con: MongoConnection = c // configured with `yourConnectionHandler`

    val s: String = yourFunctionUsingMongo(con)
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

Driver behaviour is configured using connection handlers, themselves based on query and write handlers, managing respectively MongoDB queries or write operations, and returning appropriate result.

You can start looking at empty/no-op connection handler. With driver configured in this way, there is no query or write handler. So as no response is provided whatever is the command performed, it will raise explicit error `No response: ...` for every request.

```scala
import scala.concurrent.ExecutionContext
import reactivemongo.api.{ MongoConnection, MongoDriver }
import acolyte.reactivemongo.{ AcolyteDSL, ConnectionHandler }

def handler1: ConnectionHandler = ??? // e.g. AcolyteDSL.handle

def foo1(implicit ec: ExecutionContext, d: MongoDriver) =
  AcolyteDSL.withConnection(handler1) { c =>
    val noOpCon: MongoConnection = c
  }
```

Acolyte provides several ways to initialize MongoDB resources (driver, connection, DB and collection) your code could expect.

- `withDriver`
- `withConnection`
- `withDB`
- `withCollection`
- `withQueryHandler`
- `withQueryResult`
- `withWriteHandler`
- `withWriteResult`

```scala
import scala.concurrent.ExecutionContext

import reactivemongo.api.{ DB, MongoConnection, MongoDriver }
import reactivemongo.api.collections.bson.BSONCollection

import reactivemongo.bson.BSONDocument
import acolyte.reactivemongo.{ 
  AcolyteDSL,
  ConnectionHandler,
  QueryResponse,
  PreparedResponse,
  Request,
  WriteOp 
}

def yourHandler: ConnectionHandler = ???

def yourFunctionWorkingWithDriver(d: MongoDriver) = ???
def yourFunctionWorkingWithConnection(c: MongoConnection) = ???
def yourFunctionWorkingWithDB(db: DB) = ???
def yourFunctionWorkingWithCol(c: BSONCollection) = ???

def yourFunction1WorkingWithConnection(c: MongoConnection) = ???
def yourFunction2WorkingWithConnection(c: MongoConnection) = ???
def yourFunction3WorkingWithConnection(c: MongoConnection) = ???
def yourFunction4WorkingWithDB(db: DB) = ???

def queryResultForAll: PreparedResponse = ???
def writeResultForAll: PreparedResponse = ???
def aResp: PreparedResponse = ???

def simpleExamples1(implicit ec: ExecutionContext) = {
  // Simple cases
  AcolyteDSL.withDriver { implicit d: MongoDriver =>
    yourFunctionWorkingWithDriver(d)

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
    }) { con => yourFunctionWorkingWithConnection(con) }

    AcolyteDSL.withQueryResult(queryResultForAll) { con =>
      yourFunctionWorkingWithConnection(con)
    }

    AcolyteDSL.withWriteHandler({ (_: WriteOp, _: Request) => aResp }) { con =>
      yourFunctionWorkingWithConnection(con)
    }

    AcolyteDSL.withWriteResult(writeResultForAll) { con =>
      yourFunctionWorkingWithConnection(con)
    }
  }
}

def moreCompleteExamples1(implicit ec: ExecutionContext) = {
  AcolyteDSL.withDriver { implicit d =>
    AcolyteDSL.withConnection(yourHandler) { c1 =>
      if (yourFunction1WorkingWithConnection(c1))
        yourFunction2WorkingWithConnection(c1)
    }

    AcolyteDSL.withConnection(yourHandler) { c2 =>
      yourFunction3WorkingWithConnection(c2)
    }

    AcolyteDSL.withConnection(yourHandler) { c3 => // expect a Future
      AcolyteDSL.withDB(c3) { db => // expect a Future
        AcolyteDSL.withCollection(db, "colName") { // expect Future
          yourFunction4WorkingWithDB(db) // return a Future
        }
      }
    }
  }
}
```

Many other combinations are possible: see complete [test cases](https://github.com/cchantep/acolyte/blob/master/reactive-mongo/src/test/scala/acolyte/reactivemongo/DriverSpec.scala#L27).

### Configure connection behaviour

At this point we can focus on playing handlers. To handle MongoDB query and to return the kind of result your code should work with, you can do as following.

```scala
import scala.util.{ Failure, Success }

import scala.concurrent.ExecutionContext

import reactivemongo.api.{ Cursor, MongoConnection }
import reactivemongo.api.collections.bson.BSONCollection

import reactivemongo.bson.BSONDocument

import acolyte.reactivemongo.{ AcolyteDSL, PreparedResponse, Request }

def aResponse: PreparedResponse = ???

def setup1(implicit ec: ExecutionContext) =
  AcolyteDSL.withDriver { implicit driver =>
    AcolyteDSL.withConnection(
      AcolyteDSL handleQuery { req: Request => aResponse }) { c =>
      val readOnlyCon: MongoConnection = c
      // work with configured driver
    }
  }

// Then when MongoDB code is given this driver instead of production one ...
// (see DI or cake pattern) and resolve a BSON collection `col` by this way:

def foo2(col: BSONCollection)(implicit ec: ExecutionContext) =
  col.find(BSONDocument("anyQuery" -> 1)).
    cursor[BSONDocument]().collect[List](
      maxDocs = 10,
      err = Cursor.FailOnError[List[BSONDocument]]()
    ).onComplete {
      case Success(res) => ??? // In case of response given by provided handler
      case Failure(err) => ??? // "No response: " if case not handled
    }
```

In the same way, write operations can be responded with appropriate result.

```scala
import scala.util.{ Failure, Success }

import scala.concurrent.ExecutionContext

import reactivemongo.bson.BSONDocument
import reactivemongo.api.MongoConnection
import reactivemongo.api.collections.bson.BSONCollection

import acolyte.reactivemongo.{ AcolyteDSL, Request, WriteOp }

def setup2(implicit ec: ExecutionContext) =
  AcolyteDSL.withDriver { implicit driver =>
    AcolyteDSL.withConnection(
      AcolyteDSL handleWrite { (op: WriteOp, r: Request) => aResponse }) { c =>
      val writeOnlyDriver: MongoConnection = c
      // work with configured driver
    }
  }

// Then when MongoDB code is given this driver instead of production one ...
// (see DI or cake pattern) and resolve a BSON collection `col` by this way:

def foo3(col: BSONCollection)(implicit ec: ExecutionContext) = 
  col.insert(BSONDocument("prop" -> "value")).onComplete {
    case Success(res) => ??? // In case or response given by provided handler
    case Failure(err) => ??? // "No response: " if case not handled
  }
```

Obviously connection handler can manage both queries and write operations:

```scala
import scala.concurrent.ExecutionContext

import acolyte.reactivemongo.{
  AcolyteDSL, ConnectionHandler, PreparedResponse, Request, WriteOp
}

def aQueryResponse: PreparedResponse = ???
def aWriteResponse: PreparedResponse = ???

val completeHandler: ConnectionHandler = 
  AcolyteDSL.handleQuery { req: Request => 
    // First define query handling
    aQueryResponse
  } withWriteHandler { (op: WriteOp, req: Request) =>
    // Then define write handling
    aWriteResponse
  }

def foo4(implicit ec: ExecutionContext) = AcolyteDSL.withDriver { implicit d =>
  AcolyteDSL.withConnection(completeHandler) { con =>
    // work with configured connection
  }
}
```

### Request patterns

Pattern matching can be used in handler to dispatch result accordingly.

```scala
import reactivemongo.bson.{ BSONInteger, BSONString }

import acolyte.reactivemongo.{
  CountRequest,
  InClause,
  QueryHandler,
  PreparedResponse,
  Property,
  Request,
  RequestBody,
  SimpleBody,
  ValueDocument,
  ValueList,
  &
}

def resultA: PreparedResponse = ???
def resultB: PreparedResponse = ???
def resultC: PreparedResponse = ???
def resultD: PreparedResponse = ???
def resultE: PreparedResponse = ???
def resultF: PreparedResponse = ???
def resultG: PreparedResponse = ???
def resultH: PreparedResponse = ???
def resultI: PreparedResponse = ???
def resultJ: PreparedResponse = ???
def resultK: PreparedResponse = ???
def resultL: PreparedResponse = ???
def resultM: PreparedResponse = ???
def resultN: PreparedResponse = ???

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

    case Request(_, SimpleBody(("age", ValueDocument(
      ("$gt", BSONInteger(minAge)) :: Nil)) :: _)) =>
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

    case CountRequest(colName, ("email", BSONString("em@il.net")) :: Nil) =>
      // Matching on count query
      resultK

    case CountRequest(_, ("property", InClause(
      BSONString("A") :: BSONString("B") :: Nil)) :: Nil) =>
      resultL // matches count with selector on 'property' using $in operator

    case Request("col1", SimpleBody((
      "$in", ValueList(bsonA :: bsonB :: _)) :: Nil)) =>
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
import reactivemongo.bson.BSONString
import acolyte.reactivemongo.{ Property, PreparedResponse, Request, SimpleBody }

def result: PreparedResponse = ???

// With scalac plugin
def check1(request: Request) = request match {
  case Request("db.col", SimpleBody(
    ~(Property("email"), BSONString(e)))) => result
  // ...
}

// Without
val EmailXtr = Property("email")
// has declare email extractor before, as stable identifier

def check2(request: Request) = request match {
  case Request("db.col", SimpleBody(EmailXtr(BSONString(e)))) => result
  // ...
}
```

In case of write operation, handler is given the write operator along with the request itself, so dispatch can be based on this information (and combine with pattern matching on request content).

```scala
import acolyte.reactivemongo.{
  DeleteOp, InsertOp, UpdateOp, PreparedResponse, Request, WriteHandler
}

def resultDelete: PreparedResponse = ???
def resultInsert: PreparedResponse = ???
def resultUpdate: PreparedResponse = ???

val handler2 = WriteHandler { (op, wreq) =>
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

val handler3 = WriteHandler { (op, req) =>
  case InsertRequest("colname", ("prop1", BSONString("val")) :: _) => ???
  case UpdateRequest("colname", 
    ("sel", BSONString("ector")) :: Nil, 
    ("prop1", BSONString("val")) :: _) => ???
  case DeleteRequest("colname", ("sel", BSONString("ector")) :: _) => ???
}
```

> In case of insert operation, the `_id` property is added to original document, so it must be taken in account if pattern matching over properties of saved document.

### Result creation for queries

MongoDB result to be returned by query handler, can be created as following:

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

val countResponse = QueryResponse.count(4) // response to MongoDB Count
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
import reactivemongo.core.protocol.Response 
import acolyte.reactivemongo.{ WriteResponse, PreparedResponse }

val errorA: PreparedResponse = WriteResponse.failed("Error #1")
val errorB = WriteResponse("Error #1") // equivalent
val errorC = WriteResponse.failed("Error #2", 1/* code */)
val errorD = WriteResponse("Error #2" -> 1/* code */) // equivalent

val successA = WriteResponse(1/* update count */ -> true/* updatedExisting */)
val successB = WriteResponse.successful(1, true) // equivalent
val successC = WriteResponse({}) // = WriteResponse.successful(0, false)
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
import scala.concurrent.duration._
import reactivemongo.bson.BSONDocument

import acolyte.reactivemongo.QueryResponse
import acolyte.reactivemongo.AcolyteDSL.{ withQueryResult, withDriver }

class MySpec1(implicit ee: org.specs2.concurrent.ExecutionEnv)
  extends org.specs2.mutable.Specification {

  "MongoDB persistence" should {
    "properly work with query result" in {
      withDriver { implicit driver =>
        withQueryResult(QueryResponse(BSONDocument("foo" -> 1))) { con =>
          // code executing query with connection 'con',
          // and parsing result as expected
        }
      } aka "result" must beEqualTo(???).
        awaitFor(5.seconds) // as ReactiveMongo is async and returns Future
    }
  }

  // ...
}
```

In order to use same driver accross several example, a custom `After` trait can be used.

```scala
import acolyte.reactivemongo.AcolyteDSL

sealed trait WithDriver extends org.specs2.mutable.After {
  implicit lazy val driver = AcolyteDSL.driver
  def after = driver.close()
}

object MySpec2 extends org.specs2.mutable.Specification {
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
import org.specs2.specification.core.Fragments
import org.specs2.mutable.Specification

import acolyte.reactivemongo.AcolyteDSL

sealed trait WithDriver { specs: Specification =>
  implicit lazy val driver = AcolyteDSL.driver
  override def map(fs: => Fragments) = fs ^ step(driver.close())
}

object MySpec3 extends Specification with WithDriver {
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

```ocaml
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
