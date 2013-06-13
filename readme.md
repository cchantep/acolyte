# Acolyte

Acolyte is a JDBC driver designed for cases like mockup, testing, or any case you would like to be able to handle JDBC query by hand (or maybe that's only Chmeee's son on the Ringworld).

[![Build Status](https://secure.travis-ci.org/cchantep/acolyte.png?branch=master)](http://travis-ci.org/cchantep/acolyte)

## Requirements

* Java 1.6+

## Usage

Acolyte can be used in SBT projects adding dependency `"cchantep" %% "acolyte" % "VERSION"` (coming on a repository).

### Code

Acolyte driver behaves as any other JDBC driver, that's to say you can get a connection from, by using the well-known `java.sql.DriverManager.getConnection(jdbcUrl)` (and its variants).

JDBC URL should match `"jdbc:acolyte:anything-you-want"`.

```java
import java.sql.DriverManager;
import java.sql.Connection;

import acolyte.RuleStatementHandler;
import acolyte.ConnectionHandler;
import acolyte.StatementHandler;

// Prepare handler
StatementHandler handler = new RuleStatementHandler().
  withQueryDetection("^SELECT "). // regex test from beginning
  withQueryDetection("EXEC that_proc"). // second detection regex
  withUpdateHandler(new RuleStatementHandler.UpdateHandler() {
    // Handle execution of update statement (not query)
    public int apply(String sql, ...) {
      // ...
      return count;
    }
  }).withQueryHandler(new QueryHandler () {
    public ResultSet apply(String sql, ...) {
      // ...

      // Prepare list of 2 rows
      // with 3 columns of types String, Float, Date
      RowList<Row3<String, Float, Date>> rows = 
        new RowList<Row3<String, Float, Date>>().
        append(rows3("str", 1.2f, d1)).
        append(rows3("val", 2.34f, d2));

      return rows.resultSet();
    }
  });

Connection con = DriverManager.getConnection("jdbc:acolyte:anything-you-want",
  acolyte.Driver.properties(handler);
```

### Limitations

- Binary datatype are not currently supported.
- Callable statement are not (yet) implemented.
- `ResultSet.RETURN_GENERATED_KEYS` is not supported.

## Build

Acolyte can be built from these sources using SBT (0.12.2+): `sbt publish`
