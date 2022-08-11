package acolyte.jdbc

import java.io.ByteArrayInputStream

import java.math.{ BigDecimal => JBigDec }

import java.sql.{
  Date,
  ResultSet,
  ResultSetMetaData,
  SQLException,
  Time,
  Timestamp,
  Types
}

import scala.reflect.ClassTag

import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragment

import acolyte.jdbc.RowList.{ Column => Col }
import acolyte.jdbc.test.Params
import org.apache.commons.io.IOUtils.contentEquals

object RowListSpec extends Specification with RowListTest {
  "Row list".title

  "Creation" should {
    "not accept null list" in {
      new RowList1.Impl(
        classOf[String],
        null,
        new java.util.HashMap[String, Integer](),
        new java.util.HashMap[Integer, java.lang.Boolean]()
      ).aka("ctor") must throwA[IllegalArgumentException](
        message = "Invalid rows"
      )
    }

    "not accept null map" in {
      new RowList2.Impl(
        classOf[String],
        classOf[Float],
        new java.util.ArrayList[Row2[String, Float]](),
        null,
        new java.util.HashMap[Integer, java.lang.Boolean]()
      ).aka("ctor") must throwA[IllegalArgumentException](
        message = "Invalid names"
      )
    }

    "not accept null nullables" in {
      new RowList2.Impl(
        classOf[String],
        classOf[Float],
        new java.util.ArrayList[Row2[String, Float]](),
        new java.util.HashMap[String, Integer](),
        null
      ).aka("ctor") must throwA[IllegalArgumentException](
        message = "Invalid nullable flags"
      )
    }
  }

  "Factory" should {
    "create list with given classes" in {
      RowLists.rowList2(classOf[String], classOf[Int]) aka "list" must beLike {
        case list =>
          val cs = list.getColumnClasses

          (cs.get(0) -> cs.get(1)) aka "col classes" must beTypedEqualTo(
            classOf[String] -> classOf[Int]
          )
      }
    }

    lazy val meta = (Col(classOf[String], "a"), Col(classOf[Int], "b"))

    "create list with given labels" in {
      RowLists.rowList2(meta._1, meta._2).aka("list") must beLike {
        case list =>
          val cs = list.getColumnClasses
          val ls = list.getColumnLabels

          ((cs.get(0) -> cs.get(1)) aka "column classes" must beTypedEqualTo(
            classOf[String] -> classOf[Int]
          )) and {
            (ls.get("a") -> ls.get("b")) aka "labels" must beEqualTo(1 -> 2)
          }
      }
    }

    "create list with given meta-data" in {
      RowLists.rowList2(meta._1, meta._2.withNullable(true)) must beLike {
        case list =>
          val cs = list.getColumnClasses
          val ls = list.getColumnLabels
          val ns = list.getColumnNullables

          ((cs.get(0) -> cs.get(1)) aka "col classes" must beTypedEqualTo {
            classOf[String] -> classOf[Int]
          }) and {
            (ls.get("a") -> ls.get("b")) aka "labels" must beEqualTo(1 -> 2)
          } and {
            (ns.get(1), ns.get(2)) aka "nullables" must beEqualTo(false -> true)
          }
      }
    }

    "create resultset using projection" >> {
      lazy val list = RowLists
        .rowList2(meta._1, meta._2.withNullable(true))
        .append("Foo", 1)
        .append("Bar", 2)

      def spec(res: ResultSet) = {
        val meta = res.getMetaData

        meta.getColumnCount must_=== 1 and {
          meta.getColumnName(1) must_=== "b"
        } and {
          meta.isNullable(1) must_=== java.sql.ResultSetMetaData.columnNullable
        } and {
          res.next() aka "has row #1" must beTrue
        } and {
          res.getInt(1) aka "row #1" must_=== 1
        } and {
          res.next() aka "has row #2" must beTrue
        } and {
          res.getInt(1) aka "row #2" must_=== 2
        } and {
          res.next() aka "has row #3" must beFalse
        }
      }

      "according the specified column names" in {
        spec(list.resultSet.withProjection(Array("b")))
      }

      "according the specified column indexes" in {
        spec(list.resultSet.withProjection(Array(2)))
      }
    }
  }

  "Result set metadata" should {
    lazy val meta = RowLists
      .rowList3(classOf[Float], classOf[String], classOf[Time])
      .withLabel(2, "title")
      .withNullable(1, false)
      .withNullable(3, true)
      .append(1.23F, "str", new Time(System.currentTimeMillis()))
      .resultSet
      .getMetaData

    "have expected column catalog" in {
      meta.getCatalogName(1) must_=== ""
    }

    "have expected column schema" in {
      meta.getSchemaName(1) must_=== ""
    }

    "have expected column table" in {
      meta.getTableName(1) must_=== ""
    }

    "have expected column count" in {
      meta.getColumnCount must_=== 3
    }

    "have expected class" >> {
      "for column #1" in {
        meta.getColumnClassName(1) must_=== classOf[Float].getName
      }

      "for column #2" in {
        meta.getColumnClassName(2) must_=== classOf[String].getName
      }

      "for column #3" in {
        meta.getColumnClassName(3) must_=== classOf[Time].getName
      }
    }

    "have expected display size" in {
      meta.getColumnDisplaySize(1) must_=== Integer.MAX_VALUE
    }

    "have expected label" >> {
      "for column #1" in {
        meta.getColumnName(1) aka "name" must beNull and {
          meta.getColumnLabel(1) aka "label" must beNull
        }
      }

      "for column #2" in {
        (meta.getColumnName(2) aka "name" must_=== "title")
          .and(meta.getColumnLabel(2) aka "label" must_=== "title")
      }

      "for column #3" in {
        (meta.getColumnName(3) aka "name" must beNull)
          .and(meta.getColumnLabel(3) aka "label" must beNull)

      }
    }

    "have expected column sign" >> {
      "for column #1" in {
        meta.isSigned(1) aka "signed" must beTrue
      }

      "for column #2" in {
        meta.isSigned(2) aka "signed" must beFalse
      }

      "for column #3" in {
        meta.isSigned(3) aka "signed" must beFalse
      }
    }

    "have expected nullable flag" >> {
      import ResultSetMetaData.{
        columnNoNulls,
        columnNullable,
        columnNullableUnknown
      }

      "for column #1" in {
        meta.isNullable(1) must_=== columnNoNulls
      }

      "for column #2" in {
        meta.isNullable(2) must_=== columnNullableUnknown
      }

      "for column #3" in {
        meta.isNullable(3) must_=== columnNullable
      }
    }

    "not support currency" in {
      meta.isCurrency(1) aka "currency" must beFalse
    }

    "have expected precision" >> {
      "for column #1" in {
        meta.getPrecision(1) aka "precision" must_=== 32
      }

      "for column #2" in {
        meta.getPrecision(2) aka "precision" must_=== 0
      }

      "for column #3" in {
        meta.getPrecision(3) aka "precision" must_=== 0
      }
    }

    "have expected scale" >> {
      "for column #1" in {
        meta.getScale(1) aka "scale" must_=== 2
      }

      "for column #2" in {
        meta.getScale(2) aka "scale" must_=== 0
      }

      "for column #3" in {
        meta.getScale(3) aka "scale" must_=== 0
      }
    }

    "have expected type" >> {
      "for column #1" in {
        (meta.getColumnType(1) aka "type" must_=== Types.FLOAT)
          .and(meta.getColumnTypeName(1) aka "type name" must_=== "FLOAT")
      }

      "for column #2" in {
        (meta.getColumnType(2) aka "type" must_=== Types.VARCHAR)
          .and(meta.getColumnTypeName(2) aka "type name" must_=== "VARCHAR")
      }

      "for column #3" in {
        (meta.getColumnType(3) aka "type" must_=== Types.TIME)
          .and(meta.getColumnTypeName(3) aka "type name" must_=== "TIME")
      }
    }

    "have expected flags" in {
      (meta.isSearchable(1) aka "searchable" must beTrue)
        .and(meta.isCaseSensitive(1) aka "case sensitive" must beTrue)
        .and(meta.isAutoIncrement(1) aka "auto-increment" must beFalse)
        .and(meta.isReadOnly(1) aka "read-only" must beTrue)
        .and(meta.isWritable(1) aka "writable" must beFalse)
        .and(
          meta.isDefinitelyWritable(1).aka("definitely writable") must beFalse
        )

    }
  }

