---
layout: page
title: Scala
subtitle: Scala DSL to use the Acolyte features
---

## Get started

For the impatient, bellow is a complete example.

```scala
import java.sql.{ Date, DriverManager }
import acolyte.jdbc.{
  AcolyteDSL, Driver ⇒ AcolyteDriver, QueryExecution, UpdateExecution
}
import acolyte.jdbc.RowLists.{ rowList1, rowList3 }
import acolyte.jdbc.Implicits._

// ...

// Prepare handler
val handlerA = AcolyteDSL.handleStatement.withQueryDetection(
  "^SELECT ", // regex test from beginning
  "EXEC that_proc"). // second detection regex
  withUpdateHandler { e: UpdateExecution ⇒
    if (e.sql.startsWith("DELETE ")) {
      // Process deletion ...
      /* deleted = */ 2;
    } else {
      // ... Process ...
      /* count = */ 1;
    }
  } withQueryHandler { e: QueryExecution ⇒
    if (e.sql.startsWith("SELECT ")) {
      // Empty resultset with 1 text column declared
      rowList1(classOf[String]).asResult
    } else {
      // ... EXEC that_proc
      // (see previous withQueryDetection)

      // Prepare list of 2 rows
      // with 3 columns of types String, Float, Date
      (rowList3(classOf[String], classOf[Float], classOf[Date]).
        withLabels( // Optional: set labels
          1 -> "String",
          3 -> "Date")
        :+ ("str", 1.2f, new Date(1l)) // tuple as row 
        :+ ("val", 2.34f, null)
      ).asResult

    }
  }

// Register prepared handler with expected ID 'my-handler-id'
AcolyteDriver.register("my-handler-id", handlerA)

// ... then connection is managed through |handler|
DriverManager.getConnection(jdbcUrl)
```

