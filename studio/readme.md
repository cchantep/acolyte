# Acolyte Studio

Acolyte Studio can format database row as Acolyte Java or Scala syntax.
Thus data can be recorded from DB and replayed/used as fixtures in tests.

## Requirements

* Java 1.6+

## Graphical interface

PENDING

## Command-line interface

Row formatter can be called from CLI:

```
java -jar STUDIO.jar acolyte.RowFormatter <options>
```

Either configuration file is found at `$USER_HOME/.acolyte/studio.properties` and arguments are SQL query followed by column types, or arguments are:

1. JDBC URL,
2. path to JAR of JDBC driver,
3. name of database user,
4. user password,
5. character set of database,
6. type of first column,
7. type of second column,
8. ...

## Column types

- bigdecimal
- bool
- byte
- short
- date
- double
- float
- int
- long
- time
- timestamp
- string