  "Column classes" should {
    "be String" in {
      val cs = {
        val l = new java.util.ArrayList[Class[_]]
        l.add(classOf[String])
        l
      }

      RowLists.rowList1(classOf[String]).getColumnClasses must_=== cs
    }

    "be String, Double, Date" in {
      val cs = {
        val l = new java.util.ArrayList[Class[_]]
        l.add(classOf[String])
        l.add(classOf[Double])
        l.add(classOf[java.util.Date])
        l
      }

      RowLists
        .rowList3(classOf[String], classOf[Double], classOf[java.util.Date])
        .getColumnClasses must_=== cs

    }
  }

  "Result set statement" should {
    "be null" in {
      (new RowList1.Impl(
        classOf[String]
      ).resultSet.getStatement aka "statement" must beNull).and(
        new RowList1.Impl(classOf[String]).resultSet
          .withStatement(null)
          .getStatement aka "statement" must beNull
      )

    }

    "be expected one" in {
      val url = "jdbc:acolyte:test"
      val ch = test.EmptyConnectionHandler
      lazy val con = new acolyte.jdbc.Connection(url, null, ch)
      lazy val s = new AbstractStatement(con, ch.getStatementHandler) {}

      new RowList1.Impl(classOf[String]).resultSet
        .withStatement(s)
        .getStatement aka "statement" must_=== s

    }
  }

