## 1.0.10

([500abb5493a1b6221b5e5c0f2fdf102a5bcf88ff](https://github.com/cchantep/acolyte/commit/500abb5493a1b6221b5e5c0f2fdf102a5bcf88ff) @ [scala](https://github.com/cchantep/acolyte/tree/master/scala)) Connection properties support:

```scala
import acolyte.Acolyte.connection

connection(handler, "acolyte.parameter.untypedNull" -> "true"/* ... */)
```

([3b7d9151ab1d09e994f3fbbcfd7bf0610343d4f4](https://github.com/cchantep/acolyte/commit/3b7d9151ab1d09e994f3fbbcfd7bf0610343d4f4) @ [core](https://github.com/cchantep/acolyte/tree/master/core)) Fallback for untyped null parameter, configured with connection property:

```java
import java.util.Properties;

Properties props = new Properties();
props.put("acolyte.parameter.untypedNull", "true"); // default: false

DriverManager.getConnection(jdbcUrl, props);
```

([1240c3956e91807a25bd6b8c2fcdfa8e745dd2f1](https://github.com/cchantep/acolyte/commit/1240c3956e91807a25bd6b8c2fcdfa8e745dd2f1) @ [core](https://github.com/cchantep/acolyte/tree/master/core)) Add factories for single column row lists,
to create a list with initial values:

```java
import acolyte.RowLists;
import acolyte.RowList1;

// Creates a row list with 3 rows "A", "B" and "C"
RowList1<String> list = RowLists.stringList("A", "B", "C");
```

([9d01cedde46f13cb1d9a2224732b2e96ba9314ad](https://github.com/cchantep/acolyte/commit/9d01cedde46f13cb1d9a2224732b2e96ba9314ad) @ [core](https://github.com/cchantep/acolyte/tree/master/core)) Supports max rows limit on statement (and row list):

```java
java.sql.PreparedStatement stmt = conn.prepareStatement("SQL");
stmt.setMaxRows(2);

stmt.execute();

// Will go through RowList.resultSet(2)
java.sql.ResultSet rs = stmt.getResultSet();
```

([44f9c1e4cb12ca18cd3584eed5ae16636ecb3b3e](https://github.com/cchantep/acolyte/commit/44f9c1e4cb12ca18cd3584eed5ae16636ecb3b3e) @ [core](https://github.com/cchantep/acolyte/tree/master/core)) Implements statement creation with supported result set
type, concurrency or holdability:

```java
conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);

conn.prepareStatement("SQL", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
conn.prepareStatement("SQL", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);

conn.prepareCall("SQL", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
conn.prepareCall("SQL", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
```

([334711bd91f361a1ff591609911f87dccd393c28](https://github.com/cchantep/acolyte/commit/334711bd91f361a1ff591609911f87dccd393c28) @ [scala](https://github.com/cchantep/acolyte/tree/master/scala)) Move implicit conversions appart (for cleaner imports):

```scala
import acolyte.Acolyte.{ connection, handleStatement/* ... */ } // DSL
import acolyte.Implicits._ // implicit conversions

val conn = connection(handleStatement { _ => list :+ row })
```

([18b94e06d565b35699250af7e7dabbe4c8df9ee3](https://github.com/cchantep/acolyte/commit/18b94e06d565b35699250af7e7dabbe4c8df9ee3) @ [scala](https://github.com/cchantep/acolyte/tree/master/scala)) Remove `.handleQuery` without argument from DSL, replaced by:

```scala
import acolyte.Acolyte._
import acolyte.{ QueryExecution, QueryResult }

handleQuery { e: QueryExecution => /* QueryResult */ }
```

([6b1c7602b58f2952f22e18c3408e273924572d91](https://github.com/cchantep/acolyte/commit/6b1c7602b58f2952f22e18c3408e273924572d91) @ [core](https://github.com/cchantep/acolyte/tree/master/core)) Empty composite handler: `acolyte.CompositeHandler.empty()`

([4e7c51b26586ff6b4049b4fd7b73447561dabd07](https://github.com/cchantep/acolyte/commit/4e7c51b26586ff6b4049b4fd7b73447561dabd07) @ [scala](https://github.com/cchantep/acolyte/tree/master/scala)) Update handleQuery:
```scala
import acolyte.Acolyte.handleQuery

handleQuery { e => res }
```

([e3536e8a6d10d72385cf8b4880e31a583d2a2f7a](https://github.com/cchantep/acolyte/commit/e3536e8a6d10d72385cf8b4880e31a583d2a2f7a) @ [scala](https://github.com/cchantep/acolyte/tree/master/scala)) Distinct execution case classes for update or query:

```scala
Acolyte.
  withQueryHandler({ e: QueryExecution => res }).
  withUpdateHandler({ e: UpdateExecution => res })
```

([3747ba9c82c25b5741ac3129a4e2fb410b5404c0](https://github.com/cchantep/acolyte/commit/3747ba9c82c25b5741ac3129a4e2fb410b5404c0) @ [core](https://github.com/cchantep/acolyte/tree/master/core)) Append operation, to directly append values to row list:

```java
RowList2<String,Integer> list = new RowList2<String,Integer>();
list.append("cell1", 2);
```

([764c22d87a590ec1785985e389386720f535e66a](https://github.com/cchantep/acolyte/commit/764c22d87a590ec1785985e389386720f535e66a) @ [scala](https://github.com/cchantep/acolyte/tree/master/scala)) Append operation, to add a single value as row in row list:

```scala
val list = RowLists.rowList1(classOf[String])
list :+ "row"
```

([18a2add4e33e7b5765e2af6d637c339343f89c2e](https://github.com/cchantep/acolyte/commit/18a2add4e33e7b5765e2af6d637c339343f89c2e) @ [core](https://github.com/cchantep/acolyte/tree/master/core)) Append operation, to add a single value as row in row list:

```java
RowList1<String> list = new RowList1<String>();
list.append("row");
```

([a7d52d131853936f4283c336058ff75e409251fa](https://github.com/cchantep/acolyte/commit/a7d52d131853936f4283c336058ff75e409251fa) @ [scala](https://github.com/cchantep/acolyte/tree/master/scala)) Supports `Execution => RowList[_]` as query handler:

```scala
Acolyte.withQueryHandler({ e: Execution => rowList })
```
