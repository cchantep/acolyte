# Acolyte for ReactiveMongo

Acolyte API for ReactiveMongo (0.10.0).

## Motivation

Wherever in your code you use ReactiveMongo driver, you can pass Acolyte Mongo driver instead during tests.

Then any connection created will be managed by your Acolyte (query & writer) handlers.

## Usage

- 1. Configure connection handler according expected behaviour: which response to which query, which result for which write request.
- 2. Create a custom `MongoDriver` instance, set up with prepared connection handler.

```scala
import reactivemongo.api.MongoDriver

import acolyte.reactivemongo.AcolyteDSL.{ driver, handleStatement }

val mongoDriver: MongoDriver = driver {
  ??? // dispatch query and write request as you want using pattern matching
}

val noOpDriver = driver { handleStatement/* ConnectionHandler.empty */}
```

### Request patterns

Pattern matching can be used in handler to dispatch result accordingly.

```scala
import reactivemongo.bson.{ BSONInteger, BSONString }

import acolyte.reactivemongo.{
  CollectionName, QueryHandler, RequestBody, Property, &
}

val queryHandler = QueryHandler { queryRequest =>
  queryRequest match {
    case RequestBody("a-mongo-db.a-col-name", _) => 
      // Any request on collection "a-mongo-db.a-col-name"
      resultA

    case RequestBody(colNameOfAnyOther, _)  => resultB // Any request

    case RequestBody(colName, (k1, v1) :: (k2, v2) :: Nil) => 
      // Any request with exactly 2 BSON properties
      resultC

    case RequestBody("db.col", ("email", BSONString(v)) :: _) =>
      // Request on db.col starting with email string property
      resultD

    case RequestBody("db.col", ("name", BSONString("eman")) :: _) =>
      // Request on db.col starting with an "name" string property,
      // whose value is "eman"
      resultE

    case RequestBody(_, ("age": ValueDocument(
      ("$gt", BSONInteger(minAge)) :: Nil))) =>
      // Request on any collection, with an "age" document as property,
      // itself with exactly one integer "$gt" property
      // e.g. `{ 'age': { '$gt', 10 } }`
      resultF

    case RequestBody("db.col", ~(Property("email"), BSONString(e))) =>
      // Request on db.col with an "email" string property,
      // anywhere in properties (possible with others which are ignored there)
      resultG

    case RequestBody("db.col", ~(Property("name"), BSONString("eman"))) =>
      // Request on db.col with an "name" string property with "eman" as value,
      // anywhere in properties (possibly with others which are ignored there).
      resultH

    case RequestBody(colName,
      ~(Property("age"), BSONInteger(age)) &
      ~(Property("email"), BSONString(v))) =>
      // Request on any collection, with an "age" integer property
      // and an "email" string property, possibly not in this order.
      resultI

    case RequestBody(colName, 
      ~(Property("age"), ValueDocument(
        ~(Property("$gt"), BSONInteger(minAge)))) &
      ~(Property("email"), BSONString(email))) =>
      // Request on any collection, with an "age" property with itself
      // a operator property "$gt" having an integer value, and an "email" 
      // property (at the same level as age), without order constraint.
      resultJ
  }
}
```

Pattern matching using rich syntax `~(..., ...)` requires [scalac plugin](../scalac-plugin/readme.html).
Without this plugin, such parameterized extractor need to be declared as stable identifier before `match` block:

```scala
// With scalac plugin
request match {
  case RequestBody("db.col", ~(Property("email"), BSONString(e))) => result
  // ...
}

// Without
val EmailXtr = Property("email")
// has declare email extractor before, as stable identifier

request match {
  case RequestBody("db.col", ~(EmailXtr, BSONString(e))) => result
  // ...
}
```

In case of write operation, handler is given the write operator along with the request itself, so dispatch can be based on this information (and combine with pattern matching on request content).

```scala
import acolyte.reactivemongo.{ WriteHandler, DeleteOp, InsertOp, UpdateOp }

val handler = WriteHandler { (op, wreq) =>
  (op, wreq) match {
    case (DeleteOp, RequestBody("a-mongo-db.a-col-name", _)) => resultDelete
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
```

When a handler supports some query cases, but not other, it can return an undefined response, to let the chance other handlers would manage it.

```scala
val undefined1 = QueryResponse(None)
val undefined2 = QueryResponse.empty
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
val undefined2 = WriteResponse.empty
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