  "Single column row list" should {
    "accept string value" in {
      val rs = RowLists.stringList.append("strval").resultSet

      (rs.getFetchSize aka "size" must_=== 1)
        .and(rs.next aka "has row" must beTrue)
        .and(rs.getString(1) aka "single col" must_=== "strval")
    }

    "be created with initial string values" in {
      val rs = RowLists.stringList("A", "B", "C").resultSet

      (rs.getFetchSize aka "size" must_=== 3)
        .and(rs.next aka "has row #1" must beTrue)
        .and(rs.getString(1) aka "single col #1" must_=== "A")
        .and(rs.next aka "has row #2" must beTrue)
        .and(rs.getString(1) aka "single col #2" must_=== "B")
        .and(rs.next aka "has row #3" must beTrue)
        .and(rs.getString(1) aka "single col #3" must_=== "C")

    }

    "accept binary value" in {
      val bytes = Array[Byte](11, 100, 9)
      val rs = RowLists.binaryList.append(bytes).resultSet
      def stream = new ByteArrayInputStream(bytes)

      (rs.getFetchSize aka "size" must_=== 1) and (rs.next aka "has row" must beTrue) and (rs
        .getBytes(1) aka "single byte array" must_=== bytes) and (contentEquals(
        rs.getBlob(1).getBinaryStream,
        stream
      ).aka("expected blob") must beTrue) and (contentEquals(
        rs.getBinaryStream(1),
        stream
      ).aka("single stream") must beTrue)
    }

    "be created with initial binary values" in {
      val (a, b) = (Array[Byte](1) -> Array[Byte](2))
      val rs = RowLists.binaryList(a, b).resultSet

      (rs.getFetchSize aka "size" must_=== 2)
        .and(rs.next aka "has row #1" must beTrue)
        .and(rs.getBytes(1) aka "single col #1" must_=== a)
        .and(rs.next aka "has row #2" must beTrue)
        .and(rs.getBytes(1) aka "single col #2" must_=== b)

    }

    "accept blob value" in {
      val bytes = Array[Byte](3, 7, 11)
      val blob = new Blob(bytes)
      def stream = new ByteArrayInputStream(bytes)
      val rs = RowLists.blobList.append(blob).resultSet

      (rs.getFetchSize aka "size" must_=== 1) and (rs.next aka "has row" must beTrue) and (rs
        .getBlob(1) aka "single blob" must_=== blob) and (rs.getBytes(
        1
      ) aka "single byte array" must_=== bytes) and (contentEquals(
        stream,
        rs.getBinaryStream(1)
      ).aka("single stream") must beTrue)
    }

    "be created with initial blob values" in {
      val (a, b) = (Blob.Nil -> Blob.Nil)
      b.setBytes(0, Array[Byte](1, 2, 3))

      val rs = RowLists.blobList(a, b).resultSet

      (rs.getFetchSize aka "size" must_=== 2)
        .and(rs.next aka "has row #1" must beTrue)
        .and(rs.getBlob(1) aka "single col #1" must_=== a)
        .and(rs.next aka "has row #2" must beTrue)
        .and(rs.getBlob(1) aka "single col #2" must_=== b)

    }

    "accept boolean value" in {
      val rs = RowLists.booleanList.append(false).resultSet

      (rs.getFetchSize aka "size" must_=== 1)
        .and(rs.next aka "has row" must beTrue)
        .and(rs.getBoolean(1) aka "single col" must beFalse)
    }

    "be created with initial boolean values" in {
      val rs = RowLists.booleanList(true, true, false).resultSet

      (rs.getFetchSize aka "size" must_=== 3)
        .and(rs.next aka "has row #1" must beTrue)
        .and(rs.getBoolean(1) aka "single col #1" must beTrue)
        .and(rs.next aka "has row #2" must beTrue)
        .and(rs.getBoolean(1) aka "single col #2" must beTrue)
        .and(rs.next aka "has row #3" must beTrue)
        .and(rs.getBoolean(1) aka "single col #3" must beFalse)

    }

    "accept byte value" in {
      val rs = RowLists.byteList.append(2.toByte).resultSet

      (rs.getFetchSize aka "size" must_=== 1)
        .and(rs.next aka "has row" must beTrue)
        .and(rs.getByte(1) aka "single col" must_=== 2)
    }

    "be created with initial byte values" in {
      val rs = RowLists.byteList(1.toByte, 2.toByte, 3.toByte).resultSet

      (rs.getFetchSize aka "size" must_=== 3)
        .and(rs.next aka "has row #1" must beTrue)
        .and(rs.getByte(1) aka "single col #1" must_=== 1)
        .and(rs.next aka "has row #2" must beTrue)
        .and(rs.getByte(1) aka "single col #2" must_=== 2)
        .and(rs.next aka "has row #3" must beTrue)
        .and(rs.getByte(1) aka "single col #3" must_=== 3)

    }

    "accept short value" in {
      val rs = RowLists.shortList.append(3.toShort).resultSet

      (rs.getFetchSize aka "size" must_=== 1)
        .and(rs.next aka "has row" must beTrue)
        .and(rs.getShort(1) aka "single col" must_=== 3)
    }

    "be created with initial short values" in {
      val rs = RowLists.shortList(1.toShort, 2.toShort, 3.toShort).resultSet

      (rs.getFetchSize aka "size" must_=== 3)
        .and(rs.next aka "has row #1" must beTrue)
        .and(rs.getShort(1) aka "single col #1" must_=== 1)
        .and(rs.next aka "has row #2" must beTrue)
        .and(rs.getShort(1) aka "single col #2" must_=== 2)
        .and(rs.next aka "has row #3" must beTrue)
        .and(rs.getShort(1) aka "single col #3" must_=== 3)

    }

    "accept int value" in {
      val rs = RowLists.intList.append(4).resultSet

      (rs.getFetchSize aka "size" must_=== 1)
        .and(rs.next aka "has row" must beTrue)
        .and(rs.getInt(1) aka "single col" must_=== 4)
    }

    "be created with initial int values" in {
      val rs = RowLists.intList(1, 2, 3).resultSet

      (rs.getFetchSize aka "size" must_=== 3)
        .and(rs.next aka "has row #1" must beTrue)
        .and(rs.getInt(1) aka "single col #1" must_=== 1)
        .and(rs.next aka "has row #2" must beTrue)
        .and(rs.getInt(1) aka "single col #2" must_=== 2)
        .and(rs.next aka "has row #3" must beTrue)
        .and(rs.getInt(1) aka "single col #3" must_=== 3)

    }

    "accept stream value" in {
      val bytes = Array[Byte](3, 5, 7)
      def stream = new ByteArrayInputStream(bytes)
      val rs = RowLists.streamList.append(stream).resultSet

      rs.getFetchSize aka "size" must_=== 1 and (rs.next aka "has row" must beTrue) and (contentEquals(
        rs.getBinaryStream(1),
        stream
      ).aka("expected stream") must beTrue) and (rs.getBytes(
        1
      ) aka "single col bytes" must_=== bytes) and (contentEquals(
        rs.getBlob(1).getBinaryStream,
        stream
      )
        aka ("expected blob") must beTrue)
    }

    "be created with initial stream values" in {
      val a = new ByteArrayInputStream(Array[Byte](3, 5, 7))
      val b = new ByteArrayInputStream(Array[Byte](5, 7, 8))
      val c = new ByteArrayInputStream(Array[Byte](3, 9, 10))
      val rs = RowLists.streamList(a, b, c).resultSet

      (rs.getFetchSize aka "size" must_=== 3)
        .and(rs.next aka "has row #1" must beTrue)
        .and(rs.getBinaryStream(1) aka "single col #1" must_=== a)
        .and(rs.next aka "has row #2" must beTrue)
        .and(rs.getBinaryStream(1) aka "single col #2" must_=== b)
        .and(rs.next aka "has row #3" must beTrue)
        .and(rs.getBinaryStream(1) aka "single col #3" must_=== c)

    }

    "accept long value" in {
      val rs = RowLists.longList.append(5L).resultSet

      (rs.getFetchSize aka "size" must_=== 1)
        .and(rs.next aka "has row" must beTrue)
        .and(rs.getLong(1) aka "single col" must_=== 5L)
    }

    "be created with initial long values" in {
      val rs = RowLists.longList(1L, 2L, 3L).resultSet

      (rs.getFetchSize aka "size" must_=== 3)
        .and(rs.next aka "has row #1" must beTrue)
        .and(rs.getLong(1) aka "single col #1" must_=== 1L)
        .and(rs.next aka "has row #2" must beTrue)
        .and(rs.getLong(1) aka "single col #2" must_=== 2L)
        .and(rs.next aka "has row #3" must beTrue)
        .and(rs.getLong(1) aka "single col #3" must_=== 3L)

    }

    "accept float value" in {
      val rs = RowLists.floatList.append(6.7F).resultSet

      (rs.getFetchSize aka "size" must_=== 1)
        .and(rs.next aka "has row" must beTrue)
        .and(rs.getFloat(1) aka "single col" must_=== 6.7F)
    }

    "be created with initial float values" in {
      val rs = RowLists.floatList(1F, 2F, 3F).resultSet

      (rs.getFetchSize aka "size" must_=== 3)
        .and(rs.next aka "has row #1" must beTrue)
        .and(rs.getFloat(1) aka "single col #1" must_=== 1F)
        .and(rs.next aka "has row #2" must beTrue)
        .and(rs.getFloat(1) aka "single col #2" must_=== 2F)
        .and(rs.next aka "has row #3" must beTrue)
        .and(rs.getFloat(1) aka "single col #3" must_=== 3F)

    }

    "accept double value" in {
      val rs = RowLists.doubleList.append(7.89D).resultSet

      (rs.getFetchSize aka "size" must_=== 1)
        .and(rs.next aka "has row" must beTrue)
        .and(rs.getDouble(1) aka "single col" must_=== 7.89D)
    }

    "be created with initial double values" in {
      val rs = RowLists.doubleList(1D, 2D, 3D).resultSet

      (rs.getFetchSize aka "size" must_=== 3)
        .and(rs.next aka "has row #1" must beTrue)
        .and(rs.getDouble(1) aka "single col #1" must_=== 1D)
        .and(rs.next aka "has row #2" must beTrue)
        .and(rs.getDouble(1) aka "single col #2" must_=== 2D)
        .and(rs.next aka "has row #3" must beTrue)
        .and(rs.getDouble(1) aka "single col #3" must_=== 3D)

    }

    "accept big decimal" in {
      val bigdec = new java.math.BigDecimal(1.234)
      val rs = RowLists.bigDecimalList.append(bigdec).resultSet

      (rs.getFetchSize aka "size" must_=== 1)
        .and(rs.next aka "has row" must beTrue)
        .and(rs.getBigDecimal(1) aka "single col" must_=== bigdec)
    }

    "be created with initial big decimal values" in {
      val (a, b, c) = (
        new java.math.BigDecimal(1.2),
        new java.math.BigDecimal(23.4),
        new java.math.BigDecimal(4.567)
      )

      val rs = RowLists.bigDecimalList(a, b, c).resultSet

      (rs.getFetchSize aka "size" must_=== 3)
        .and(rs.next aka "has row #1" must beTrue)
        .and(rs.getBigDecimal(1) aka "single col #1" must_=== a)
        .and(rs.next aka "has row #2" must beTrue)
        .and(rs.getBigDecimal(1) aka "single col #2" must_=== b)
        .and(rs.next aka "has row #3" must beTrue)
        .and(rs.getBigDecimal(1) aka "single col #3" must_=== c)

    }

    "accept date" in {
      val d = new Date(System.currentTimeMillis())
      val rs = RowLists.dateList.append(d).resultSet

      (rs.getFetchSize aka "size" must_=== 1)
        .and(rs.next aka "has row" must beTrue)
        .and(rs.getDate(1) aka "single col" must_=== d)
    }

    "be created with initial date values" in {
      val (a, b, c) = (
        new Date(System.currentTimeMillis()),
        new Date(System.currentTimeMillis() + 100L),
        new Date(System.currentTimeMillis() + 1000L)
      )

      val rs = RowLists.dateList(a, b, c).resultSet

      (rs.getFetchSize aka "size" must_=== 3)
        .and(rs.next aka "has row #1" must beTrue)
        .and(rs.getDate(1) aka "single col #1" must_=== a)
        .and(rs.next aka "has row #2" must beTrue)
        .and(rs.getDate(1) aka "single col #2" must_=== b)
        .and(rs.next aka "has row #3" must beTrue)
        .and(rs.getDate(1) aka "single col #3" must_=== c)

    }

    "accept time" in {
      val t = new Time(System.currentTimeMillis())
      val rs = RowLists.timeList.append(t).resultSet

      (rs.getFetchSize aka "size" must_=== 1)
        .and(rs.next aka "has row" must beTrue)
        .and(rs.getTime(1) aka "single col" must_=== t)
    }

    "be created with initial time values" in {
      val (a, b, c) = (
        new Time(System.currentTimeMillis()),
        new Time(System.currentTimeMillis() + 100L),
        new Time(System.currentTimeMillis() + 1000L)
      )

      val rs = RowLists.timeList(a, b, c).resultSet

      (rs.getFetchSize aka "size" must_=== 3)
        .and(rs.next aka "has row #1" must beTrue)
        .and(rs.getTime(1) aka "single col #1" must_=== a)
        .and(rs.next aka "has row #2" must beTrue)
        .and(rs.getTime(1) aka "single col #2" must_=== b)
        .and(rs.next aka "has row #3" must beTrue)
        .and(rs.getTime(1) aka "single col #3" must_=== c)

    }

    "accept timestamp" in {
      val ts = new Timestamp(1234L)
      val rs = RowLists.timestampList.append(ts).resultSet

      (rs.getFetchSize aka "size" must_=== 1)
        .and(rs.next aka "has row" must beTrue)
        .and(rs.getTimestamp(1) aka "single col" must_=== ts)
    }

    "be created with initial timestamp values" in {
      val (a, b, c) = (
        new Timestamp(System.currentTimeMillis()),
        new Timestamp(System.currentTimeMillis() + 100L),
        new Timestamp(System.currentTimeMillis() + 1000L)
      )

      val rs = RowLists.timestampList(a, b, c).resultSet

      (rs.getFetchSize aka "size" must_=== 3)
        .and(rs.next aka "has row #1" must beTrue)
        .and(rs.getTimestamp(1) aka "single col #1" must_=== a)
        .and(rs.next aka "has row #2" must beTrue)
        .and(rs.getTimestamp(1) aka "single col #2" must_=== b)
        .and(rs.next aka "has row #3" must beTrue)
        .and(rs.getTimestamp(1) aka "single col #3" must_=== c)

    }
  }

