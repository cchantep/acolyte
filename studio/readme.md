# Acolyte Studio

Studio *record* results from DB, so they can be *replayed* with Acolyte framework.

Once connected to database, Acolyte Studio formats query result in Acolyte row syntax. Thus recorded rows can be used anytime you want as fixtures.

## Requirements

* Java 1.6+

## Download

- Platform independent: [standalone JAR](https://github.com/cchantep/acolyte/releases/download/1.0.11/acolyte-studio-1.0.11.jar)

## Graphical interface

![Acolyte Studio GUI](https://github.com/cchantep/acolyte/raw/master/studio/src/site/images/mockup1.png)

General use case is following one.

1. Configure JDBC access.
2. Prepare SQL query.
3. Test SQL query.
4. Define column mappings, then extract rows with that (using previous query).
5. Convert extracted rows to Acolyte syntax.

![Test result](https://github.com/cchantep/acolyte/raw/master/studio/src/site/images/mockup2.png) ![Conversion](https://github.com/cchantep/acolyte/raw/master/studio/src/site/images/mockup3.png)

## Command-line interface

Row formatter can be called from CLI:

```
java -jar STUDIO.jar acolyte.RowFormatter <arguments>
```

If second argument is a path to JDBC driver, then arguments are expected to be:

1. JDBC URL,
2. path to JAR of JDBC driver,
3. name of database user,
4. character set of database,
5. user password,
6. output format (Java or Scala),
7. SQL query,
8. type of first column,
9. type of second column,
10. ...

Otherwise, arguments #1 to #4 are omitted, values loaded from configuration file `$USER_HOME/.acolyte/studio.properties`:

1. User password,
2. output format (Java or Scala),
3. SQL query,
4. type of first column,
5. type of second column,
6. ...

### Column types

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

## Configuration file

File `studio.properties` uses Java properties syntax, with following keys:

- `jdbc.url`: JDBC URL
- `jdbc.driverPath`: Path to JDBC driver JAR
- `db.user`: Name of database user
- `db.charset`: Database character set
