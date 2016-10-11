## 1.0.39 & 1.0.41

Fix the ReactiveMongo module

## 1.0.38

Update to ReactiveMongo 0.12-RC4

## 1.0.37

([b527a212302abda6c3943aad97a60c37676a2d58](https://github.com/cchantep/acolyte/commit/b527a212302abda6c3943aad97a60c37676a2d58) @ [play-jdbc](https://github.com/cchantep/acolyte/tree/master/play-jdbc)) New Play JDBC module (with DSL).

([2eb464aa7ad36a550d230c69074da2c24d21421b](https://github.com/cchantep/acolyte/commit/2eb464aa7ad36a550d230c69074da2c24d21421b) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactivemongo)) Update ReactiveMongo to 0.12-RC0.

## 1.0.36

([40bfcf737112b166f2d7c2d4d56791d15178b90f](https://github.com/cchantep/acolyte/commit/40bfcf737112b166f2d7c2d4d56791d15178b90f) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Set opaque parameter as JDBC OTHER.

## 1.0.35

([de0179bd758b93861fb9405869743005ec31ff09](https://github.com/cchantep/acolyte/commit/de0179bd758b93861fb9405869743005ec31ff09) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactivemongo)) Support ReactiveMongo 0.11.x.

([d60d07e1afb887e055c46f5d49ace4842a93946f](https://github.com/cchantep/acolyte/commit/d60d07e1afb887e055c46f5d49ace4842a93946f) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Better fetch-size support.

## 1.0.34

([4f6afc2928fa3dbb469a1a54f7c556fa3e245afa](https://github.com/cchantep/acolyte/commit/4f6afc2928fa3dbb469a1a54f7c556fa3e245afa) @ [jdbc-scala](https://github.com/cchantep/acolyte/tree/master/jdbc-scala)) New debuging utility in the AcolyteDSL.

```scala
AcolyteDSL.debuging() { con =>
  val stmt = con.prepareStatement("SELECT * FROM Test WHERE id = ?")
  stmt.setString(1, "foo")
  stmt.executeQuery()
}
```

## 1.0.33

([56ca676e19164a251801579397c2411a9a503505](https://github.com/cchantep/acolyte/commit/56ca676e19164a251801579397c2411a9a503505) @ [jdbc-java8](https://github.com/cchantep/acolyte/tree/master/jdbc-java8)) Acolyte DSL for JDBC.

([05d7fbaaf2d31c96afcfed9ddcdb101555d615ea](https://github.com/cchantep/acolyte/commit/05d7fbaaf2d31c96afcfed9ddcdb101555d615ea) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Add a new `RowList` factory for a single row with a single value.

```java
import static acolyte.jdbc.RowLists;

RowLists.scalar("Foo"); // Scalar RowList of String
```

([92f72ae4bb716c1bfb853be401dc4761e8c5f251](https://github.com/cchantep/acolyte/commit/92f72ae4bb716c1bfb853be401dc4761e8c5f251) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Add class `Driver.Property`.

```java
import acolyte.jdbc.Driver;
import acolyte.jdbc.Property;

Driver.connection(handler,
  new Property("name1, "Foo"),
  new Property("name2", "Bar"));
```

([a8ed90b4c44a38626bf7e1935bca692fdc152012](https://github.com/cchantep/acolyte/commit/a8ed90b4c44a38626bf7e1935bca692fdc152012) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Java 1.7+ support

## 1.0.32

([42a9ddfdccc65c175edf7d22cd677592843af0bf](https://github.com/cchantep/acolyte/commit/42a9ddfdccc65c175edf7d22cd677592843af0bf) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Add connection property `acolyte.resultSet.initOnFirstRow` to make Acolyte ResultSet iterates rows as degraded Oracle one.

## 1.0.31

([bd6763c3e1d513d05cfefbb887e20a672b1c71db](https://github.com/cchantep/acolyte/commit/bd6763c3e1d513d05cfefbb887e20a672b1c71db) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactivemongo)) Manage connection handler per actor, so that an single ActorSystem can manage several handlers. Documentation about driver manager with SBT or Specs2.

[6197ee32151ccfa03f75ce6dfef563ee39c9581b](https://github.com/cchantep/acolyte/commit/6197ee32151ccfa03f75ce6dfef563ee39c9581b) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactivemongo)) Add clause extractor `NotInClause(List(_))`.

## 1.0.30

([9b641b8e3b1252611a1e9d5376b84a2580ca8f42](https://github.com/cchantep/acolyte/commit/9b641b8e3b1252611a1e9d5376b84a2580ca8f42) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactivemongo)) Write request convenient extractors

## 1.0.29

Minor refactoring.

## 1.0.28

([ca0470eb13f5dd9a6a083fa26f2665303f96ffdb](https://github.com/cchantep/acolyte/commit/ca0470eb13f5dd9a6a083fa26f2665303f96ffdb) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Support for SQL Array as statement parameter

([ccc0485199c8081b17b40d008983984e4d23dba0](https://github.com/cchantep/acolyte/commit/ccc0485199c8081b17b40d008983984e4d23dba0) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactivemongo)) Update project doc

## 1.0.27

([c94cd5883bbef746273c5245885d5e7d22e2e8a3](https://github.com/cchantep/acolyte/commit/c94cd5883bbef746273c5245885d5e7d22e2e8a3) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactivemongo)) Pattern matching for request body with multiple document

([f5e58c0dbf83e3f067af8081a39575550657115a](https://github.com/cchantep/acolyte/commit/f5e58c0dbf83e3f067af8081a39575550657115a) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactivemongo)) $in extractor

([d213d6df9c7c4f158f9699f1027c363ddbafbfc8](https://github.com/cchantep/acolyte/commit/d213d6df9c7c4f158f9699f1027c363ddbafbfc8) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactivemongo)) Refactor Request pattern matching

([8fe2ef491a7601ab1916838c92c5277b983db62c](https://github.com/cchantep/acolyte/commit/8fe2ef491a7601ab1916838c92c5277b983db62c) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactivemongo)) Fix scaladoc

([b663965aa94b91a6e573ac760a1ba3e20ea6fb0b](https://github.com/cchantep/acolyte/commit/b663965aa94b91a6e573ac760a1ba3e20ea6fb0b) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactivemongo)) BSON array extractor

## 1.0.26

([646c8f57fccf605d97e2318adebb4b2540d5514b](https://github.com/cchantep/acolyte/commit/646c8f57fccf605d97e2318adebb4b2540d5514b) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactivemongo)) Refactor DSL and better support for Count command.

([cb826da6c11d9eac28a9e7a392423fc28aae5a2b](https://github.com/cchantep/acolyte/commit/cb826da6c11d9eac28a9e7a392423fc28aae5a2b) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactivemongo)) Count response.

## 1.0.25

([919d248bff3afd5549e240e4d871b86a32d6f3db](https://github.com/cchantep/acolyte/commit/919d248bff3afd5549e240e4d871b86a32d6f3db) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactive-mongo)) Fix doc

([406cc26a029d868d74c5fd5f7e28305e31e088fc](https://github.com/cchantep/acolyte/commit/406cc26a029d868d74c5fd5f7e28305e31e088fc) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactive-mongo)) Complete driver specs

([d0f067434324650f09199be1b88a91c5c329aea2](https://github.com/cchantep/acolyte/commit/d0f067434324650f09199be1b88a91c5c329aea2) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactive-mongo)) Load patterns for Mongo resources

([f925d4ffcd64388836d6da252b3c13b3bff32aac](https://github.com/cchantep/acolyte/commit/f925d4ffcd64388836d6da252b3c13b3bff32aac) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactive-mongo)) Convenient response maker for write operation. More specs for Driver/AcolyteDSL, update documentation.

([14b59bc5a92637d97e60c2f2a884f4aa7037a2ef](https://github.com/cchantep/acolyte/commit/14b59bc5a92637d97e60c2f2a884f4aa7037a2ef) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactive-mongo)) Disable Akka reverse engineering, only use Acolyte actor system

([11832214f443bb84a54d4397aee4a68818737da5](https://github.com/cchantep/acolyte/commit/11832214f443bb84a54d4397aee4a68818737da5) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactive-mongo)) Driver specification

([23517d9db23b4e815205196166fd57a1fcff7351](https://github.com/cchantep/acolyte/commit/23517d9db23b4e815205196166fd57a1fcff7351) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactive-mongo)) Update count in WriteResponse

([1f714141517f5dc547a30482cf50c1ce1f60d3f7](https://github.com/cchantep/acolyte/commit/1f714141517f5dc547a30482cf50c1ce1f60d3f7) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactive-mongo)) Specs (connection handler), add optional code in query error

([66cc12f36f0127598f72c2c9c2cc59d01d1ba9d2](https://github.com/cchantep/acolyte/commit/66cc12f36f0127598f72c2c9c2cc59d01d1ba9d2) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactive-mongo)) Write response & handler specs

([357c67663436dfa73f82e2b329c4fb7e559b447d](https://github.com/cchantep/acolyte/commit/357c67663436dfa73f82e2b329c4fb7e559b447d) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactive-mongo)) Documentation for pattern matching on write operator

([04355e9b1ae25c53960c49b53e842c40d8028d7a](https://github.com/cchantep/acolyte/commit/04355e9b1ae25c53960c49b53e842c40d8028d7a) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactive-mongo)) Typeclasses WriteResponseMaker, WriteResponse and associated handler types.

([95e66462f6c225a84be1b69732a15385f61261c4](https://github.com/cchantep/acolyte/commit/95e66462f6c225a84be1b69732a15385f61261c4) @ [reactivemongo](https://github.com/cchantep/acolyte/tree/master/reactive-mongo)) Fallback functions for write errors

## 1.0.23 to 1.0.24

([1d8160dd6b26fcf964fce51096f3043994069fab](https://github.com/cchantep/acolyte/commit/1d8160dd6b26fcf964fce51096f3043994069fab) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Support for ResultSet type/scrollability

([0942aaefa304f7ef13e5260b779e2f51d65bb4be](https://github.com/cchantep/acolyte/commit/0942aaefa304f7ef13e5260b779e2f51d65bb4be) @ [scalac-plugin](https://github.com/cchantep/acolyte/tree/master/scalac-plugin)) Recursive support `~(A(...), ~(B(...), (x, y)))`. Add specifications for pattern matching in `val` statement. Update documentation.

([d6d2a26b601fd76d09b6c626f87998f28489cd0e](https://github.com/cchantep/acolyte/commit/d6d2a26b601fd76d09b6c626f87998f28489cd0e) @ [scalac-plugin](https://github.com/cchantep/acolyte/tree/master/scalac-plugin)) Support application other than a single value or tuple-like one as bindings:

```scala
str match {
  case str @ ~(IndexOf('/'), a :: b :: c :: _) ⇒ ??? // Support :: application
  case _ ⇒ ???
}
```

([193d3bf1118dcd617cdd6bc915a597b2c550c5a1](https://github.com/cchantep/acolyte/commit/193d3bf1118dcd617cdd6bc915a597b2c550c5a1) @ [scalac-plugin](https://github.com/cchantep/acolyte/tree/master/scalac-plugin)) Fix compatibility warning between 2.10 & 2.11

([e9c5c4777cdfa00ea5a3dfe88cf9ac7bfc8f242f](https://github.com/cchantep/acolyte/commit/e9c5c4777cdfa00ea5a3dfe88cf9ac7bfc8f242f) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Remove obselete JDBC classes from base package  (previously moved to `acolyte.jdbc`).

([194d549fca55e35ea07c175f0f12edda1c79a85a](https://github.com/cchantep/acolyte/commit/194d549fca55e35ea07c175f0f12edda1c79a85a) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Support for binary types (BLOB, byte array, input stream)

([43257c1db69ceb1cf9f31c0856903255924cb880](https://github.com/cchantep/acolyte/commit/43257c1db69ceb1cf9f31c0856903255924cb880) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Fix wasNull operation on result

([c849e2489f9e66a96760d800d5a72becf5d870e9](https://github.com/cchantep/acolyte/commit/c849e2489f9e66a96760d800d5a72becf5d870e9) @ [scalac-plugin](https://github.com/cchantep/acolyte/tree/master/scalac-plugin)) Support for literal with rich match:

```scala
str match {
  ~(RegEx("# ([a-z]+): .*"), "start") => /* Start */ ???
  ~(RegEx("# ([a-z]+): .*"), "stop")  => /* Stop */ ???
}
```

## 1.0.22

([a07e7306cab7b2e4eac1d4a39c90789e42065655](https://github.com/cchantep/acolyte/commit/a07e7306cab7b2e4eac1d4a39c90789e42065655) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Support for binary types (BLOB, byte array, input stream)

([43257c1db69ceb1cf9f31c0856903255924cb880](https://github.com/cchantep/acolyte/commit/43257c1db69ceb1cf9f31c0856903255924cb880) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Fix wasNull operation on result

([c849e2489f9e66a96760d800d5a72becf5d870e9](https://github.com/cchantep/acolyte/commit/c849e2489f9e66a96760d800d5a72becf5d870e9) @ [scalac-plugin](https://github.com/cchantep/acolyte/tree/master/scalac-plugin)) Support for literal with rich match:

```scala
str match {
  ~(RegEx("# ([a-z]+): .*"), "start") => /* Start */ ???
  ~(RegEx("# ([a-z]+): .*"), "stop")  => /* Stop */ ???
}
```

## 1.0.21

([9c261f107338c4a9d4364d89540f8965cb46c71f](https://github.com/cchantep/acolyte/commit/9c261f107338c4a9d4364d89540f8965cb46c71f) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Moves classes from package `acolyte` to `acolyte.jdbc`. Classes in former package are deprecated, and will be removed in future release.

([210e6d17602af672fe4154b11d1009580cce01a3](https://github.com/cchantep/acolyte/commit/210e6d17602af672fe4154b11d1009580cce01a3) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Array support in query result:

```java
import acolyte.RowLists;
import acolyte.ImmutableArray;

// List of row with 1 column,
// whose type is array of string
RowLists.rowList1(java.sql.Array.class).
  append(ImmutableArray.getInstance(String.class,
    new String[] { "Ab", "Cd", "Ef" }));
```

## 1.0.20

([210e6d17602af672fe4154b11d1009580cce01a3](https://github.com/cchantep/acolyte/commit/210e6d17602af672fe4154b11d1009580cce01a3) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Array support in query result:

```java
import acolyte.RowLists;
import acolyte.ImmutableArray;

// List of row with 1 column,
// whose type is array of string
RowLists.rowList1(java.sql.Array.class).
  append(ImmutableArray.getInstance(String.class,
    new String[] { "Ab", "Cd", "Ef" }));
```

## 1.0.19

([28fff28e89368a64d4882efd7517abebc88c3edd](https://github.com/cchantep/acolyte/commit/28fff28e89368a64d4882efd7517abebc88c3edd) @ [scalac-plugin](https://github.com/cchantep/acolyte/tree/master/scalac-plugin)) Refactor recursive match:

```scala
x match {
  case y => y match {
    case ~(RichPat("x"), _) => ???
  }
}
```

([f4acc2dc143f0f584b9142c096e0194a91ad8bb9](https://github.com/cchantep/acolyte/commit/f4acc2dc143f0f584b9142c096e0194a91ad8bb9) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Make warning of query result accessible on raised ResultSet

## 1.0.18

([ac3415cabc01f85de1d766c0de53f0ab7e8c6c38](https://github.com/cchantep/acolyte/commit/ac3415cabc01f85de1d766c0de53f0ab7e8c6c38) @ [jdbc-scala](https://github.com/cchantep/acolyte/tree/master/jdbc-scala)) Companion function for update result with generated keys.

```scala
import acolyte.{ Acolyte, RowLists }

val res = Acolyte.updateResult(3/*count*/, RowLists.stringList("generatedKey"))
```

([99ac2f1d01681b01f2c236268061d498ba1217c1](https://github.com/cchantep/acolyte/commit/99ac2f1d01681b01f2c236268061d498ba1217c1) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Fix generated keys on update result.

## 1.0.17

([363aab2f67d2d7866626d750f442b61d4c728223](https://github.com/cchantep/acolyte/commit/363aab2f67d2d7866626d750f442b61d4c728223) @ [jdbc-scala](https://github.com/cchantep/acolyte/tree/master/jdbc-scala)) Cross build for Scala 2.11.

## 1.0.16

([a0122044eb1125fe66899582c1eb1df617cf8351](https://github.com/cchantep/acolyte/commit/a0122044eb1125fe66899582c1eb1df617cf8351) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Fix parameter definition for null decimal/numeric (1.0.16-2).

([bf89ecec5b9b1645c347c32cf334475e39e4155c](https://github.com/cchantep/acolyte/commit/bf89ecec5b9b1645c347c32cf334475e39e4155c) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Simplified exception message (1.0.16-1).

([2a2093606f5c4582efa2fea4aa18de357f4b8a1c](https://github.com/cchantep/acolyte/commit/2a2093606f5c4582efa2fea4aa18de357f4b8a1c) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Batch support on statement and prepared statement.

([ce8a913be91b04759903e8dab843baf41cad7b86](https://github.com/cchantep/acolyte/commit/ce8a913be91b04759903e8dab843baf41cad7b86) @ [jdbc-driver](https://github.com/cchantep/acolyte/tree/master/jdbc-driver)) Generated keys support for statement:

```java
import acolyte.UpdateResult;
import acolyte.RowLists;

UpdateResult.One.withGeneratedKeys(RowLists.intList().append(4));
// to be returned from an update handler:
// update count = 1 and one generated key = 4
```

## 1.0.15

([65a7e3313ab5681b77cba502c0aa945ef645f68c](https://github.com/cchantep/acolyte/commit/65a7e3313ab5681b77cba502c0aa945ef645f68c) @ [jdbc-scala](https://github.com/cchantep/acolyte/tree/master/jdbc-scala)) Regex extractor for executed statement, usable with rich pattern:

```scala
e/* : QueryExecution */ match {
  case ~(ExecutedStatement("^SELECT"), (sql, parameters)) => // ...
}
```

([7bc9b546b3a3d05008b8ace84f3d2e47ba18d367](https://github.com/cchantep/acolyte/commit/7bc9b546b3a3d05008b8ace84f3d2e47ba18d367) @ [scalac-plugin](https://github.com/cchantep/acolyte/tree/master/scalac-plugin)) Support binding over rich pattern matching (e.g. `case binding @ ~(..., ...) => ...`; [Fix #20](https://github.com/cchantep/acolyte/issues/20))

## 1.0.14

([9b813586190f6e5f8e7c3fdcde642bcc2234e1a4](https://github.com/cchantep/acolyte/commit/9b813586190f6e5f8e7c3fdcde642bcc2234e1a4) @ [scalac-plugin](https://github.com/cchantep/acolyte/tree/master/scalac-plugin)) Documentation

([b5994bd1773f63e7c04eccc2b24b6378c10b8f4d](https://github.com/cchantep/acolyte/commit/b5994bd1773f63e7c04eccc2b24b6378c10b8f4d) @ [scalac-plugin](https://github.com/cchantep/acolyte/tree/master/scalac-plugin)) Match component specs

([88aaedde8f2d6a90bcc3e012810701f72eac3781](https://github.com/cchantep/acolyte/commit/88aaedde8f2d6a90bcc3e012810701f72eac3781) @ [scalac-plugin](https://github.com/cchantep/acolyte/tree/master/scalac-plugin)) Add MatchPlugin (rich pattern matching)

([4b64595e37b5c2d70761d1a7c1307f06fdc771e6](https://github.com/cchantep/acolyte/commit/4b64595e37b5c2d70761d1a7c1307f06fdc771e6) @ [studio](https://github.com/cchantep/acolyte/tree/master/studio)) Fix display issue in extract table

([654575844c3386a5471c01cf2c1710ab9a29d7e4](https://github.com/cchantep/acolyte/commit/654575844c3386a5471c01cf2c1710ab9a29d7e4) @ [studio](https://github.com/cchantep/acolyte/tree/master/studio)) Fix bigdecimal issue

([c33c9b88cb58b3cce07d06f3b161eeb8fc9ff3d0](https://github.com/cchantep/acolyte/commit/c33c9b88cb58b3cce07d06f3b161eeb8fc9ff3d0) @ [studio](https://github.com/cchantep/acolyte/tree/master/studio)) Expert editor for column mappings.

## 1.0.13

([924ef57d29285ad49c8674d76ceee97630a25c1a](https://github.com/cchantep/acolyte/commit/924ef57d29285ad49c8674d76ceee97630a25c1a) @ [studio](https://github.com/cchantep/acolyte/tree/master/studio)) Allow Studio to connection without password (Fix #14)

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