  "Result set fetch size" should {
    "be 1" in {
      (new RowList1.Impl(classOf[String])
        .append("str")
        .resultSet
        .getFetchSize aka "size" must_=== 1).and(
        new RowList2.Impl(classOf[String], classOf[Float])
          .append("str", 1.23F)
          .resultSet
          .getFetchSize aka "size" must_=== 1
      )

    }

    "be 2" in {
      (new RowList1.Impl(classOf[String])
        .append("a")
        .append("b")
        .resultSet
        .getFetchSize aka "size" must_=== 2)

    }

    "be updated to 4" in {
      lazy val rows = RowLists.intList(10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0)
      val rs = rows.resultSet()
      rs.setFetchSize(4)

      rs aka "resultset" must_=== RowLists.intList(10, 9, 8, 7).resultSet
    }

    "not be updated on closed result set" in {
      val rs = RowLists.intList(1).resultSet
      rs.close()

      rs.setFetchSize(1) must throwA[SQLException]("Result set is closed")
    }
  }

  "Max rows limit" should {
    lazy val rows = RowLists.intList(10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0)

    "drop half of rows" in {
      rows.resultSet(5) aka "resultset" must_=== {
        RowLists.intList(10, 9, 8, 7, 6).resultSet
      }
    }

    "drop 2 rows at end (max = 8)" in {
      rows.resultSet(8) aka "resultset" must_=== (RowLists
        .intList(10, 9, 8, 7, 6, 5, 4, 3)
        .resultSet)
    }

    "extract only 3 rows" in {
      rows.resultSet(3) aka "resultset" must_=== (RowLists
        .intList(10, 9, 8)
        .resultSet)
    }

    "get all rows" in {
      rows.resultSet(11) aka "resultset" must_=== rows.resultSet and (rows
        .resultSet(0) must_=== rows.resultSet) and (rows
        .resultSet() must_=== rows.resultSet)
    }
  }

  "Object column by index" should {
    "not be read when not on a row" in {
      lazy val typemap = null.asInstanceOf[java.util.Map[String, Class[_]]]
      lazy val rs = new RowList1.Impl(classOf[String])
        .withLabel(1, "n")
        .append("str")
        .resultSet

      (rs.getObject(1) aka "getObject" must throwA[SQLException](
        message = "Not on a row"
      )).and(
        rs.getObject("n") aka "getObject" must throwA[SQLException](
          message = "Not on a row"
        )
      ).and(
        rs.getObject(1, typemap) aka "getObject" must throwA[SQLException](
          message = "Not on a row"
        )
      ).and(
        rs.getObject("n", typemap).aka("getObject") must throwA[SQLException](
          "Not on a row"
        )
      ).and(
        rs.getObject(1, classOf[String])
          .aka("getObject") must throwA[SQLException]("Not on a row")
      ).and(
        rs.getObject("n", classOf[String])
          .aka("getObject") must throwA[SQLException]("Not on a row")
      )

    }

    "be read on first row without `next()` call if option 'acolyte.resultSet.initOnFirstRow' is used" in {
      val url = "jdbc:acolyte:test"
      lazy val sh = new StatementHandler {
        def isQuery(s: String) = true
        def whenSQLUpdate(s: String, p: Params) = UpdateResult.Nothing
        def whenSQLQuery(s: String, p: Params) = {
          RowLists
            .rowList1(classOf[String])
            .append("Foo")
            .append("Bar")
            .asResult
        }
      }
      val ch = new ConnectionHandler.Default(sh)
      val props = new java.util.Properties()
      props.put("acolyte.resultSet.initOnFirstRow", "true")
      lazy val con = new acolyte.jdbc.Connection(url, props, ch)
      lazy val st = con.prepareStatement("SELECT * FROM Test")
      lazy val rs = {
        st.execute()
        st.getResultSet
      }

      rs.getFetchSize aka "fetch size" must_=== 2 and {
        rs.getObject(1) aka "first row" must_=== "Foo"
      } and {
        rs.next aka "has second row" must beTrue
      } and {
        rs.getObject(1) aka "second row" must_=== "Bar"
      } and {
        rs.next aka "has third row" must beFalse
      }
    }

    "be expected one" in {
      val value = 123L
      lazy val rs = (new RowList1.Impl(classOf[Long]).append(value)).resultSet
      rs.next

      rs.getObject(1) must beEqualTo(value)
    }

    "be null" in {
      lazy val rs = new RowList1.Impl(classOf[java.lang.Float])
        .withLabel(1, "n")
        .append(null.asInstanceOf[java.lang.Float])
        .resultSet

      rs.next

      (rs.getObject(1) aka "cell1" must beNull)
        .and(rs.getObject("n") aka "cell1" must beNull)
        .and(rs.getObject(1, classOf[String]) aka "cell1" must beNull)
        .and(rs.getObject("n", classOf[Float]) aka "cell1" must_=== 0.0F)

    }

    "not be read with invalid index" in {
      lazy val rs = new RowList1.Impl(classOf[Long]).append(123L).resultSet
      rs.next

      rs.getObject(2) aka "getObject" must throwA[SQLException](
        message = "Invalid column index: 2"
      )

    }

    "not convert without type" in {
      lazy val rs = new RowList1.Impl(classOf[String])
        .withLabel(1, "n")
        .append("str")
        .resultSet
      rs.next

      (rs
        .getObject(1, null.asInstanceOf[Class[Object]])
        .aka("byIndex") must throwA[SQLException]("Invalid type")).and(
        rs.getObject("n", null.asInstanceOf[Class[Object]])
          .aka("byLabel") must throwA[SQLException]("Invalid type")
      )

    }

    "not convert incompatible type" in {
      lazy val rs = new RowList1.Impl(classOf[String])
        .withLabel(1, "n")
        .append("str")
        .resultSet
      rs.next

      (rs.getObject(1, classOf[Date]).aka("byIndex") must throwA[SQLException](
        "Incompatible type"
      )).and(
        rs.getObject("n", classOf[Date])
          .aka("byLabel") must throwA[SQLException]("Incompatible type")
      )

    }

    "convert compatible type" in {
      val d = new Date(System.currentTimeMillis())
      lazy val rs =
        new RowList1.Impl(classOf[Date]).withLabel(1, "n").append(d).resultSet
      rs.next

      (rs.getObject(1, classOf[java.util.Date]) aka "byIndex" must_=== d).and(
        rs.getObject("n", classOf[java.util.Date]).aka("byLabel") must_=== d
      )

    }

    "convert compatible temporal type" in {
      val d = new Date(System.currentTimeMillis())
      lazy val t = new Time(d.getTime)
      lazy val ts = new Timestamp(d.getTime)
      lazy val rs =
        new RowList1.Impl(classOf[Date]).withLabel(1, "n").append(d).resultSet
      rs.next

      (rs.getObject(1, classOf[Time]) aka "byIndex" must_=== t)
        .and(rs.getObject("n", classOf[Timestamp]).aka("byLabel") must_=== ts)

    }

    "convert compatible numeric type" in {
      lazy val rs =
        new RowList1.Impl(classOf[Int]).withLabel(1, "n").append(1).resultSet
      rs.next

      (rs
        .getObject(1, classOf[java.lang.Float])
        .aka("byIndex") must_=== 1F).and(
        rs.getObject("n", classOf[java.lang.Double]).aka("byLabel") must_=== 1D
      )

    }
  }

