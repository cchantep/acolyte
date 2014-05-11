# Frequently Asked Questions

## Why I need to specify a query detection pattern?

JDBC manages 2 kinds of statement: query statement that fetchs rows, or updates statement that update persistent data.

To supports this behaviour, to return rows when you consider statement to be a query, or update state/count when it should be an update, Acolyte should be taught how to detect these cases.

Using composite handler, you can specify it using detection pattern(s) (regular expressions):

```java
import acolyte.CompositeHandler;

CompositeHandler handler = CompositeHandler.empty().
  // Considers as query if starts with 'SELECT ' or contains 'EXEC fetch_data'
  withQueryDetection("^SELECT ", "EXEC fetch_data"). 
  // Variant with pre-compiled pattern
  withQueryDetection(Pattern.compile("ORDER BY test$"));
```

In previous example, handler is given different patterns. In such case, order in which patterns were given is used to check executed statement, until it matches at least one of them.

Equivalent with the Scala DSL is:

```scala
import acolyte.Acolyte.handleStatement

val handler = handleStatement.
  withQueryDetection("^SELECT ", "EXEC fetch_data"). 
  withQueryDetection(Pattern.compile("ORDER BY test$"))
```

With Scala DSL, if you want all statements to be considered as queries, you can use:

```scala
import acolyte.QueryExecution
import acolyte.Acolyte.handleQuery

val handler = handleQuery { e: QueryExecution =>
  // handleQueries
  queryResults
}
```

## What's the maximum number of column for a row?

Implementations of [Row](http://cchantep.github.io/acolyte/apidocs/acolyte/Row.html) and [RowList](http://cchantep.github.io/acolyte/apidocs/acolyte/RowList.html) are provided up to 52 columns.

## How to simply return a single scalar row?

If you just need to mockup a result containing only 1 row with 1 column, single column factories (`booleanList`, `byteList`, `intList`, `stringList`, ...) can be called, or in Scala single value can be directly used (e.g. `2` for a single row with only one int column).

## Why do I get an error like "overloaded method value rowListX with alternatives" from Scala compiler?

Example:
```
overloaded method value rowList6 with alternatives:
[error]   [A, B, C, D, E, F](x$1: acolyte.RowList.Column[A], x$2: acolyte.RowList.Column[B], x$3: acolyte.RowList.Column[C], x$4: acolyte.RowList.Column[D], x$5: acolyte.RowList.Column[E], x$6: acolyte.RowList.Column[F])acolyte.RowList6[A,B,C,D,E,F] <and>
[error]   [A, B, C, D, E, F](x$1: Class[A], x$2: Class[B], x$3: Class[C], x$4: Class[D], x$5: Class[E], x$6: Class[F])acolyte.RowList6[A,B,C,D,E,F]
[error]  cannot be applied to ((Class[String], String), (Class[String], String), (Class[String], String), (Class[String], String), (Class[String], String), (Class[String], String))
```

It occurs when `acolyte.Implicits.PairAsColumn` is missing while using 
pimped Scala syntax to declare row list:

```scala
import acolyte.RowLists.rowList6

// Corresponding to error example
// - will raise compilation error
rowList6(
  classOf[String] -> "col1", 
  classOf[String] -> "col2",
  classOf[String] -> "col3",
  classOf[String] -> "col4",
  classOf[String] -> "col5",
  classOf[String] -> "col6")
```

Proper import simply fixes that:

```scala
import acolyte.RowLists.rowList6
import acolyte.Implicits.PairAsColumn // or acolyte.Implicits._

rowList6( // Now it's ok
  classOf[String] -> "col1", 
  classOf[String] -> "col2",
  classOf[String] -> "col3",
  classOf[String] -> "col4",
  classOf[String] -> "col5",
  classOf[String] -> "col6")
```

## Why do I get error with null value in row?

As type can't always be inferred when using `null`, it can lead to ambiguity:

```
[error]  found   : Null(null)
[error]  required: Int
```

In such cases, [type of null](index.html#NULL_values) must be explicitly given.

## Can I re-use data from existing DB with Acolyte?

[Studio](./studio.html) can record results from DB, so they can be used later with Acolyte framework, either in Java or Scala.