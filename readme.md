# Acolyte

Acolyte is a JDBC driver designed for cases like mockup, testing, or any case you would like to be able to handle JDBC query by hand (or maybe that's only Chmeee's son on the Ringworld).

[![Build Status](https://secure.travis-ci.org/cchantep/acolyte.png?branch=master)](http://travis-ci.org/cchantep/acolyte)

## Requirements

* Java 1.6+

## Usage

Acolyte can be used in SBT projects adding dependency `"cchantep" %% "acolyte" % "VERSION"` (coming on a repository).

### Java code

Acolyte driver behaves as any other JDBC driver, that's to say you can get a connection from, by using the well-known `java.sql.DriverManager.getConnection(jdbcUrl)` (and its variants).

JDBC URL should match `"jdbc:acolyte:anything-you-want?handler=id"` (see after for details about `handler` parameter).

```java
import java.sql.DriverManager;
import java.sql.Connection;

import acolyte.CompositeHandler;
import acolyte.ConnectionHandler;
import acolyte.StatementHandler;

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
    public int apply(String sql, ...) {
      // ...
      return count;
    }
  }).withQueryHandler(new CompositeHandler.QueryHandler () {
    public ResultSet apply(String sql, ...) {
      // ...

      // Prepare list of 2 rows
      // with 3 columns of types String, Float, Date
      RowList<Row3<String, Float, Date>> rows = 
        new RowList<Row3<String, Float, Date>>().
        withLabel(1, "String").withLabel(3, "Date"). // Optional: set labels
        append(rows3("str", 1.2f, d1)).
        append(rows3("val", 2.34f, d2));

      return rows.resultSet();
    }
  });

// Register prepared handler with expected ID 'my-unique-id'
acolyte.Driver.register("my-unique-id", handler);

// then ...
Connection con = DriverManager.getConnection(jdbcUrl);

// ... Connection |con| is managed through |handler|
```

You can see tested/detailed [use cases](./src/test/java/acolyte/JavaUseCases.java).

### Limitations

- Binary datatype are not currently supported.
- Callable statement are not (yet) implemented.
- `ResultSet.RETURN_GENERATED_KEYS` is not supported.
- Pseudo-support for transaction.

## Build

Acolyte can be built from these sources using SBT (0.12.2+): `sbt publish`