  "Object column by name" should {
    "not be read when not on a row" in {
      new RowList1.Impl(classOf[String])
        .withLabel(1, "n")
        .append("str")
        .resultSet
        .getObject("n") aka "getObject" must throwA[SQLException](
        message = "Not on a row"
      )

    }

    "be read on first row without `next()` call if option 'acolyte.resultSet.initOnFirstRow' is used" in {
      val url = "jdbc:acolyte:test"
      lazy val sh = new StatementHandler {
        def isQuery(s: String) = true
        def whenSQLUpdate(s: String, p: Params) = UpdateResult.Nothing
        def whenSQLQuery(s: String, p: Params) = {
          RowLists
            .rowList1(Col(classOf[String], "colStr"))
            .append("Foo")
            .append("Bar")
            .asResult
        }
      }
      val ch = new ConnectionHandler.Default(sh)
      val props = new java.util.Properties()
      props.put("acolyte.resultSet.initOnFirstRow", "true")
      lazy val con = new acolyte.jdbc.Connection(url, props, ch)
      lazy val st = con.prepareStatement("SELECT * FROM Test")
      lazy val rs = {
        st.execute()
        st.getResultSet
      }

      rs.getFetchSize aka "fetch size" must_=== 2 and (rs.getObject(
        "colStr"
      ) aka "first row" must_=== "Foo") and (rs.next aka "has second row" must beTrue) and (rs
        .getObject(
          "colStr"
        ) aka "second row" must_=== "Bar") and (rs.next aka "has third row" must beFalse)
    }

    "be expected one" in {
      val value = 123L
      lazy val rs = (new RowList1.Impl(classOf[Long])
        .withLabel(1, "l")
        .append(value))
        .resultSet
      rs.next

      rs.getObject("l") must beEqualTo(value)
    }

    "be null" in {
      lazy val rs = new RowList1.Impl(classOf[java.lang.Float])
        .withLabel(1, "name")
        .append(null.asInstanceOf[java.lang.Float])
        .resultSet

      rs.next

      rs.getObject("name") aka "cell1" must beNull
    }

    "not be read with invalid name" in {
      lazy val rs = new RowList1.Impl(classOf[Long])
        .withLabel(1, "n")
        .append(123L)
        .resultSet
      rs.next

      (rs.getObject(null).aka("getObject") must throwA[SQLException](
        "Invalid label: null"
      )).and(
        rs.getObject("label").aka("getObject") must throwA[SQLException](
          "Invalid label: label"
        )
      )
    }

    "not be read with invalid name mapping" in {
      lazy val rs = new RowList1.Impl(classOf[Long])
        .withLabel(0, "before")
        .withLabel(2, "after")
        .append(123L)
        .resultSet
      rs.next

      (rs.getLong("before") aka "get" must throwA[SQLException](
        message = "Invalid column index: 0"
      )).and(
        rs.getLong("after") aka "get" must throwA[SQLException](
          message = "Invalid column index: 2"
        )
      )
    }
  }

  "String column from result set" should {
    "not be read by index when not on a row" in {
      (new RowList1.Impl(classOf[String])
        .append("str")
        .resultSet
        .getString(1) aka "getString" must throwA[SQLException](
        message = "Not on a row"
      )).and(
        new RowList1.Impl(classOf[String])
          .withLabel(1, "n")
          .append("str")
          .resultSet
          .getString("n")
          .aka("getString") must throwA[SQLException]("Not on a row")
      )

    }

    "be expected one" in {
      val rs = new RowList1.Impl(classOf[String])
        .withLabel(1, "n")
        .append("str")
        .resultSet
      rs.next

      (rs.getString(1) aka "string by index" must_=== "str")
        .and(rs.getString("n") aka "string by name" must_=== "str")
    }

    "be null" in {
      val rs = (new RowList1.Impl(classOf[String])
        .withLabel(1, "n")
        .append(null.asInstanceOf[String]))
        .resultSet

      rs.next

      (rs.getString(1) aka "boolean" must beNull)
        .and(rs.getString("n") aka "boolean" must beNull)
    }
  }

