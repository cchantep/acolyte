# Java

Acolyte JDBC can be used in Vanilla Java.

With Maven 2/3+, its dependency can be resolved in the following way:

```xml
<!-- ... -->
  <dependencies>
    <!-- ... -->
    <dependency>
      <groupId>org.eu.acolyte</groupId>
      <artifactId>jdbc-driver</artifactId>
      <version>VERSION</version>
    </dependency>
  </dependencies>
```

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.eu.acolyte/jdbc-driver/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.eu.acolyte/jdbc-driver/)

Then code could be:

```java
import java.util.List;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Date;

import acolyte.jdbc.ConnectionHandler;
import acolyte.jdbc.StatementHandler;
import acolyte.jdbc.CompositeHandler;
import acolyte.jdbc.RowList3;
import acolyte.jdbc.Result;

import acolyte.jdbc.StatementHandler.Parameter;

import static acolyte.jdbc.RowLists.rowList3;

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
acolyte.jdbc.Driver.register("my-unique-id", handler);

// then ...
Connection con = DriverManager.getConnection(jdbcUrl);

// ... Connection |con| is managed through |handler|
```

You can see more [use cases](https://github.com/cchantep/acolyte/blob/master/jdbc-driver/src/test/java/usecase/JavaUseCases.java) whose expectations are visible in [specifications](https://github.com/cchantep/acolyte/blob/master/jdbc-driver/src/test/jdbc-scala/acolyte/AcolyteSpec.scala).

*See online [API documentation](http://http://acolyte.eu.org/jdbc-driver-javadoc)*.

## Connection 

As soon as you register Acolyte handler with a unique ID, corresponding connection can be resolved using JDBC URL including this ID as parameter.

```java
// Register prepared handler with expected ID 'my-unique-id'
// handler: acolyte.jdbc.ConnectionHandler or acolyte.jdbc.StatementHandler instance
acolyte.jdbc.Driver.register("my-unique-id", handler);

// then ...
// ... later as handler has registered with 'my-unique-id'
final String jdbcUrl = "jdbc:acolyte:anything-you-want?handler=my-unique-id";

Connection con = DriverManager.getConnection(jdbcUrl);
// ... Connection |con| is managed through |handler|
```

If you just want to directly get connection from `acolyte.jdbc.Driver`, without using JDBC driver registry, you can use Acolyte direct connection:

```java
// handler: acolyte.jdbc.ConnectionHandler or acolyte.jdbc.StatementHandler instance
Connection con = acolyte.jdbc.Driver.connection(handler);
```

### Connection properties

JDBC allows to pass properties to driver to customize connection creation:

```java
Connection con = DriverManager.getConnection(jdbcUrl, someJavaUtilProps);
Connection con = acolyte.jdbc.Driver.connection(handler, someJavaUtilProps);
```

Acolyte specific properties are:

- `acolyte.parameter.untypedNull`: If `"true"`, Acolyte fallbacks untyped null from `statement.setObject(p, null)` to null string (default: false).
- `acolyte.batch.continueOnError`: If `"true"`, Acolyte doesn't stop executing batch on statement, but continue processing and finally throw `BatchUpdateException` with update counts of successfully executed elements (see [java.sql.Statement#executeBatch](http://docs.oracle.com/javase/7/docs/api/java/sql/Statement.html#executeBatch%28%29)).
- `acolyte.resultSet.initOnFirstRow`: If `"true"`, Acolyte will degrade JDBC compliance by positioning cursor of result sets initially on the first row, rather than before (as specified by [JDBC ResultSet class](https://docs.oracle.com/javase/7/docs/api/java/sql/ResultSet.html#next%28%29). It makes Acolyte behaves has Oracle JDBC driver.

## Query result creation

Acolyte provides [Row](http://acolyte.eu.org/jdbc-driver-javadoc/acolyte/jdbc/Row.html) and [RowList](http://acolyte.eu.org/jdbc-driver-javadoc/acolyte/jdbc/RowList.html) classes (and their sub-classes) to allow easy and typesafe creation of result.

Row lists can be built as following using [RowLists factory](http://acolyte.eu.org/jdbc-driver-javadoc/acolyte/jdbc/RowLists.html).

```java
import acolyte.jdbc.RowList1;
import acolyte.jdbc.RowList3;

import static acolyte.jdbc.RowLists.rowList1;
import static acolyte.jdbc.RowLists.rowList3; 

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

Both column classes and names can be declared in bulk way, using [definition class](http://acolyte.eu.org/jdbc-driver-javadoc/acolyte/jdbc/Column.html):

```java
import static acolyte.jdbc.RowList.Column;

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

import static acolyte.jdbc.Rows.row1;

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

[RowLists factory](http://acolyte.eu.org/jdbc-driver-javadoc/acolyte/jdbc/RowLists.html) also provide convinience constructor for single column row list:

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

## SQL Warnings

Acolyte can also mock up SQL warnings, on update or query, so that `java.sql.Statement.getWarnings()` will returned expected instance.

```java
import acolyte.jdbc.UpdateResult;
import acolyte.jdbc.QueryResult;

// ...

// Update results to be returned from an acolyte.jdbc.UpdateHandler
UpdateResult upNothingWarn = UpdateResult.Nothing.withWarning("Nothing");
UpdateResult up1ResWithWarn = UpdateResult.One.withWarning("Warning 1");
UpdateResult up10ResWithWarn = new UpdateResult(10).
  withWarning("updateCount = 10 with warning");

// Query result (wrapping row list) to be returned from acolyte.jdbc.QueryHandler
QueryResult nilWithWarning = QueryResult.Nil.withWarning("Nil with warning");
QueryResult resWithWarning = aRowList.asResult().
  withWarning("Row list result with warning");
```

## Generated keys

Update case not only returning update count but also generated keys can be represented with `UpdateResult`:

```java
import acolyte.jdbc.UpdateResult;
import acolyte.jdbc.RowLists;

// Result with update count == 1 and a generated key 2L
UpdateResult.One.withGeneratedKeys(RowLists.longList().append(2L));
```

Keys specified on result will be given to JDBC statement `.getGeneratedKeys()`.

## Java 8

The module `jdbc-java8` provides a JDBC DSL benefiting from Java 8 features.

```java
import acolyte.jdbc.Java8CompositeHandler;
import acolyte.jdbc.RowLists;
import acolyte.jdbc.RowList3;

import acolyte.jdbc.AcolyteDSL.handleStatement;
import acolyte.jdbc.AcolyteDSL.handleQuery2;
import acolyte.jdbc.AcolyteDSL.connection;

connection(handleQuery2((sql, params) -> true));

Java8CompositeHandler handler = handleStatement.
  withQueryDetection("^SELECT ", // regex test from beginning
                     "EXEC that_proc"). // second detection regex
  withUpdateHandler1((sql, ps) -> {
    if (sql.startsWith("DELETE ")) {
      /* Process deletion ... deleted = */ return 2;
    } else {
      /* ... Process ... count = */ return 1;
    }
  }).
  withQueryHandler((sql, ps) -> {
    if (sql.startsWith("SELECT ")) {
      return RowLists.rowList1(String.class).asResult();
    } else {
      // ... EXEC that_proc 
      // (see previous withQueryDetection)

      // Prepare list of 2 rows
      // with 3 columns of types String, Float, Date
      RowList3.Impl<String, Float, Date> rows =
        RowLists.rowList3(String.class, Float.class, Date.class).
                          // Optional: set labels
                          withLabel(1, "String").withLabel(3, "Date").
                          append("str", 1.2F, new Date(1l)).
                          append("val", 2.34F, null);

        return rows.asResult();
      }
    });

```

It can be added to your project with the following dependency.

```xml
<dependency>
  <groupId>org.eu.acolyte</groupId>
  <artifactId>jdbc-java8</artifactId>
  <version>VERSION-j7p</version>
</dependency>
```

> Note the suffix `-j7p` added at end of the version to get the Acolyte artifacts built as Java 1.7+ bytecode.

*See online [API documentation](http://acolyte.eu.org/jdbc-java8-javadoc)*.