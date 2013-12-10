# Frequently Asked Questions

## Why I need to specify a query detection pattern?

A database engine manages 2 kinds of statement, query statement that fetchs rows or updates statement that update persistent data.

To simulate this behaviour, to allow you return rows when you consider statement to be a query, or update state/count when it should be an update, Acolyte should be taught how to detect these cases.

Using composite handler, you can specify it using detection pattern(s) (regular expressions):

```java
import acolyte.CompositeHandler;

CompositeHandler handler = CompositeHandler.empty().
  // Considers as query if starts with 'SELECT ' or contains 'EXEC fetch_data'
  withQueryDetection("^SELECT ", "EXEC fetch_data"). 
  // Variant with pre-compiled pattern
  withQueryDetection(Pattern.compile("ORDER BY test$"));
```

In previous example, handler is given different patterns. In such case, order in which patterns were given is used to check executed statement, until it matches at least one of them, or they are all checked.

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

Implementations of [Row](http://cchantep.github.io/acolyte/apidocs/acolyte/Row.html) and [RowList](http://cchantep.github.io/acolyte/apidocs/acolyte/RowList.html) are provided up to 26 columns.