  "Boolean column from result set" should {
    "not be read by index when not on a row" in {
      (new RowList1.Impl(classOf[Boolean])
        .append(true)
        .resultSet
        .getBoolean(1) aka "getBoolean" must throwA[SQLException](
        message = "Not on a row"
      )).and(
        new RowList1.Impl(classOf[Boolean])
          .withLabel(1, "n")
          .append(false)
          .resultSet
          .getBoolean("n")
          .aka("getBoolean") must throwA[SQLException]("Not on a row")
      )

    }

    "be expected one" in {
      val rs = new RowList1.Impl(classOf[Boolean])
        .withLabel(1, "n")
        .append(true)
        .resultSet
      rs.next

      (rs.getBoolean(1) aka "boolean by index" must beTrue)
        .and(rs.getBoolean("n") aka "boolean by name" must beTrue)
    }

    "be null (false)" in {
      val rs = new RowList1.Impl(classOf[Boolean])
        .withLabel(1, "n")
        .append(null.asInstanceOf[Boolean])
        .resultSet

      rs.next

      (rs.getBoolean(1) aka "boolean" must beFalse)
        .and(rs.getBoolean("n") aka "boolean" must beFalse)
    }

    "converted to false by index" >> {
      val rs = Seq[(String, ResultSet)](
        "char" -> new RowList1.Impl(classOf[Char]).append('0').resultSet,
        "byte" -> new RowList1.Impl(classOf[Byte]).append(0.toByte).resultSet,
        "short" -> new RowList1.Impl(classOf[Short])
          .append(0.toShort)
          .resultSet,
        "int" -> new RowList1.Impl(classOf[Int]).append(0).resultSet,
        "long" -> new RowList1.Impl(classOf[Long]).append(0L).resultSet
      )

      rs.foreach(_._2.next)

      Fragment.foreach(rs) { r =>
        s"from ${r._1}" in { r._2.getBoolean(1) aka "boolean" must beFalse }
      }
    }

    "converted to true by index" >> {
      val rs = Seq[(String, ResultSet)](
        "char" -> (new RowList1.Impl(classOf[Char]).append('1')).resultSet,
        "byte" -> (new RowList1.Impl(classOf[Byte]).append(2.toByte)).resultSet,
        "short" -> (new RowList1.Impl(classOf[Short])
          .append(3.toShort))
          .resultSet,
        "int" -> (new RowList1.Impl(classOf[Int]).append(4)).resultSet,
        "long" -> (new RowList1.Impl(classOf[Long]).append(5L)).resultSet
      )

      rs.foreach(_._2.next)

      Fragment.foreach(rs) { r =>
        s"from ${r._1}" in { r._2.getBoolean(1) aka "boolean" must beTrue }
      }
    }

    "converted to false by label" >> {
      val rs = Seq[(String, ResultSet)](
        "char" -> (new RowList1.Impl(classOf[Char])
          .withLabel(1, "n")
          .append('0'))
          .resultSet,
        "byte" -> (new RowList1.Impl(classOf[Byte])
          .withLabel(1, "n")
          .append(0.toByte))
          .resultSet,
        "short" -> (new RowList1.Impl(classOf[Short])
          .withLabel(1, "n")
          .append(0.toShort))
          .resultSet,
        "int" -> (new RowList1.Impl(classOf[Int])
          .withLabel(1, "n")
          .append(0))
          .resultSet,
        "long" -> (new RowList1.Impl(classOf[Long])
          .withLabel(1, "n")
          .append(0L))
          .resultSet
      )

      rs.foreach(_._2.next)

      Fragment.foreach(rs) { r =>
        s"from ${r._1}" in { r._2.getBoolean("n") aka "boolean" must beFalse }
      }
    }

    "converted to true by label" >> {
      val rs = Seq[(String, ResultSet)](
        "char" -> (new RowList1.Impl(classOf[Char])
          .withLabel(1, "n")
          .append('1'))
          .resultSet,
        "byte" -> (new RowList1.Impl(classOf[Byte])
          .withLabel(1, "n")
          .append(2.toByte))
          .resultSet,
        "short" -> (new RowList1.Impl(classOf[Short])
          .withLabel(1, "n")
          .append(3.toShort))
          .resultSet,
        "int" -> (new RowList1.Impl(classOf[Int])
          .withLabel(1, "n")
          .append(4))
          .resultSet,
        "long" -> (new RowList1.Impl(classOf[Long])
          .withLabel(1, "n")
          .append(5L))
          .resultSet
      )

      rs.foreach(_._2.next)

      Fragment.foreach(rs) { r =>
        s"from ${r._1}" in { r._2.getBoolean("n") aka "boolean" must beTrue }
      }
    }
  }

  numberGetterSpec[Byte]("Byte", 1.toByte)
  numberGetterSpec[Short]("Short", 1.toShort)
  numberGetterSpec[Int]("Int", 1)
  numberGetterSpec[Long]("Long", 1L)
  numberGetterSpec[Float]("Float", 1F)
  numberGetterSpec[Double]("Double", 1.toDouble)

  "BigDecimal column from result set" should {
    val v = new JBigDec("1")

    "not be read by index when not on a row" in {
      (new RowList1.Impl(classOf[JBigDec])
        .append(v)
        .resultSet
        .getBigDecimal(1) aka "get" must throwA[SQLException](
        message = "Not on a row"
      )).and(
        new RowList1.Impl(classOf[JBigDec])
          .withLabel(1, "n")
          .append(v)
          .resultSet
          .getBigDecimal("n")
          .aka("get") must throwA[SQLException]("Not on a row")
      )

    }

    "be expected one" in {
      val rs = new RowList1.Impl(classOf[JBigDec])
        .withLabel(1, "n")
        .append(v)
        .resultSet
      rs.next

      (rs.getBigDecimal(1) aka "big decimal by index" must_=== v)
        .and(rs.getBigDecimal("n") aka "big decimal by name" must_=== v)
    }

    "be scaled one" in {
      val rs = new RowList1.Impl(classOf[JBigDec])
        .withLabel(1, "n")
        .append(new JBigDec("1.2345"))
        .resultSet

      rs.next

      (rs.getBigDecimal(1, 2).aka("big decimal by index") must_=== new JBigDec(
        "1.23"
      )).and(
        rs.getBigDecimal("n", 3)
          .aka("big decimal by name") must_=== new JBigDec("1.234")
      )
    }

    "be null" in {
      val rs = new RowList1.Impl(classOf[JBigDec])
        .withLabel(1, "n")
        .append(null.asInstanceOf[JBigDec])
        .resultSet
      rs.next

      (rs.getBigDecimal(1) aka "big decimal" must beNull)
        .and(rs.getBigDecimal(1, 1) aka "big decimal" must beNull)
        .and(rs.getBigDecimal("n") aka "big decimal" must beNull)
        .and(rs.getBigDecimal("n", 1) aka "big decimal" must beNull)
    }

    "be undefined" in {
      val rs = new RowList1.Impl(classOf[String])
        .withLabel(1, "n")
        .append("str")
        .resultSet
      rs.next

      (rs.getBigDecimal(1) aka "getBigDecimal" must throwA[SQLException](
        message = "Not a BigDecimal: 1"
      )).and(rs.getBigDecimal(1, 1) aka "getBigDecimal" must {
        throwA[SQLException]("Not a BigDecimal: 1")
      }).and(rs.getBigDecimal("n").aka("getBigDecimal").must {
        throwA[SQLException]("Not a BigDecimal: n")
      }).and(rs.getBigDecimal("n", 1).aka("getBigDecimal").must {
        throwA[SQLException]("Not a BigDecimal: n")
      })

    }

    "converted by index" >> {
      val rs = Seq[(String, ResultSet)](
        "byte" -> (new RowList1.Impl(classOf[Byte])
          .append(v.intValue.toByte))
          .resultSet,
        "short" -> (new RowList1.Impl(classOf[Short])
          .append(v.intValue.toShort))
          .resultSet,
        "int" -> (new RowList1.Impl(classOf[Int]).append(v.intValue)).resultSet,
        "long" -> (new RowList1.Impl(classOf[Long])
          .append(v.longValue))
          .resultSet,
        "float" -> (new RowList1.Impl(classOf[Float])
          .append(v.floatValue))
          .resultSet,
        "double" -> (new RowList1.Impl(classOf[Double])
          .append(v.doubleValue))
          .resultSet
      )

      rs.foreach(_._2.next)

      Fragment.foreach(rs) { r =>
        s"from ${r._1}" in {
          r._2.getBigDecimal(1).doubleValue must_=== v.doubleValue
        }
      }
    }

    "converted by label" >> {
      val rs = Seq[(String, ResultSet)](
        "byte" -> (new RowList1.Impl(classOf[Byte])
          .withLabel(1, "n")
          .append(v.intValue.toByte))
          .resultSet,
        "short" -> (new RowList1.Impl(classOf[Short])
          .withLabel(1, "n")
          .append(v.intValue.toShort))
          .resultSet,
        "int" -> (new RowList1.Impl(classOf[Int])
          .withLabel(1, "n")
          .append(v.intValue))
          .resultSet,
        "long" -> (new RowList1.Impl(classOf[Long])
          .withLabel(1, "n")
          .append(v.longValue))
          .resultSet,
        "float" -> (new RowList1.Impl(classOf[Float])
          .withLabel(1, "n")
          .append(v.floatValue))
          .resultSet,
        "double" -> (new RowList1.Impl(classOf[Double])
          .withLabel(1, "n")
          .append(v.doubleValue))
          .resultSet
      )

      rs.foreach(_._2.next)

      Fragment.foreach(rs) { r =>
        s"from ${r._1}" in {
          r._2
            .getBigDecimal("n")
            .doubleValue
            .aka("big decimal") must_=== v.doubleValue
        }
      }
    }
  }

