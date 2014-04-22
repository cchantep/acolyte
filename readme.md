# Acolyte

Acolyte is a JDBC driver designed for cases like mockup, testing, or any case you would like to be able to handle JDBC query by hand (or maybe that's only Chmeee's son on the Ringworld).

[![Build Status](https://secure.travis-ci.org/cchantep/acolyte.png?branch=master)](http://travis-ci.org/cchantep/acolyte)

This documentation can be read [online](http://cchantep.github.io/acolyte/).

## Requirements

* Java 1.6

## Usage

Acolyte driver behaves as any other JDBC driver, that's to say you can get a connection from, by using the well-known `java.sql.DriverManager.getConnection(jdbcUrl)` (and its variants).

JDBC URL should match `jdbc:acolyte:anything-you-want?handler=id` (see after for details about `handler` parameter).

Projects using Acolyte:

- [Play Framework](http://www.playframework.com/) Anorm ([AnormSpec](https://github.com/playframework/playframework/blob/master/framework/src/anorm/src/test/jdbc-scala/anorm/AnormSpec.scala)). 
- [Cielago](https://github.com/cchantep/cielago-tracker) ([DispatchReportSpec](https://github.com/cchantep/cielago-tracker/blob/master/test/models/DispatchReportSpec.scala), [ListInfoSpec](https://github.com/cchantep/cielago-tracker/blob/master/test/models/ListInfoSpec.scala), [MainSpec](https://github.com/cchantep/cielago-tracker/blob/master/test/controllers/MainSpec.scala), …).

### Java

Using Maven 2/3+, Acolyte dependency can be resolved as following from your POM:

```xml
<!-- ... -->

  <repositories>
    <!-- ... -->
    <repository>
      <!-- Optional: Get Acolyte release quicker than Maven Central repo -->
      <id>applicius-releases</id>
      <name>Applicius Maven2 Snapshots Repository</name>
      <url>https://raw.github.com/applicius/mvn-repo/master/releases/</url>
    </repository>
  </repositories>

  <dependencies>
    <!-- ... -->
    <dependency>
      <groupId>org.eu.acolyte</groupId>
      <artifactId>jdbc-driver</artifactId>
      <version>VERSION</version>
    </dependency>
  </dependencies>

```

Then code could be:

```java
import java.util.List;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Date;

import acolyte.ConnectionHandler;
import acolyte.StatementHandler;
import acolyte.CompositeHandler;
import acolyte.RowList3;
import acolyte.Result;
import acolyte.Rows;

import acolyte.StatementHandler.Parameter;

import static acolyte.RowLists.rowList3;
import static acolyte.Rows.row3;

// ...

// Configure in anyway JDBC with following url,
// declaring handler registered with 'my-unique-id' will be used.
final String jdbcUrl = "jdbc:acolyte:anything-you-want?handler=my-unique-id"

// Prepare handler
StatementHandler handler = new CompositeHandler().
  withQueryDetection("^SELECT "). // regex test from beginning
  withQueryDetection("EXEC that_proc"). // second detection regex
  withUpdateHandler(new CompositeHandler.UpdateHandler() {
    // Handle execution of update statement (not query)
    public UpdateResult apply(String sql, List<Parameter> parameters) {
      // ...
      return UpdateResult.Nothing;
    }
  }).withQueryHandler(new CompositeHandler.QueryHandler () {
    public QueryResult apply(String sql, List<Parameter> parameters) {
      // ...

      // Prepare list of 2 rows
      // with 3 columns of types String, Float, Date
      RowList3<String, Float, Date> rows = 
        rowList3(String.class, Float.class, Date.class).
        withLabel(1, "String").withLabel(3, "Date"). // Optional: set labels
        append("str", 1.2f, new Date(1, 2, 3)). // values append
        append("val", 2.34f, null);

      return rows.asResult();
    }
  });

// Register prepared handler with expected ID 'my-unique-id'
acolyte.Driver.register("my-unique-id", handler);

// then ...
Connection con = DriverManager.getConnection(jdbcUrl);

// ... Connection |con| is managed through |handler|
```

You can see more [use cases](https://github.com/cchantep/acolyte/blob/master/jdbc-driver/src/test/java/usecase/JavaUseCases.java) whose expectations are visible in [specifications](https://github.com/cchantep/acolyte/blob/master/jdbc-driver/src/test/jdbc-scala/acolyte/AcolyteSpec.scala).

#### Connection 

Connection to Acolyte can be accessed by JDBC URL, if your handler has been registered against driver:

```java
// Register prepared handler with expected ID 'my-unique-id'
// handler: acolyte.ConnectionHandler or acolyte.StatementHandler instance
acolyte.Driver.register("my-unique-id", handler);

// then ...
// ... later as handler has registered with 'my-unique-id'
final String jdbcUrl = "jdbc:acolyte:anything-you-want?handler=my-unique-id"

Connection con = DriverManager.getConnection(jdbcUrl);
// ... Connection |con| is managed through |handler|
```

If you just need/want to directly get connection from `acolyte.Driver`, without using JDBC driver registry, you can use Acolyte direct connection:

```java
// handler: acolyte.ConnectionHandler or acolyte.StatementHandler instance
Connection con = acolyte.Driver.connection(handler);
```

### Connection properties

JDBC allows to pass properties to driver to customize connection creation:

```java
Connection con = DriverManager.getConnection(jdbcUrl, someJavaUtilProps);
Connection con = acolyte.Driver.connection(handler, someJavaUtilProps);
```

Acolyte specific properties are:

- `acolyte.parameter.untypedNull`: If `"true"`, Acolyte fallbacks untyped null from `statement.setObject(p, null)` to null string (default: false).
- `acolyte.batch.continueOnError`: If `"true"`, Acolyte doesn't stop executing batch on statement, but continue processing and finally throw `BatchUpdateException` with update counts of successfully executed elements (see [java.sql.Statement#executeBatch](http://docs.oracle.com/javase/7/docs/api/java/sql/Statement.html#executeBatch%28%29)).

#### Query result creation

Acolyte provides [Row](http://cchantep.github.io/acolyte/apidocs/acolyte/Row.html) and [RowList](http://cchantep.github.io/acolyte/apidocs/acolyte/RowList.html) classes (and their sub-classes) to allow easy and typesafe creation of result.

Row lists can be built as following using [RowLists factory](http://cchantep.github.io/acolyte/apidocs/acolyte/RowLists.html).

```java
import acolyte.RowList1;
import acolyte.RowList3;

import static acolyte.RowLists.rowList1;
import static acolyte.RowLists.rowList3; 

// ...

RowList1<String> list1 = RowLists.rowList1(String.class);

RowList3<Integer, Float, Character> list2 = RowLists.
  rowList3(Integer.class, Float.class, Character.class)
```

In previous example, `list1` is a list of row with 1 column whose class is `String` (`VARCHAR` as for JDBC/SQL type).
Considering `list2`, it is a list of row with 3 columns, whose classes are `Integer`, `Float` and `Character`.

Column names/labels can also be setup (column first index is 1):

```java
// ...

list1 = list1.withLabel(1, "first label");

list2 = list2.withLabel(2, "first label").withLabel(3, "third name");
```

Both column classes and names can be declared in bulk way, using [definition class](http://cchantep.github.io/acolyte/apidocs/acolyte/RowList.Column.html):

```java
import static acolyte.RowList.Column;

// ...

RowList1<String> list1 = RowLists.
  rowList1(Column(String.class, "first label"));

RowList3<Integer, Float, Character> list2 = RowLists.
  rowList3(Column(Integer.class, "1st"), 
           Column(Float.class, "2nd"), 
           Column(Character.class, "3rd"));
```

Once you have declared your row list, and before turning it as result set, you can either add rows to it, or leave it empty.

```java
import java.sql.ResultSet;

import static acolyte.Rows.row1;

// ...

// we have declared list1 and list2 (see previous example)

list1 = list1.append("str");

ResultSet rs1 = list1.resultSet();
ResultSet rs2 = list2.resultSet();
```

From previous example, result set `rs1` will contain 1 row, whereas `rs2` is empty.

Take care to `list1 = list1.append("str");`. As provided `RowList` classes are immutable, you should get updated instance from `append` to work on the list containing added row. This is more safe, and allow to rewrite previous example like:

```java
ResultSet rs1 = list1.append("str").resultSet();
ResultSet rs2 = list2.resultSet();
```

[RowLists factory](http://cchantep.github.io/acolyte/apidocs/acolyte/RowLists.html) also provide convinience constructor for single column row list:

```java
// Instead of RowLists.rowList1(String.class).append("string") ...
RowLists.stringList().append("string");
RowLists.stringList("string"/* ... */); // init with value(s)

// Instead of RowLists.rowList1(Boolean.TYPE).append(true) ...
RowLists.booleanList().append(true);
RowLists.booleanList(true/* ... */);

// Instead of RowLists.rowList1(Byte.TYPE).append((byte)1) ...
RowLists.byteList().append((byte)1);
RowLists.byteList((byte)1/* ... */);

// Instead of RowLists.rowList1(Short.TYPE).append((short)1) ...
RowLists.shortList().append((short)1);
RowLists.shortList((short)1/* ... */);

// Instead of RowLists.rowList1(Integer.TYPE).append(1) ...
RowLists.intList().append(1);
RowLists.intList(1/* ... */);

// Instead of RowLists.rowList1(Long.TYPE).append(1l) ...
RowLists.longList().append(1l);
RowLists.longList(1l/* ... */);

// Instead of RowLists.rowList1(Float.TYPE).append(1f) ...
RowLists.floatList().append(1f);
RowLists.floatList(1f/* ... */);

// Instead of RowLists.rowList1(Double.TYPE).append(1d) ...
RowLists.doubleList().append(1d);
RowLists.doubleList(1d/* ... */);

// Instead of RowLists.rowList1(BigDecimal.class).append(bigDecimal) ...
RowLists.bigDecimalList().append(bigDecimal);
RowLists.bigDecimalList(bigDecimal/* ... */);

// Instead of RowLists.rowList1(Date.class).append(date) ...
RowLists.dateList().append(date);
RowLists.dateList(date/* ... */);

// Instead of RowLists.rowList1(Time.class).append(time) ...
RowLists.timeList().append(time);
RowLists.timeList(time/* ... */);

// Instead of RowLists.rowList1(Timestamp.class).append(ts) ...
RowLists.timestampList().append(tsRow);
RowLists.timestampList(tsRow/* ... */);
```

#### SQL Warnings

Acolyte can also mock up SQL warnings, on update or query, so that `java.sql.Statement.getWarnings()` will returned expected instance.

```java
import acolute.UpdateResult;
import acolyte.QueryResult;

// ...

// Update results to be returned from an acolyte.UpdateHandler
UpdateResult upNothingWarn = UpdateResult.Nothing.withWarning("Nothing");
UpdateResult up1ResWithWarn = UpdateResult.One.withWarning("Warning 1");
UpdateResult up10ResWithWarn = new UpdateResult(10).
  withWarning("updateCount = 10 with warning");

// Query result (wrapping row list) to be returned from acolyte.QueryHandler
QueryResult nilWithWarning = QueryResult.Nil.withWarning("Nil with warning");
QueryResult resWithWarning = aRowList.asResult().
  withWarning("Row list result with warning");
```

### Generated keys

Update case not only returning update count but also generated keys can be represented with `UpdateResult`:

```java
import acolyte.UpdateResult;
import acolyte.RowLists;

// Result with update count == 1 and a generated key 2L
UpdateResult.One.withGeneratedKeys(RowLists.longList().append(2L));
```

Keys specified on result will be given to JDBC statement `.getGeneratedKeys()`.

### Scala

Module `jdbc-scala` provide a Scala DSL to use more friendly Acolyte features.

Using SBT, Acolyte dependency can resolved as following:

```scala
resolvers += 
  "Applicius Snapshots" at "https://raw.github.com/applicius/mvn-repo/master/snapshots/"

libraryDependencies += "org.eu.acolyte" %% "jdbc-scala" % "VERSION" % "test"
```

Then code could be:

```scala
import java.sql.{ Connection ⇒ SqlConnection, Date, DriverManager }
import acolyte.{ Driver ⇒ AcolyteDriver, Execution }
import acolyte.RowLists.{ rowList1, rowList3 }
import acolyte.Rows.row3
import acolyte.Acolyte // DSL
import acolyte.Implicits._

// ...

// Prepare handler
val handler: CompositeHandler = Acolyte.handleStatement.
  withQueryDetection(
    "^SELECT ", // regex test from beginning
    "EXEC that_proc"). // second detection regex
  withUpdateHandler { e: Execution ⇒
    if (e.sql.startsWith("DELETE ")) {
      // Process deletion ...
      /* deleted = */ 2;
    } else {
      // ... Process ...
      /* count = */ 1;
    }
  } withQueryHandler { e: Execution ⇒
    if (e.sql.startsWith("SELECT ")) {
      // Empty resultset with 1 text column declared
      rowList1(String.class).asResult
    } else {
      // ... EXEC that_proc
      // (see previous withQueryDetection)

      // Prepare list of 2 rows
      // with 3 columns of types String, Float, Date
      rowList3(classOf[String], classOf[Float], classOf[Date]).
        withLabels( // Optional: set labels
          1 -> "String",
          3 -> "Date")
        :+ ("str", 1.2f, new Date(1l)) // tuple as row 
        :+ ("val", 2.34f, null)).
        asResult

    }
  }

// Register prepared handler with expected ID 'my-handler-id'
AcolyteDriver.register("my-handler-id", handler)

// ... then connection is managed through |handler|
DriverManager.getConnection(jdbcUrl)
```

You can see detailed [use cases](https://github.com/cchantep/acolyte/blob/master/jdbc-scala/src/test/jdbc-scala/acolyte/ScalaUseCases.scala) whose expectations are visible in [specifications](https://github.com/cchantep/acolyte/blob/master/jdbc-scala/src/test/jdbc-scala/acolyte/AcolyteSpec.scala).

It's also possible to get directly get an Acolyte connection, without using JDBC driver registry:

```scala
import acolyte.Acolyte.connection

val con = connection(handler)
```

#### Query handler

In Scala query handler, pattern matching can be use to easily describe result case:

```scala
import acolyte.{ QueryExecution, DefinedParameter, ExecutedParameter }

handleStatement.withQueryDetection("^SELECT").
  withQueryHandler { e: QueryExecution ⇒ 
    // ...

    e match {
      case QueryExecution(sql, DefinedParameter("str", _) :: Nil)
        if sql.startsWith("SELECT") ⇒
        // result when sql starts with SELECT
        // and there is only 1 parameter with "str" value

      case QueryExecution(_, 
        ExecutedParameter(_) :: ExecutedParameter(2) :: _) ⇒
        // result when there is at least 2 parameters for any sql
        // with the second having integer value 2
    }
  }
```

Partial function can also be used to describe handled cases:

```scala
/* ... */ withUpdateHandler {
  case UpdateExecution("SELECT 1", Nil) => /* case 1 */
  case UpdateExecution("SELECT 2", p1 :: Nil) => /* case 2 */
  /* ... */
}
```

With [scalac plugin](./scalac-plugin.html), extractor `ExecutedStatement(regex, params)` can be used with [rich pattern matching](https://github.com/cchantep/acolyte/blob/master/jdbc-driver/src/test/jdbc-scala/acolyte/ExecutionSpec.scala):

```scala
e/*: Execution match {
  case ~(ExecutedStatement("^SELECT"), // if sql starts with SELECT
    (matchingSql, ExecutedParameter("strVal") :: Nil)) => /* ... */
}
```

If you plan only to handle query (not update) statements, `handleQuery` can be used:

```scala
handleQuery withQueryHandler { e ⇒ … }
```

When you only need connection for a single result case, `withQueryResult` is useful:

```scala
import acolyte.Acolyte

// res: acolyte.QueryResult
val str: String = Acolyte.withQueryResult(res) { connection ⇒ … }
```

### Anorm

Acolyte is useful to write test about persistence in projects using [Anorm](http://www.playframework.com/documentation/2.2.x/ScalaAnorm): read [10 minutes tutorial about Acolyte with Anorm](https://github.com/cchantep/acolyte/tree/10m-anorm-tutorial#acolyteanorm-10-minutes-tutorial).

### Play Framework

Acolyte can be easily used with Play test helpers.

First step is to create a Play fake application:

```scala
import play.api.test.FakeApplication
import acolyte.{ StatementHandler }

def fakeApp(h: Option[StatementHandler] = None): FakeApplication =
  FakeApplication(additionalConfiguration = Map(
    "application.secret" -> "test",
    "evolutionplugin" -> "disabled") ++ h.fold(Map[String, String]())(
      handler ⇒ {
        val id = System.identityHashCode(this).toString
        acolyte.Driver.register(id, handler)

        Map("db.default.driver" -> "acolyte.Driver",
          "db.default.url" -> "jdbc:acolyte:test?handler=%s".format(id))
      }))
```

Then Play/DB test can be performed as following:

```scala
lazy val handler = Some(handleStatement.
  withQueryDetection("^SELECT") withQueryHandler { e ⇒
    // Any Acolyte result
  })

Helpers.running(fakeApp(handler)) {
  DB withConnection { con ⇒
    // Connection |con| will use provided |handler|
    // So any DB related test can be done there
  }
}
```

#### Update/query handlers

As implicit conversions are provided for `QueryResult`, query handler `QueryExecution => QueryResult` can be defined with following alternatives.

- `QueryExecution => RowList`: query result as given row lists.
- `QueryExecution => T`: one row with one column of type `T` as query result.

In same way, implicit conversions are provided for `UpdateResult` allowing update handler to defined as following.

- `UpdateExecution => Int`: update count as update result.

```scala
import acolyte.{ QueryResult, RowLists, UpdateResult }
import acolyte.Implicits._

// Alternative definitions for query handler
val qh1: QueryExecution => QueryResult = 
  // Defined from QueryExecution => RowList
  { ex: QueryExecution =>
    RowLists.rowList2(classOf[String], classOf[Int]) :+ ("str", 2)
  }

val qh2: QueryExecution => QueryResult = // Defined from QueryExecution => T
  { ex: QueryExecution => 
    val ur: UpdateResult = "str" // as RowList1[String] with only one row
    ur
  }

// Alternative definition for update handler
val uh1: UpdateExecution => UpdateResult =
  // Defined from UpdateExecution => Int
  { ex: UpdateExecution => /* update count = */ 2 }
```

#### Result creation

Row lists can be built in the following way:

```scala
import acolyte.{ RowList1, RowList3 }
import acolyte.RowLists.{ rowList1, rowList3 }

// ...

val list1 = RowLists.rowList1(String.class)
val list2 = RowLists.rowList3(classOf[Int], classOf[Float], classOf[Char])
```

Column names/labels can also be setup (column first index is 1):

```scala
// ...

val list1up = list1.withLabel(1 -> "first label")
val list2up = list2.withLabel(2 -> "first label").withLabel(3 -> "third name")
```

Both column classes and names can be declared in bulk way:

```scala
import acolyte.{ RowLists, RowList1, RowList3 }

// ...

val list1 = RowLists.rowList1(
  classOf[String] -> "first label")

val list2 = RowLists.rowList3(
  classOf[Int] -> "1st",
  classOf[Float] -> "2nd",
  classOf[Char] -> "3rd")
```

[RowLists factory](http://cchantep.github.io/acolyte/apidocs/acolyte/RowLists.html) also provide convinience constructor for single column row list:

```scala
// Instead of RowLists.rowList1(classOf[String]) :+ stringRow) ...
RowLists.stringList() :+ stringRow

// Instead of RowLists.rowList1(Boolean.TYPE) :+ boolRow) ...
RowLists.booleanList() :+ boolRow

// Instead of RowLists.rowList1(Byte.TYPE) :+ byteRow) ...
RowLists.byteList() :+ byteRow

// Instead of RowLists.rowList1(Short.TYPE) :+ shortRow) ...
RowLists.shortList() :+ shortRow

// Instead of RowLists.rowList1(Integer.TYPE) :+ intRow) ...
RowLists.intList() :+ intRow

// Instead of RowLists.rowList1(Long.TYPE) :+ longRow) ...
RowLists.longList() :+ longRow

// Instead of RowLists.rowList1(Float.TYPE) :+ floatRow) ...
RowLists.floatList() :+ floatRow

// Instead of RowLists.rowList1(Double.TYPE) :+ doubleRow) ...
RowLists.doubleList() :+ doubleRow

// Instead of RowLists.rowList1(classOf[BigDecimal]) :+ bdRow) ...
RowLists.bigDecimalList() :+ bdRow

// Instead of RowLists.rowList1(classOf[Date]) :+ dateRow) ...
RowLists.dateList() :+ dateRow

// Instead of RowLists.rowList1(classOf[Time]) :+ timeRow) ...
RowLists.timeList() :+ timeRow

// Instead of RowLists.rowList1(classOf[Timestamp]) :+ tsRow) ...
RowLists.timestampList() :+ tsRow
```

Once you have declared your row list, and before turning it as result set, you can either add rows to it, or leave it empty.

```scala
import java.sql.ResultSet

import acolyte.Rows.row1

// ...

val rs1: ResultSet = list1.append("str").resultSet()
val rs2: ResultSet = list2.resultSet()
```

### Converters

Row instance can be maid more Scala-friendly using converter.

```scala
import acolyte.Rows.row2
import acolyte.JavaConverters.rowAsScala

val row = row2(classOf[String], classOf[Float])
val cells = row.list // implicit convert while getting Scala list of cells
```

### Specs2

Acolyte can be used with specs2 to write executable specification for function accessing persistence.

Considering a sample persistence function:

```scala
object Zoo {
  trait Animal { def name: String }
  case class Bird(name: String, fly: Boolean = true) extends Animal
  case class Dog(name: String, color: String) extends Animal

  def atLocation(con: java.sql.Connection)(id: Int): Option[Animal] = {
    // Yes would be better with something like Anorm or Slick
    var stmt: java.sql.PreparedStatement = null
    var rs: java.sql.ResultSet = null

    try {
      stmt = con.prepareStatement("SELECT * FROM zoo WHERE location = ?")
      stmt.setInt(1, id)

      rs = stmt.executeQuery()
      rs.next()

      rs.getString("type") match {
        case "bird" ⇒ Some(Bird(rs.getString("name"), rs.getBoolean("fly")))
        case "dog"  ⇒ Some(Dog(rs.getString("name"), rs.getString("color")))
        case _      ⇒ None
      }
    } catch {
      case _: Throwable ⇒ sys.error("Fails to locate animate")
    } finally {
      try { rs.close() }
      try { stmt.close() }
    }
  }
}
```

Then following specification can be written, checking that query result is properly selected and mapped:

```scala
import acolyte.Implicits._
import acolyte.Acolyte.{ connection, handleQuery } // DSL
import acolyte.RowLists.rowList5
import acolyte.Rows.row5
import Zoo._

object ZooSpec extends org.specs2.mutable.Specification {
  val zooSchema = rowList5(
    classOf[String] -> "type",
    classOf[Int] -> "location",
    classOf[String] -> "name",
    classOf[Boolean] -> "fly",
    classOf[String] -> "color")

  "Dog" should {
    "be found at location 1, and be red" in withQueryResult(
      zooSchema :+ ("dog", 1, "Scooby", null.asInstanceOf[Boolean], "red")) {
        conn ⇒
          atLocation(conn)(1) aka "animal" must beSome(Dog("Scooby", "red"))
    }
  }

  "Ostrich" should {
    "be found at location 2" in {
      val conn = connection(handleQuery { _ ⇒
        zooSchema :+ ("bird", 2, "Ostrich", false, null.asInstanceOf[String])
      })

      atLocation(conn)(2) aka "animal" must beSome(Bird("Ostrich", false))
    }
  }
}
```

### JUnit

Acolyte can be used with JUnit to write test case for Java method accessing persistence:

```java
import java.util.List;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;

import static org.junit.Assert.*;

import acolyte.AbstractCompositeHandler.QueryHandler;
import acolyte.StatementHandler.Parameter;
import acolyte.QueryResult;
import acolyte.RowList5;

import static acolyte.RowList.Column;
import static acolyte.RowLists.rowList5;
import static acolyte.Rows.row5;

@org.junit.runner.RunWith(org.junit.runners.JUnit4.class)
public class ZooTest {
    private RowList5<String,Integer,String,Boolean,String> zooSchema =
        rowList5(Column(String.class, "type"),
                 Column(Integer.class, "location"),
                 Column(String.class, "name"),
                 Column(Boolean.class, "fly"),
                 Column(String.class, "color"));

    @org.junit.Test
    public void dogAtLocation() {
        final String handlerId = "dogTest";
        acolyte.Driver.
            register(handlerId, acolyte.CompositeHandler.empty().
                     withQueryDetection("^SELECT").
                     withQueryHandler(new QueryHandler() {
                             public QueryResult apply(String sql, List<Parameter> parameters) throws SQLException {
                                 return zooSchema.
                                     append("dog", 1, "Scooby", null, "red").
                                     asResult();

                             }
                         }));

        final String jdbcUrl = 
            "jdbc:acolyte:anything-you-want?handler=" + handlerId;

        try {
            final Zoo zoo = new Zoo(DriverManager.getConnection(jdbcUrl));
            
            org.junit.Assert.assertEquals("Dog should be found at location 1", 
                                          zoo.atLocation(1), 
                                          new Dog("Scooby", "red"));

        } catch (Exception e) {
            org.junit.Assert.fail(e.getMessage());
        }
    }

    @org.junit.Test
    public void birdAtLocation() {
        final Connection conn = acolyte.Driver.
            connection(acolyte.CompositeHandler.empty().
                     withQueryDetection("^SELECT").
                     withQueryHandler(new QueryHandler() {
                             public QueryResult apply(String sql, List<Parameter> parameters) throws SQLException {
                                 return zooSchema.
                                     append("bird", 2, "Ostrich", false, null)).
                                     asResult();

                             }
                         }));

        org.junit.Assert.assertEquals("Ostrich should be found at location 2", 
                                      new Zoo(conn).atLocation(2), 
                                      new Bird("Ostrich", false));

    }
}
```

Sample zoo method could be as following:

```java
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;

public class Zoo {
    private final Connection connection;

    public Zoo(Connection c) { this.connection = c; }

    public Animal atLocation(int id) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = this.connection.
                prepareStatement("SELECT * FROM zoo WHERE location = ?");

            stmt.setInt(1, id);

            rs = stmt.executeQuery();
            rs.next();

            final String type = rs.getString("type");
            
            if (type == "bird") {
                return new Bird(rs.getString("name"), rs.getBoolean("fly"));
            } else if (type == "dog") {
                return new Dog(rs.getString("name"), rs.getString("color"));
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Fails to locate animate");
        } finally {
            try { rs.close(); } catch (Exception e) {}
            try { stmt.close(); } catch (Exception e) {}
        }
    }
}

interface Animal {
    public String getName();
}
class Bird implements Animal {
    public final String name;
    public final boolean fly;

    public Bird(String n, boolean f) {
        this.name = n;
        this.fly = f;
    }

    public String getName() { return this.name; }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Bird)) {
            return false;
        } 

        final Bird other = (Bird) o;

        return (((this.name == null && other.name == null) ||
                 (this.name != null && this.name.equals(other.name))) &&
                this.fly == other.fly);

    }
}
class Dog implements Animal {
    public final String name;
    public final String color;

    public Dog(String n, String c) {
        this.name = n;
        this.color = c;
    }

    public String getName() { return this.name; }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Dog)) {
            return false;
        } 

        final Dog other = (Dog) o;

        return (((this.name == null && other.name == null) ||
                 (this.name != null && this.name.equals(other.name))) &&
                ((this.color == null && other.color == null) ||
                 (this.color != null && this.color.equals(other.color))));

    }
}
```

### Limitations

- Limited datatype conversions.
- Binary datatype are not currently supported.
- Pseudo-support for transaction.
- Currency types.

### Related applications

- [Acolyte Studio](./studio.html): Application with CLI and GUI which is useful when you already have a database and want tests to use data extracted from there.

## Build

Acolyte can be built from these sources using SBT (0.12.2+): `sbt publish`

## Documentation

Documentation is generated using Maven 3: `mvn -f site.xml site`

## Deploy

To local repository:

```
mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=jdbc-driver/target/jdbc-driver-$VERSION.pom -Dfile=jdbc-driver/target/jdbc-driver-$VERSION.jar -Durl=file://$REPOPATH

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=jdbc-scala/target/scala-2.10/jdbc-scala_2.10-$VERSION.pom -Dfile=jdbc-scala/target/scala-2.10/jdbc-scala_2.10-$VERSION.jar -Durl=file://$REPOPATH

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=scalac-plugin/target/scala-2.10/scalac-plugin_2.10-$VERSION.pom -Dfile=scalac-plugin/target/scala-2.10/scalac-plugin_2.10-$VERSION.jar -Durl=file://$REPOPATH
```

At Sonatype:

```
export REPO="https://oss.sonatype.org/service/local/staging/deploy/maven2/"
# or https://oss.sonatype.org/content/repositories/snapshots/

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=jdbc-driver/target/jdbc-driver-$VERSION.pom -Dfile=jdbc-driver/target/jdbc-driver-$VERSION.jar -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=jdbc-driver/target/jdbc-driver-$VERSION.pom -Dfile=jdbc-driver/target/jdbc-driver-$VERSION-javadoc.jar -Dclassifier=javadoc -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=jdbc-driver/target/jdbc-driver-$VERSION.pom -Dfile=jdbc-driver/target/jdbc-driver-$VERSION-sources.jar -Dclassifier=sources -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

export SCALAVER="2.10"

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=jdbc-scala/target/scala-$SCALAVER/jdbc-scala_$SCALAVER-$VERSION.pom -Dfile=jdbc-scala/target/scala-$SCALAVER/jdbc-scala_$SCALAVER-$VERSION.jar -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=jdbc-scala/target/scala-$SCALAVER/jdbc-scala_$SCALAVER-$VERSION.pom -Dfile=jdbc-scala/target/scala-$SCALAVER/jdbc-scala_$SCALAVER-$VERSION-javadoc.jar -Dclassifier=javadoc -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=jdbc-scala/target/scala-$SCALAVER/jdbc-scala_$SCALAVER-$VERSION.pom -Dfile=jdbc-scala/target/scala-$SCALAVER/jdbc-scala_$SCALAVER-$VERSION-sources.jar -Dclassifier=sources -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=scalac-plugin/target/scala-$SCALAVER/scalac-plugin_$SCALAVER-$VERSION.pom -Dfile=scalac-plugin/target/scala-$SCALAVER/scalac-plugin_$SCALAVER-$VERSION.jar -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=scalac-plugin/target/scala-$SCALAVER/scalac-plugin_$SCALAVER-$VERSION.pom -Dfile=scalac-plugin/target/scala-$SCALAVER/scalac-plugin_$SCALAVER-$VERSION-javadoc.jar -Dclassifier=javadoc -Durl=$REPO -DrepositoryId=sonatype-nexus-staging

mvn gpg:sign-and-deploy-file -Dkeyname=$KEY -DpomFile=scalac-plugin/target/scala-$SCALAVER/scalac-plugin_$SCALAVER-$VERSION.pom -Dfile=scalac-plugin/target/scala-$SCALAVER/scalac-plugin_$SCALAVER-$VERSION-sources.jar -Dclassifier=sources -Durl=$REPO -DrepositoryId=sonatype-nexus-staging
```

Authentication should be configured in `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <!-- ... -->

    <server>
      <id>sonatype-nexus-staging</id>
      <username>your-jira-id</username>
      <password>your-jira-pwd</password>
    </server>
  </servers>
  <!-- ... -->
</settings>
```
