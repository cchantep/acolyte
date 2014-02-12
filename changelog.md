## 1.0.13

([924ef57d29285ad49c8674d76ceee97630a25c1a](https://github.com/cchantep/acolyte/commit/924ef57d29285ad49c8674d76ceee97630a25c1a) @ [studio](https://github.com/cchantep/acolyte/tree/master/studio)) Allow Studio to connection without password (fix #14)

([965bc416990b54b0d9421c72af769bfe75e3f31e](https://github.com/cchantep/acolyte/commit/965bc416990b54b0d9421c72af769bfe75e3f31e) @ [studio](https://github.com/cchantep/acolyte/tree/master/studio)) Fix issue with driver at system classloader (overloading those in selected driver JAR)

([6a526416ca73ffee884ad1744f6c2f3376264781](https://github.com/cchantep/acolyte/commit/6a526416ca73ffee884ad1744f6c2f3376264781) @ [studio](https://github.com/cchantep/acolyte/tree/master/studio)) Fix row column cursor in formatting

## 1.0.12

([f33f9c82f4e64bc3b83c9f64ab82319b47af873e](https://github.com/cchantep/acolyte/commit/f33f9c82f4e64bc3b83c9f64ab82319b47af873e) @ [scala](https://github.com/cchantep/acolyte/tree/master/scala)) Single result case connection using `withQueryResult` in DSL:

```scala
import acolyte.Acolyte

// res: acolyte.QueryResult
val str: String = Acolyte.withQueryResult(res) { connection ⇒ … }
```

([38f0b52dc120488d1bae3a4e830a849958146fe3](https://github.com/cchantep/acolyte/commit/38f0b52dc120488d1bae3a4e830a849958146fe3) @ [scala](https://github.com/cchantep/acolyte/tree/master/scala)) Support nullable flag for column of row list

([4edb03ec7b48e89e3219fe898f667c63cdd356a8](https://github.com/cchantep/acolyte/commit/4edb03ec7b48e89e3219fe898f667c63cdd356a8) @ [core](https://github.com/cchantep/acolyte/tree/master/core)) Support nullable flag for meta-data of row column.

```java
import acolyte.RowList2;
import acolyte.RowLists;

RowList2.Impl<String,Float> l1 =
  RowLists.rowList2(String.class, Float.class).
  withNullable(1, true); // First column is nullable

import acolyte.RowList;
import acolyte.Column;

Column<String> meta1 = new Column<String>(String.class, "a", true);

RowList2.Impl<String,Float> l2 =
  RowLists.rowLists2(meta1, RowList.Column(Float.class, "b"));

import static acolyte.RowList.Column

RowList2.Impl<String,Float> l3 =
  RowLists.rowLists2(Column(String.class, "a").withNullable(true),
    Column(Float.class, "b"));
```

([c68afc6a732f50c4e8b7cea9628a335caf38583b](https://github.com/cchantep/acolyte/commit/c68afc6a732f50c4e8b7cea9628a335caf38583b) @ [core](https://github.com/cchantep/acolyte/tree/master/core)) Deprecates append-row operation on RowList, replaced by multivalue-append.
Prevents null inference issues.

```java
import acolyte.RowLists;
import acolyte.Rows;

// Before
RowLists.rowList2(String.class, Integer.class).
  append(Rows.row2("str", 1)); // now deprecated

RowLists.rowList2(String.class, Integer.class).
  append("str", 1);
```

([dd64b526794625a65c1ca06e12492e2a4f692bd9](https://github.com/cchantep/acolyte/commit/dd64b526794625a65c1ca06e12492e2a4f692bd9) @ [scala](https://github.com/cchantep/acolyte/tree/master/scala)) Refactor Scala RowList with self-types, for better inference and implicits (operation on ScalaRowList returning ScalaRowList, not core RowList which would be converted again on next operation).

```scala
// Before
val l1a: RowList1[String] = rowList1(classOf[String])
val l1b: RowList1[String] = l1a :+ "B" // convert on :+ ...
// but core type RowList1 is returned

// Now
val l1a = rowList1(classOf[String])
val l1b: ScalaRowList1[A] = l1a :+ "B" // convert on :+ ...
// and keep it as pimped Scala type
val l1c: ScalaRowList1[A] = l1b :+ "C" // no conversion required
```

Typesafe append operations are generated along with generated row lists, so append-row operation and related Row implicits are no longer required.

```scala
// Before
rowList3 :+ row3(a, b, c) // no supported due to inference issues

// Now
rowList3 :+ (a, b, c)
```

([b4d944aaee23afbeeb8aa09cd97c3741c83f7fd8](https://github.com/cchantep/acolyte/commit/b4d944aaee23afbeeb8aa09cd97c3741c83f7fd8) @ [core](https://github.com/cchantep/acolyte/tree/master/core)) Refactor RowList with self-type trick on append/labeling operation.

```java
// Before this change
RowList1<String> l1 = new RowList1<String>(String.class);
RowList<Row1<A>> l1updated = l1.append("str");
// Self-type RowList1<A> of l1 is lost in append operation

// Now
RowList1.Impl<String> l2 = new RowList1.Impl<String>(String.class);
RowList1.Impl<String> l2updated = l2.append("str");
// Implementation self-type is kept
```

([a447ac9436f4c020e6bb48a58f5e8d6558da9b75](https://github.com/cchantep/acolyte/commit/a447ac9436f4c020e6bb48a58f5e8d6558da9b75) @ [core](https://github.com/cchantep/acolyte/tree/master/core)) Fix row classes naming: move Row1 from acolyte.Row.Row1 up to acolyte.Row1.

([f87782144a691847cf4a390cced9a838cdfa2db8](https://github.com/cchantep/acolyte/commit/f87782144a691847cf4a390cced9a838cdfa2db8) @ [scala](https://github.com/cchantep/acolyte/tree/master/scala)) Fix inference of  on scala RowList1:

```scala
import acolyte.Implicits._
import acolyte.RowLists.stringList

stringList :+ null // fixed inference of null (as String there)
```

([2397517f22e7642851e10f490abd2d2f99a8301f](https://github.com/cchantep/acolyte/commit/2397517f22e7642851e10f490abd2d2f99a8301f) @ [core](https://github.com/cchantep/acolyte/tree/master/core)) Refactor RowList.Column.defineCol as RowList.Column:

```java
import static acolyte.RowList.Column;

Column(String.class, "colName");
```

## 1.0.11

([b079c0ca31f3006ce3b8e779f5e778319ce4ed72](https://github.com/cchantep/acolyte/commit/b079c0ca31f3006ce3b8e779f5e778319ce4ed72) @ [studio](https://github.com/cchantep/acolyte/tree/master/studio)) First GUI release

([d9d4e5c831916305c415708203d8c8ef6d6ba5ce](https://github.com/cchantep/acolyte/commit/d9d4e5c831916305c415708203d8c8ef6d6ba5ce) @ [core](https://github.com/cchantep/acolyte/tree/master/core)) Update groupId to publish artifacts on Central via Sonatype

([3cc594b2d3e3030e6b09c91b3384092723eb1020](https://github.com/cchantep/acolyte/commit/3cc594b2d3e3030e6b09c91b3384092723eb1020) @ [scala](https://github.com/cchantep/acolyte/tree/master/scala)) Update limit of row columns from 26 to 52

([d6e4d9a9033ede803ad863a4682ca63123ac7040](https://github.com/cchantep/acolyte/commit/d6e4d9a9033ede803ad863a4682ca63123ac7040) @ [core](https://github.com/cchantep/acolyte/tree/master/core)) Update limit of row columns from 26 to 52

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