  "Array column from result set" should {
    val v = ImmutableArray.getInstance(classOf[String], Array("a", "b", "c"))

    "not be read by index when not on a row" in {
      (new RowList1.Impl(classOf[ImmutableArray[String]])
        .append(v)
        .resultSet
        .getArray(1) aka "get" must (throwA[SQLException](message =
        "Not on a row"
      ))).and(
        new RowList1.Impl(classOf[ImmutableArray[String]])
          .withLabel(1, "n")
          .append(v)
          .resultSet
          .getArray("n")
          .aka("get") must throwA[SQLException]("Not on a row")
      )

    }

    "be expected one" in {
      val rs = new RowList1.Impl(classOf[ImmutableArray[String]])
        .withLabel(1, "n")
        .append(v)
        .resultSet
      rs.next

      (rs.getArray(1) aka "array by index" must_=== v)
        .and(rs.getArray("n") aka "array by name" must_=== v)
    }

    "be null" in {
      val rs = new RowList1.Impl(classOf[ImmutableArray[String]])
        .withLabel(1, "n")
        .append(null.asInstanceOf[ImmutableArray[String]])
        .resultSet
      rs.next

      (rs.getArray(1) aka "array" must beNull) and
        (rs.getArray("n") aka "array" must beNull)
    }

    "be undefined" in {
      val rs = new RowList1.Impl(classOf[String])
        .withLabel(1, "n")
        .append("str")
        .resultSet
      rs.next

      (rs.getArray(1) aka "get array by index" must throwA[SQLException](
        message = "Not an Array: 1"
      )).and(rs.getArray("n").aka("get array by name").must {
        throwA[SQLException]("Not an Array: n")
      })
    }

    "converted by index" >> {
      val rs = Seq[(String, ResultSet)](
        "raw array" -> (new RowList1.Impl(classOf[Array[String]]))
          .append(Array("a", "b", "c"))
          .resultSet
      )

      rs.foreach(_._2.next)

      Fragment.foreach(rs) { r =>
        s"from ${r._1}" in {
          r._2.getArray(1) aka "array" must_=== v
        }
      }
    }

    "converted by label" >> {
      val rs = Seq[(String, ResultSet)](
        "raw array" -> (new RowList1.Impl(classOf[Array[String]]))
          .withLabel(1, "n")
          .append(Array("a", "b", "c"))
          .resultSet
      )

      rs.foreach(_._2.next)

      Fragment.foreach(rs) { r =>
        s"from ${r._1}" in {
          r._2.getArray("n") aka "array" must_=== v
        }
      }
    }
  }

  "Scalar row list" should {
    "be created for a string" in {
      val rs = RowLists.scalar("Foo").resultSet
      rs.next

      rs.getString(1) aka "scalar value" must_=== "Foo"
    }

    "be created for an integer" in {
      val rs = RowLists.scalar(123).resultSet
      rs.next

      rs.getInt(1) aka "scalar value" must_=== 123
    }
  }

  temporalGetterSpec[Date]("Date", new Date(System.currentTimeMillis()))

  temporalGetterSpec[Time]("Time", new Time(System.currentTimeMillis()))

  temporalGetterSpec[Timestamp](
    "Timestamp",
    new Timestamp(System.currentTimeMillis())
  )
}

sealed trait RowListTest { specs: Specification =>
  import java.util.Calendar

  implicit def dateByIndex(rs: ResultSet, i: Int): Date = rs.getDate(i)
  implicit def dateByLabel(rs: ResultSet, n: String): Date = rs.getDate(n)

  implicit def dateByIndexWithCal(rs: ResultSet, i: Int, c: Calendar): Date =
    rs.getDate(i, c)

  implicit def dateByLabelWithCal(rs: ResultSet, n: String, c: Calendar): Date =
    rs.getDate(n, c)

  implicit def timeByIndex(rs: ResultSet, i: Int): Time = rs.getTime(i)
  implicit def timeByLabel(rs: ResultSet, n: String): Time = rs.getTime(n)

  implicit def timeByIndexWithCal(rs: ResultSet, i: Int, c: Calendar): Time =
    rs.getTime(i, c)

  implicit def timeByLabelWithCal(rs: ResultSet, n: String, c: Calendar): Time =
    rs.getTime(n, c)

  implicit def tsByIndex(rs: ResultSet, i: Int): Timestamp =
    rs.getTimestamp(i)

  implicit def tsByLabel(rs: ResultSet, n: String): Timestamp =
    rs.getTimestamp(n)

  implicit def tsByIndexWithCal(rs: ResultSet, i: Int, c: Calendar): Timestamp =
    rs.getTimestamp(i, c)

  implicit def tsByLabelWithCal(
      rs: ResultSet,
      n: String,
      c: Calendar
    ): Timestamp = rs.getTimestamp(n, c)

