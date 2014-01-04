# Acolyte Studio

Studio *record* results from DB, so they can be *replayed* with Acolyte framework.

Once connected to database, Acolyte Studio formats query result in Acolyte row syntax. Thus recorded rows can be used anytime you want as fixtures.

## Requirements

* Java 1.6+

## Download

PENDING

## Graphical interface

PENDING

## Command-line interface

Row formatter can be called from CLI:

```
java -jar STUDIO.jar acolyte.RowFormatter <options>
```

Either configuration file is found at `$USER_HOME/.acolyte/studio.properties` and arguments are SQL query followed by column types (after there, from #7), or arguments are:

1. JDBC URL,
2. path to JAR of JDBC driver,
3. name of database user,
4. user password,
5. character set of database,
6. output format (Java or Scala),
7. SQL query,
8. type of first column,
9. type of second column,
10. ...

## Column types

- bigdecimal: `java.math.BigDecimal`
- bool: `boolean`
- byte: `byte`
- short: `short`
- date: `java.sql.Date`
- double: `double`
- float: `float`
- int: `int`
- long: `long`
- time: `java.sql.Time`
- timestamp: `java.sql.Timestamp`
- string: `String`