You can see detailed [use cases](https://github.com/cchantep/acolyte/blob/master/jdbc-scala/src/test/jdbc-scala/acolyte/ScalaUseCases.scala) whose expectations are visible in [specifications](https://github.com/cchantep/acolyte/blob/master/jdbc-scala/src/test/jdbc-scala/acolyte/AcolyteSpec.scala).

- *[Interactive demo](http://tour.acolyte.eu.org/)*
- *How to use [Acolyte connection](#connection)*
- *See online [API documentation](https://oss.sonatype.org/service/local/repositories/releases/archive/org/eu/acolyte/jdbc-scala_2.12/{{site.latest_release}}/jdbc-scala_2.12-{{site.latest_release}}-javadoc.jar/!/index.html)*.

## Setup in your project

Using SBT, Acolyte JDBC dependency can resolved as following.

```ocaml
libraryDependencies ++= Seq(
  "org.eu.acolyte" %% "jdbc-scala" % "{{site.latest_release}}" % "test"
)
```

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.eu.acolyte/jdbc-scala_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.eu.acolyte/jdbc-scala_2.12/)

## Connection 

As soon as you register Acolyte handler with a unique ID, corresponding connection can be resolved using JDBC URL including this ID as parameter.

```scala
// Register prepared handler with expected ID 'my-unique-id'
// handler: acolyte.jdbc.ConnectionHandler
//   or acolyte.jdbc.StatementHandler instance
acolyte.jdbc.Driver.register("my-unique-id", handlerA)

// then ...
// ... later as handler has registered with 'my-unique-id'
val jdbcUrl = "jdbc:acolyte:anything-you-want?handler=my-unique-id"

import java.sql.{ Connection, DriverManager }

val con1: Connection = DriverManager.getConnection(jdbcUrl)
// ... Connection |con1| is managed through |handler|
```

It's also possible to get directly get an Acolyte connection, without using JDBC driver registry:

```scala
import acolyte.jdbc.AcolyteDSL

val con2 = AcolyteDSL.connection(handlerA)
```

### Connection properties

JDBC allows to pass properties to driver to customize connection creation:

```java
import acolyte.jdbc.AcolyteDSL

val con3 = DriverManager.getConnection(jdbcUrl, someJavaUtilProps)
val con4 = AcolyteDSL.connection(handler, "prop" -> "value"/* ... */)
```

(See [properties](/java/#Connection_properties) meaningful for Acolyte)

## Query handler

In Scala query handler, pattern matching can be use to easily describe result case:

```scala
import acolyte.jdbc.{
  AcolyteDSL, QueryExecution, QueryResult, 
  DefinedParameter, ExecutedParameter, RowLists
}

AcolyteDSL.handleStatement.withQueryDetection("^SELECT").
  withQueryHandler { e: QueryExecution ⇒ 
    // ...

    e match {
      case QueryExecution(sql, DefinedParameter("str", _) :: Nil)
        if sql.startsWith("SELECT") ⇒
        // result when sql starts with SELECT
        // and there is only 1 parameter with "str" value
        QueryResult.Nil // any query result (e.g. empty row list)

      case QueryExecution(_, 
        ExecutedParameter(_) :: ExecutedParameter(2) :: _) ⇒
        // result when there is at least 2 parameters for any sql
        // with the second having integer value 2
        RowLists.stringList.append("foo").asResult
    }
  }
```

Partial function can also be used to describe handled cases:

```scala
import acolyte.jdbc.{ AcolyteDSL, UpdateExecution }
import acolyte.jdbc.Implicits._

AcolyteDSL.handleStatement.withUpdateHandler {
  case UpdateExecution("SELECT 1", Nil) => 1 /* case 1 */
  case UpdateExecution("SELECT 2", p1 :: Nil) => 2 /* case 2 */
  /* ... */
}
```

Using [scalac plugin](/scalac-plugin/), extractor `ExecutedStatement(regex, params)` can be used with [rich pattern matching](https://github.com/cchantep/acolyte/blob/master/jdbc-driver/src/test/jdbc-scala/acolyte/ExecutionSpec.scala):

```scala
import acolyte.jdbc.{ Execution, ExecutedParameter, ExecutedStatement }

def handle(e: Execution) = e match {
  case ~(ExecutedStatement("^SELECT"), // if sql starts with SELECT
    (matchingSql, ExecutedParameter("strVal") :: Nil)) => /* ... */
}
```

If you plan only to handle query (not update) statements, `handleQuery` can be used:

```scala
acolyte.jdbc.AcolyteDSL.handleQuery { e ⇒ 
  acolyte.jdbc.QueryResult.Nil // any query result
}
```

When you only need connection for a single result case, `withQueryResult` is useful:

```scala
import acolyte.jdbc.{ AcolyteDSL, QueryResult }

def res = QueryResult.Nil // any query result (single value, rows...)

// res: acolyte.jdbc.QueryResult
val str: String = AcolyteDSL.withQueryResult(res) { connection ⇒ "foo" }
```

### Result creation

Row lists can be built in the following way:

```scala
import acolyte.jdbc.RowLists

// ...

val list1 = RowLists.rowList1(classOf[String]) // RowList1
val list2 = // RowList3
  RowLists.rowList3(classOf[Int], classOf[Float], classOf[Char])
```

Column names/labels can also be setup (column first index is 1):

```scala
// ...

val list1up = list1.withLabel(1, "first label")
val list2up = list2.withLabel(2, "first label").withLabel(3, "third name")
```

Both column classes and names can be declared in bulk way:

```scala
import acolyte.jdbc.RowLists
import acolyte.jdbc.Implicits._

// ...

val list3 = RowLists.rowList1(
  classOf[String] -> "first label")

val list4 = RowLists.rowList3(
  classOf[Int] -> "1st",
  classOf[Float] -> "2nd",
  classOf[Char] -> "3rd")
```

[`RowList` factory](http://acolyte.eu.org/jdbc-driver-javadoc/acolyte/jdbc/RowLists.html) also provide convenience constructor for single column row list:

```scala
import acolyte.jdbc.RowLists
import acolyte.jdbc.Implicits._

// Instead of RowLists.rowList1(classOf[String]) :+ stringRow) ...
RowLists.stringList() :+ "string"

// Instead of RowLists.rowList1(Boolean.TYPE) :+ boolRow) ...
RowLists.booleanList() :+ true

// Instead of RowLists.rowList1(Byte.TYPE) :+ byteRow) ...
RowLists.byteList() :+ 3.toByte

// Instead of RowLists.rowList1(Short.TYPE) :+ shortRow) ...
RowLists.shortList() :+ 4.toShort

// Instead of RowLists.rowList1(Integer.TYPE) :+ intRow) ...
RowLists.intList() :+ 5

// Instead of RowLists.rowList1(Long.TYPE) :+ longRow) ...
RowLists.longList() :+ 6L

// Instead of RowLists.rowList1(Float.TYPE) :+ floatRow) ...
RowLists.floatList() :+ 7F

// Instead of RowLists.rowList1(Double.TYPE) :+ doubleRow) ...
RowLists.doubleList() :+ 8D

// Instead of RowLists.rowList1(classOf[BigDecimal]) :+ bdRow) ...
RowLists.bigDecimalList() :+ (new java.math.BigDecimal(9))

// Instead of RowLists.rowList1(classOf[Date]) :+ dateRow) ...
RowLists.dateList() :+ (new java.sql.Date(10L))

// Instead of RowLists.rowList1(classOf[Time]) :+ timeRow) ...
RowLists.timeList() :+ (new java.sql.Time(11L))

// Instead of RowLists.rowList1(classOf[Timestamp]) :+ tsRow) ...
RowLists.timestampList() :+ (new java.sql.Timestamp(12L))
```

Once you have declared your row list, and before turning it as result set, you can either add rows to it, or leave it empty.

```scala
import java.sql.ResultSet

// ...

val rs1: ResultSet = list1.append("str").resultSet()
val rs2: ResultSet = list2.resultSet()
```

## Generated keys

Update case not only returning update count but also generated keys can be represented with `UpdateResult`:

```java
import acolyte.jdbc.{ AcolyteDSL, RowLists }

// Result with update count == 1 and a generated key 2L
AcolyteDSL.updateResult(1, RowLists.longList.append(2L))
```

Keys specified on result will be given to JDBC statement `.getGeneratedKeys`.

## Implicits

To ease use of Acolyte DSL, implicit conversions are provided for `QueryResult`, query handler `QueryExecution => QueryResult` can be defined with following alternatives.

- `QueryExecution => RowList`: query result as given row lists.
- `QueryExecution => T`: one row with one column of type `T` as query result.

In same way, implicit conversions are provided for `UpdateResult` allowing update handler to defined as following.

- `UpdateExecution => Int`: update count as update result.

```scala
import acolyte.jdbc.{
  QueryExecution, QueryResult, RowLists, UpdateExecution, UpdateResult
}
import acolyte.jdbc.Implicits._

// Alternative definitions for query handler
val qh1: QueryExecution => QueryResult = 
  // Defined from QueryExecution => RowList
  { ex: QueryExecution =>
    RowLists.rowList2(classOf[String], classOf[Int]) :+ ("str", 2)
  }

val qh2: QueryExecution => QueryResult = // Defined from QueryExecution => T
  { ex: QueryExecution => 
    val qr: QueryResult = "str" // as RowList1[String] with only one row
    qr
  }

// Alternative definition for update handler
val uh1: UpdateExecution => UpdateResult =
  // Defined from UpdateExecution => Int
  { ex: UpdateExecution => /* update count = */ 2 }
```

## Debug utility

Acolyte can be use to create [scope of debugging](http://acolyte.eu.org/jdbc-scaladoc/#acolyte.jdbc.AcolyteDSL$@debuging[A]%28printer:acolyte.jdbc.QueryExecution=%3EUnit%29%28f:java.sql.Connection=%3EA%29:Unit), to check what is executed in the JDBC layer.

```scala
import acolyte.jdbc.AcolyteDSL

AcolyteDSL.debuging() { con =>
  val stmt = con.prepareStatement("SELECT * FROM Test WHERE id = ?")
  stmt.setString(1, "foo")
  stmt.executeQuery()
}
```

The previous example will print the following message on the stdout.

    Executed query: QueryExecution(SELECT * FROM Test WHERE id = ?,List(Param(foo, VARCHAR)))

The default printer (using stdout) can be replaced by any function `acolyte.jdbc.QueryExecution => Unit` (see [`QueryExecution`](http://acolyte.eu.org/jdbc-scaladoc/#acolyte.jdbc.QueryExecution) API).

```scala
import acolyte.jdbc.AcolyteDSL

val anyLogging: String => Unit = { msg: String => println(s"--> $msg") }

AcolyteDSL.debuging({ exec => anyLogging(s"Executed: $exec") }) { con =>
  // code using JDBC
}
```

> Any function using JDBC which is called within the scope, will be passed to the debug printer.

[Previous: Acolyte for Java](../java/)