  def temporalGetterSpec[D <: java.util.Date](
      name: String,
      v: D
    )(implicit
      mf: ClassTag[D],
      byIndex: (ResultSet, Int) => D,
      byLabel: (ResultSet, String) => D,
      byIndexWithCal: (ResultSet, Int, Calendar) => D,
      byLabelWithCal: (ResultSet, String, Calendar) => D
    ) =
    s"$name column from result set" should {
      "not be read by index when not on a row" in {
        (byIndex(
          new RowList1.Impl(mf.runtimeClass.asInstanceOf[Class[D]])
            .append(v)
            .resultSet,
          1
        ).aka("get") must throwA[SQLException](message = "Not on a row")).and(
          byLabel(
            new RowList1.Impl(mf.runtimeClass.asInstanceOf[Class[D]])
              .withLabel(1, "n")
              .append(v)
              .resultSet,
            "n"
          ).aka("get") must throwA[SQLException]("Not on a row")
        )

      }

      "be expected one" in {
        val c = Calendar.getInstance
        val rs = new RowList1.Impl(mf.runtimeClass.asInstanceOf[Class[D]])
          .withLabel(1, "n")
          .append(v)
          .resultSet
        rs.next

        (byIndex(rs, 1) aka "big decimal by index" must_=== v)
          .and(byIndexWithCal(rs, 1, c) aka "big decimal by index" must_=== v)
          .and(byLabel(rs, "n") aka "big decimal by name" must_=== v)
          .and(byLabelWithCal(rs, "n", c) aka "big decimal by name" must_=== v)
      }

      "be null" in {
        val c = Calendar.getInstance
        val rs = new RowList1.Impl(mf.runtimeClass.asInstanceOf[Class[D]])
          .withLabel(1, "n")
          .append(null.asInstanceOf[D])
          .resultSet

        rs.next

        (byIndex(rs, 1) aka "time" must beNull)
          .and(byIndexWithCal(rs, 1, c) aka "time" must beNull)
          .and(byLabel(rs, "n") aka "time" must beNull)
          .and(byLabelWithCal(rs, "n", c) aka "time" must beNull)
      }

      "be undefined" in {
        val c = Calendar.getInstance
        val rs = new RowList1.Impl(classOf[String])
          .withLabel(1, "n")
          .append("str")
          .resultSet
        rs.next

        (byIndex(rs, 1) aka "get" must throwA[SQLException](
          message = s"Not a $name: 1"
        )).and(
          byIndexWithCal(rs, 1, c) aka "get" must throwA[SQLException](
            message = s"Not a $name: 1"
          )
        ).and(
          byLabel(rs, "n")
            .aka("get") must throwA[SQLException](s"Not a $name: n")
        ).and(
          byLabelWithCal(rs, "n", c)
            .aka("get") must throwA[SQLException](s"Not a $name: n")
        )

      }

      "converted by index" >> {
        val rs = Seq[(String, ResultSet)](
          "date" -> new RowList1.Impl(classOf[Date])
            .append(new Date(v.getTime))
            .resultSet,
          "time" -> new RowList1.Impl(classOf[Time])
            .append(new Time(v.getTime))
            .resultSet,
          "ts" -> new RowList1.Impl(classOf[Timestamp])
            .append(new Timestamp(v.getTime))
            .resultSet
        )

        rs.foreach(_._2.next)

        Fragment.foreach(rs) { r =>
          s"from ${r._1}" in {
            byIndex(r._2, 1) aka "get" must not(throwA[SQLException])
          }
        }
      }

      "converted by label" >> {
        val rs = Seq[(String, ResultSet)](
          "date" -> new RowList1.Impl(classOf[Date])
            .withLabel(1, "n")
            .append(new Date(v.getTime))
            .resultSet,
          "time" -> new RowList1.Impl(classOf[Time])
            .withLabel(1, "n")
            .append(new Time(v.getTime))
            .resultSet,
          "ts" -> new RowList1.Impl(classOf[Timestamp])
            .withLabel(1, "n")
            .append(new Timestamp(v.getTime))
            .resultSet
        )

        rs.foreach(_._2.next)

        Fragment.foreach(rs) { r =>
          s"from ${r._1}" in {
            byLabel(r._2, "n") aka "get" must not(throwA[SQLException])
          }
        }
      }
    }

  // ---

  implicit def byteByIndex(rs: ResultSet, i: Int): Byte = rs.getByte(i)
  implicit def byteByLabel(rs: ResultSet, n: String): Byte = rs.getByte(n)
  implicit def shortByIndex(rs: ResultSet, i: Int): Short = rs.getShort(i)
  implicit def shortByLabel(rs: ResultSet, n: String): Short = rs.getShort(n)
  implicit def intByIndex(rs: ResultSet, i: Int): Int = rs.getInt(i)
  implicit def intByLabel(rs: ResultSet, n: String): Int = rs.getInt(n)
  implicit def longByIndex(rs: ResultSet, i: Int): Long = rs.getLong(i)
  implicit def longByLabel(rs: ResultSet, n: String): Long = rs.getLong(n)
  implicit def floatByIndex(rs: ResultSet, i: Int): Float = rs.getFloat(i)
  implicit def floatByLabel(rs: ResultSet, n: String): Float = rs.getFloat(n)
  implicit def doubleByIndex(rs: ResultSet, i: Int): Double = rs.getDouble(i)
  implicit def doubleByLabel(rs: ResultSet, n: String): Double = rs.getDouble(n)

  def numberGetterSpec[N](
      name: String,
      v: N
    )(implicit
      num: Numeric[N],
      mf: ClassTag[N],
      byIndex: (ResultSet, Int) => N,
      byLabel: (ResultSet, String) => N
    ) =
    s"$name column from result set" should {
      "not be read by index when not on a row" in {
        (byIndex(
          new RowList1.Impl(mf.runtimeClass.asInstanceOf[Class[N]])
            .append(v)
            .resultSet,
          1
        ).aka("get") must throwA[SQLException](message = "Not on a row")).and(
          byLabel(
            new RowList1.Impl(mf.runtimeClass.asInstanceOf[Class[N]])
              .withLabel(1, "n")
              .append(v)
              .resultSet,
            "n"
          ).aka("get") must throwA[SQLException]("Not on a row")
        )

      }

      "be expected one" in {
        val rs = new RowList1.Impl(mf.runtimeClass.asInstanceOf[Class[N]])
          .withLabel(1, "n")
          .append(v)
          .resultSet
        rs.next

        (byIndex(rs, 1) aka s"$name by index" must_=== v)
          .and(byLabel(rs, "n") aka s"$name by name" must_=== v)
      }

      "be null (0)" in {
        val rs = new RowList1.Impl(mf.runtimeClass.asInstanceOf[Class[N]])
          .withLabel(1, "n")
          .append(null.asInstanceOf[N])
          .resultSet
        rs.next

        (num.toInt(byIndex(rs, 1)) aka name must_=== 0)
          .and(num.toInt(byLabel(rs, "n")) aka name must_=== 0)
      }

      "be undefined (-1)" in {
        val rs = new RowList1.Impl(classOf[String])
          .withLabel(1, "n")
          .append("str")
          .resultSet
        rs.next

        (num.toInt(byIndex(rs, 1)) aka s"$name" must_=== -1)
          .and(num.toInt(byLabel(rs, "n")) aka s"$name" must_=== -1)

      }

      "converted by index" >> {
        val rs = Seq[(String, ResultSet)](
          "byte" -> new RowList1.Impl(classOf[Byte])
            .append(num.toInt(v).toByte)
            .resultSet,
          "short" -> new RowList1.Impl(classOf[Short])
            .append(num.toInt(v).toShort)
            .resultSet,
          "int" -> new RowList1.Impl(classOf[Int])
            .append(num.toInt(v))
            .resultSet,
          "long" -> new RowList1.Impl(classOf[Long])
            .append(num.toLong(v))
            .resultSet,
          "float" -> new RowList1.Impl(classOf[Float])
            .append(num.toFloat(v))
            .resultSet,
          "double" -> new RowList1.Impl(classOf[Double])
            .append(num.toDouble(v))
            .resultSet
        )

        rs.foreach(_._2.next)

        Fragment.foreach(rs) { r =>
          s"from ${r._1}" in {
            num.toInt(byIndex(r._2, 1)) aka s"$name" must_=== 1
          }
        }
      }

      "converted by label" >> {
        val rs = Seq[(String, ResultSet)](
          "byte" -> new RowList1.Impl(classOf[Byte])
            .withLabel(1, "n")
            .append(num.toInt(v).toByte)
            .resultSet,
          "short" -> new RowList1.Impl(classOf[Short])
            .withLabel(1, "n")
            .append(num.toInt(v).toShort)
            .resultSet,
          "int" -> new RowList1.Impl(classOf[Int])
            .withLabel(1, "n")
            .append(num.toInt(v))
            .resultSet,
          "long" -> new RowList1.Impl(classOf[Long])
            .withLabel(1, "n")
            .append(num.toLong(v))
            .resultSet,
          "float" -> new RowList1.Impl(classOf[Float])
            .withLabel(1, "n")
            .append(num.toFloat(v))
            .resultSet,
          "double" -> new RowList1.Impl(classOf[Double])
            .withLabel(1, "n")
            .append(num.toDouble(v))
            .resultSet
        )

        rs.foreach(_._2.next)

        Fragment.foreach(rs) { r =>
          s"from ${r._1}" in {
            num.toInt(byLabel(r._2, "n")) aka s"$name" must_=== 1
          }
        }
      }
    }
}
