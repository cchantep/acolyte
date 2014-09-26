# Acolyte for ReactiveMongo

Acolyte API for ReactiveMongo (0.10.0).

## Motivation

Wherever in your code you use ReactiveMongo driver, you can pass Acolyte Mongo driver instead during tests.

Then any connection created will be managed by your Acolyte (query & writer) handlers.

## Usage

- 1. Configure connection handler according expected behaviour: which response to which query, which result for which write request.
- 2. Create a custom `MongoDriver` instance, set up with prepared connection handler.

```scala
import resource.ManagedResource
import reactivemongo.api.MongoDriver
import acolyte.reactivemongo.AcolyteDSL.withDriver

val res: Future[String] = withDriver(yourConnectionHandler) { d =>
  val driver: MongoDriver = d // configured with `yourConnectionHandler`

  val s: String = yourFunctionUsingMongo(driver)
  // ... dispatch query and write request as you want using pattern matching

  s
}
```

> When result Future is complete, Mongo resources initialized by Acolyte are released (driver and connections).

As in previous example, main API object is [AcolyteDSL](https://github.com/cchantep/acolyte/blob/master/reactive-mongo/src/main/scala/acolyte/reactivemongo/AcolyteDSL.scala).

Dependency can be added to SBT project with `"org.eu.acolyte" %% "reactive-mongo" % "1.0.27"`, and in a Maven one as following:

```xml
<dependency>
  <groupId>org.eu.acolyte</groupId>
  <artifactId>reactive-mongo</artifactId>
  <version>1.0.27</version>
</dependency>
```

### Setup driver

Driver behaviour is configured using a connection handler, itself based on query and write handler, managing respectively Mongo queries or write operations, and returning appropriate result.

You can start looking at empty/no-op connection handler. With driver configured in this way, there is no query or write handler. So as no response is provided wherever is the command performed, it will raise explicit error `No response: ...` for every request.

```scala
import reactivemongo.api.MongoDriver
import acolyte.reactivemongo.AcolyteDSL

AcolyteDSL.withDriver(AcolyteDSL handle/*ConnectionHandler.empty*/) { d =>
  val noOpDriver: MongoDriver = d
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

> Naming convention is `withX(...) { a => b }` to use with your Mongo function which doesn't return `Future` result, and `withFlatX(...) { a => b }` when your Mongo function return result (to flatten `withFlatX` result as `Future[YourReturnType]`, not having for example `Future[Future[YourReturnType]]`).

```scala
import reactivemongo.api.{ MongoConnection, MongoDriver }
import reactivemongo.bson.BSONDocument
import acolyte.reactivemongo.{ 
  AcolyteDSL, QueryResponse, PreparedResponse, Request, WriteOp 
}

// Simple cases
AcolyteDSL.withDriver(yourHandler) { d =>
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

### Configure driver behaviour

At this point we can focus on playing handlers. To handle Mongo query and to return the kind of result your code should work with, you can do as following.

```scala
import reactivemongo.api.MongoDriver
import acolyte.reactivemongo.{ AcolyteDSL, Request }

AcolyteDSL.withDriver(
  AcolyteDSL handleQuery { req: Request => aResponse }) { d =>
    val readOnlyDriver: MongoDriver = d
    // work with configured driver
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
import reactivemongo.api.MongoDriver
import acolyte.reactivemongo.{ AcolyteDSL, Request, WriteOp }

AcolyteDSL.withDriver(
  AcolyteDSL handleWrite { (op: WriteOp, req: Request) => aResponse }) { d =>
    val writeOnlyDriver: MongoDriver = d
    // work with configured driver
  }

// Then when Mongo code is given this driver instead of production one ...
// (see DI or cake pattern) and resolve a BSON collection `col` by this way:

col.insert(BSONDocument("prop" -> "value")).onComplete {
  case Success(res) => ??? // In case or response given by provided handler
  case Failure(err) => ??? // "No response: " if case not handled
}
```

Obviously connection handler can manage both query and write:

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
  }
}
```

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

### Result creation for queries

Mongo result to be returned by query handler, can be created as following:

```scala
import reactivemongo.bson.BSONDocument
import reactivemongo.core.protocol.Response 
import acolyte.reactivemongo.QueryResponse

val error1: Option[Try[Response]] = QueryResponse.failed("Error #1")
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
import acolyte.reactivemongo.WriteResponse

val error1: Option[Try[Response]] = WriteResponse.failed("Error #1")
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
