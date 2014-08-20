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
  handleStatement
}
```

### Query patterns

Pattern matching can be used in query handler to dispatch result accordingly.

```scala
import reactivemongo.bson.{ BSONInteger, BSONString }

import acolyte.reactivemongo.{ CollectionName, QueryBody, Property, & }

query match {
  case QueryBody("a-mongo-db.a-col-name", _) => 
    // Any query on collection "a-mongo-db.a-col-name"
    resultA

  case QueryBody(colNameOfAnyOtherQuery, _)  => resultB // Any query

  case QueryBody(colName, (k1, v1) :: (k2, v2) :: Nil) => 
    // Any query with exactly 2 BSON properties
    resultC

  case QueryBody("db.col", ("email", BSONString(v)) :: _) =>
    // Query on db.col starting with email string property
    resultD

  case QueryBody("db.col", ("name", BSONString("eman")) :: _) =>
    // Query on db.col starting with an "name" string property,
    // whose value is "eman"
    resultE

  case QueryBody(_, ("age": ValueDocument(
    ("$gt", BSONInteger(minAge)) :: Nil))) =>
    // Query on any collection, with an "age" document as property,
    // itself with exactly one integer "$gt" property
    // e.g. `{ 'age': { '$gt', 10 } }`
    resultF

  case QueryBody("db.col", ~(Property("email"), BSONString(e))) =>
    // Query on db.col with an "email" string property,
    // anywhere in properties (possible with others which are ignored there)
    resultG

  case QueryBody("db.col", ~(Property("name"), BSONString("eman"))) =>
    // Query on db.col with an "name" string property with "eman" as value,
    // anywhere in properties (possibly with others which are ignored there).
    resultH

  case QueryBody(colName,
    ~(Property("age"), BSONInteger(age)) &
    ~(Property("email"), BSONString(v))) =>
    // Query on any collection, with an "age" integer property
    // and an "email" string property, possibly not in this order.
    resultI

  case QueryBody(colName, 
    ~(Property("age"), ValueDocument(
      ~(Property("$gt"), BSONInteger(minAge)))) &
    ~(Property("email"), BSONString(email))) =>
    // Query on any collection, with an "age" property with itself
    // a operator property "$gt" having an integer value, and an "email" 
    // property (at the same level as age), without order constraint.
    resultJ

}
```

Pattern matching using rich syntax `~(..., ...)` requires [scalac plugin](../scalac-plugin/readme.html).
Without this plugin, such parameterized extractor need to be declared as stable identifier before `match` block:

```scala
// With scalac plugin
query match {
  case QueryBody("db.col", ~(Property("email"), BSONString(e))) => result
  // ...
}

// Without
val EmailXtr = Property("email")
// has declare email extractor before, as stable identifier

query match {
  case QueryBody("db.col", ~(EmailXtr, BSONString(e))) => result
  // ...
}
```

### Result creation

```scala
import scala.util.Try
import reactivemongo.bson.BSONDocument
import reactivemongo.core.protocol.Response
import acolyte.reactivemongo.MongoDB

val error: Try[Response] = MongoDB.Error(1/* channel */, "Error message")

val success: Try[Response] = MongoDB.Success(2/* channel */,
  BSONDocument("prop" -> "doc1"), BSONDocument("prop" -> "doc2")/*...*/)
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