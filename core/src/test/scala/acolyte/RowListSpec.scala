package acolyte

import java.math.{ BigDecimal ⇒ JBigDec }

import java.sql.{
  Date,
  ResultSet,
  ResultSetMetaData,
  SQLException,
  Time,
  Timestamp,
  Types
}

import org.specs2.mutable.Specification

import acolyte.Row.Row1
import acolyte.Rows.{ row1, row2 }

object RowListSpec extends Specification with RowListTest {
  "Row list" title

  "Creation" should {
    "not accept null list" in {
      new RowList1(classOf[String], null,
        new java.util.HashMap[String, Integer]()).
        aka("ctor") must throwA[IllegalArgumentException](
          message = "Invalid rows")
    }

    "not accept null map" in {
      new RowList2(classOf[String], classOf[Float],
        new java.util.ArrayList[Row2[String, Float]](), null).
        aka("ctor") must throwA[IllegalArgumentException](
          message = "Invalid names")
    }
  }

  "Result set metadata" should {
    lazy val meta = RowLists.rowList3(
      classOf[Float], classOf[String], classOf[Time]).
      withLabel(2, "title").
      append(Rows.row3(1.23f, "str", new Time(1, 2, 3))).
      resultSet.getMetaData

    "have expected column catalog" in {
      meta.getCatalogName(1) aka "catalog" mustEqual ""
    }

    "have expected column schema" in {
      meta.getSchemaName(1) aka "schema" mustEqual ""
    }

    "have expected column table" in {
      meta.getTableName(1) aka "table" mustEqual ""
    }

    "have expected column count" in {
      meta.getColumnCount aka "count" mustEqual 3
    }

    "have expected class" >> {
      "for column #1" in {
        meta.getColumnClassName(1) aka "class" mustEqual classOf[Float].getName
      }

      "for column #2" in {
        meta.getColumnClassName(2) aka "class" mustEqual classOf[String].getName
      }

      "for column #3" in {
        meta.getColumnClassName(3) aka "class" mustEqual classOf[Time].getName
      }
    }

    "have expected display size" in {
      meta.getColumnDisplaySize(1) aka "size" mustEqual Integer.MAX_VALUE
    }

    "have expected label" >> {
      "for column #1" in {
        (meta.getColumnName(1) aka "name" must beNull).
          and(meta.getColumnLabel(1) aka "label" must beNull)

      }

      "for column #2" in {
        (meta.getColumnName(2) aka "name" mustEqual "title").
          and(meta.getColumnLabel(2) aka "label" mustEqual "title")
      }

      "for column #3" in {
        (meta.getColumnName(3) aka "name" must beNull).
          and(meta.getColumnLabel(3) aka "label" must beNull)

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

    "have expected nullable flag" in {
      meta.isNullable(1).
        aka("nullable") mustEqual ResultSetMetaData.columnNullableUnknown

    }

    "not support currency" in {
      meta.isCurrency(1) aka "currency" must beFalse
    }

    "have expected precision" >> {
      "for column #1" in {
        meta.getPrecision(1) aka "precision" mustEqual 32
      }

      "for column #2" in {
        meta.getPrecision(2) aka "precision" mustEqual 0
      }

      "for column #3" in {
        meta.getPrecision(3) aka "precision" mustEqual 0
      }
    }

    "have expected scale" >> {
      "for column #1" in {
        meta.getScale(1) aka "scale" mustEqual 2
      }

      "for column #2" in {
        meta.getScale(2) aka "scale" mustEqual 0
      }

      "for column #3" in {
        meta.getScale(3) aka "scale" mustEqual 0
      }
    }

    "have expected type" >> {
      "for column #1" in {
        (meta.getColumnType(1) aka "type" mustEqual Types.FLOAT).
          and(meta.getColumnTypeName(1) aka "type name" mustEqual "FLOAT")
      }

      "for column #2" in {
        (meta.getColumnType(2) aka "type" mustEqual Types.VARCHAR).
          and(meta.getColumnTypeName(2) aka "type name" mustEqual "VARCHAR")
      }

      "for column #3" in {
        (meta.getColumnType(3) aka "type" mustEqual Types.TIME).
          and(meta.getColumnTypeName(3) aka "type name" mustEqual "TIME")
      }
    }

    "have expected flags" in {
      (meta.isSearchable(1) aka "searchable" must beTrue).
        and(meta.isCaseSensitive(1) aka "case sensitive" must beTrue).
        and(meta.isAutoIncrement(1) aka "auto-increment" must beFalse).
        and(meta.isReadOnly(1) aka "read-only" must beTrue).
        and(meta.isWritable(1) aka "writable" must beFalse).
        and(meta.isDefinitelyWritable(1).
          aka("definitely writable") must beFalse)

    }
  }

  "Column classes" should {
    "be String" in {
      val cs = {
        val l = new java.util.ArrayList[Class[_]]
        l.add(classOf[String])
        l
      }

      RowLists.rowList1(classOf[String]).
        getColumnClasses() aka "columns" mustEqual cs

    }

    "be String, Double, Date" in {
      val cs = {
        val l = new java.util.ArrayList[Class[_]]
        l.add(classOf[String])
        l.add(classOf[Double])
        l.add(classOf[java.util.Date])
        l
      }

      RowLists.rowList3(
        classOf[String],
        classOf[Double],
        classOf[java.util.Date]).getColumnClasses() aka "columns" mustEqual cs

    }
  }

  "Result set statement" should {
    "be null" in {
      (new RowList1(classOf[String]).resultSet.
        getStatement aka "statement" must beNull).
        and(new RowList1(classOf[String]).resultSet.withStatement(null).
          getStatement aka "statement" must beNull)

    }

    "be expected one" in {
      val url = "jdbc:acolyte:test"
      val ch = test.EmptyConnectionHandler
      lazy val con = new acolyte.Connection(url, null, ch)
      lazy val s = new AbstractStatement(con, ch.getStatementHandler) {}

      new RowList1(classOf[String]).resultSet.withStatement(s).
        getStatement aka "statement" mustEqual s

    }
  }

  "Single column row list" should {
    "accept string value" in {
      val rs = RowLists.stringList.append("strval").resultSet

      (rs.getFetchSize aka "size" mustEqual 1).
        and(rs.next aka "has row" must beTrue).
        and(rs.getString(1) aka "single col" mustEqual "strval")
    }

    "be created with initial string values" in {
      val rs = RowLists.stringList("A", "B", "C").resultSet

      (rs.getFetchSize aka "size" mustEqual 3).
        and(rs.next aka "has row #1" must beTrue).
        and(rs.getString(1) aka "single col #1" mustEqual "A").
        and(rs.next aka "has row #2" must beTrue).
        and(rs.getString(1) aka "single col #2" mustEqual "B").
        and(rs.next aka "has row #3" must beTrue).
        and(rs.getString(1) aka "single col #3" mustEqual "C")

    }

    "accept boolean value" in {
      val rs = RowLists.booleanList.append(false).resultSet

      (rs.getFetchSize aka "size" mustEqual 1).
        and(rs.next aka "has row" must beTrue).
        and(rs.getBoolean(1) aka "single col" must beFalse)
    }

    "be created with initial boolean values" in {
      val rs = RowLists.booleanList(true, true, false).resultSet

      (rs.getFetchSize aka "size" mustEqual 3).
        and(rs.next aka "has row #1" must beTrue).
        and(rs.getBoolean(1) aka "single col #1" must beTrue).
        and(rs.next aka "has row #2" must beTrue).
        and(rs.getBoolean(1) aka "single col #2" must beTrue).
        and(rs.next aka "has row #3" must beTrue).
        and(rs.getBoolean(1) aka "single col #3" must beFalse)

    }

    "accept byte value" in {
      val rs = RowLists.byteList.append(2.toByte).resultSet

      (rs.getFetchSize aka "size" mustEqual 1).
        and(rs.next aka "has row" must beTrue).
        and(rs.getByte(1) aka "single col" mustEqual 2)
    }

    "be created with initial byte values" in {
      val rs = RowLists.byteList(1.toByte, 2.toByte, 3.toByte).resultSet

      (rs.getFetchSize aka "size" mustEqual 3).
        and(rs.next aka "has row #1" must beTrue).
        and(rs.getByte(1) aka "single col #1" mustEqual 1).
        and(rs.next aka "has row #2" must beTrue).
        and(rs.getByte(1) aka "single col #2" mustEqual 2).
        and(rs.next aka "has row #3" must beTrue).
        and(rs.getByte(1) aka "single col #3" mustEqual 3)

    }

    "accept short value" in {
      val rs = RowLists.shortList.append(3.toShort).resultSet

      (rs.getFetchSize aka "size" mustEqual 1).
        and(rs.next aka "has row" must beTrue).
        and(rs.getShort(1) aka "single col" mustEqual 3)
    }

    "be created with initial short values" in {
      val rs = RowLists.shortList(1.toShort, 2.toShort, 3.toShort).resultSet

      (rs.getFetchSize aka "size" mustEqual 3).
        and(rs.next aka "has row #1" must beTrue).
        and(rs.getShort(1) aka "single col #1" mustEqual 1).
        and(rs.next aka "has row #2" must beTrue).
        and(rs.getShort(1) aka "single col #2" mustEqual 2).
        and(rs.next aka "has row #3" must beTrue).
        and(rs.getShort(1) aka "single col #3" mustEqual 3)

    }

    "accept int value" in {
      val rs = RowLists.intList.append(4).resultSet

      (rs.getFetchSize aka "size" mustEqual 1).
        and(rs.next aka "has row" must beTrue).
        and(rs.getInt(1) aka "single col" mustEqual 4)
    }

    "be created with initial int values" in {
      val rs = RowLists.intList(1, 2, 3).resultSet

      (rs.getFetchSize aka "size" mustEqual 3).
        and(rs.next aka "has row #1" must beTrue).
        and(rs.getInt(1) aka "single col #1" mustEqual 1).
        and(rs.next aka "has row #2" must beTrue).
        and(rs.getInt(1) aka "single col #2" mustEqual 2).
        and(rs.next aka "has row #3" must beTrue).
        and(rs.getInt(1) aka "single col #3" mustEqual 3)

    }

    "accept long value" in {
      val rs = RowLists.longList.append(5l).resultSet

      (rs.getFetchSize aka "size" mustEqual 1).
        and(rs.next aka "has row" must beTrue).
        and(rs.getLong(1) aka "single col" mustEqual 5l)
    }

    "be created with initial long values" in {
      val rs = RowLists.longList(1l, 2l, 3l).resultSet

      (rs.getFetchSize aka "size" mustEqual 3).
        and(rs.next aka "has row #1" must beTrue).
        and(rs.getLong(1) aka "single col #1" mustEqual 1l).
        and(rs.next aka "has row #2" must beTrue).
        and(rs.getLong(1) aka "single col #2" mustEqual 2l).
        and(rs.next aka "has row #3" must beTrue).
        and(rs.getLong(1) aka "single col #3" mustEqual 3l)

    }

    "accept float value" in {
      val rs = RowLists.floatList.append(6.7f).resultSet

      (rs.getFetchSize aka "size" mustEqual 1).
        and(rs.next aka "has row" must beTrue).
        and(rs.getFloat(1) aka "single col" mustEqual 6.7f)
    }

    "be created with initial float values" in {
      val rs = RowLists.floatList(1f, 2f, 3f).resultSet

      (rs.getFetchSize aka "size" mustEqual 3).
        and(rs.next aka "has row #1" must beTrue).
        and(rs.getFloat(1) aka "single col #1" mustEqual 1f).
        and(rs.next aka "has row #2" must beTrue).
        and(rs.getFloat(1) aka "single col #2" mustEqual 2f).
        and(rs.next aka "has row #3" must beTrue).
        and(rs.getFloat(1) aka "single col #3" mustEqual 3f)

    }

    "accept double value" in {
      val rs = RowLists.doubleList.append(7.89d).resultSet

      (rs.getFetchSize aka "size" mustEqual 1).
        and(rs.next aka "has row" must beTrue).
        and(rs.getDouble(1) aka "single col" mustEqual 7.89d)
    }

    "be created with initial double values" in {
      val rs = RowLists.doubleList(1d, 2d, 3d).resultSet

      (rs.getFetchSize aka "size" mustEqual 3).
        and(rs.next aka "has row #1" must beTrue).
        and(rs.getDouble(1) aka "single col #1" mustEqual 1d).
        and(rs.next aka "has row #2" must beTrue).
        and(rs.getDouble(1) aka "single col #2" mustEqual 2d).
        and(rs.next aka "has row #3" must beTrue).
        and(rs.getDouble(1) aka "single col #3" mustEqual 3d)

    }

    "accept big decimal" in {
      val bigdec = new java.math.BigDecimal(1.234)
      val rs = RowLists.bigDecimalList.append(bigdec).resultSet

      (rs.getFetchSize aka "size" mustEqual 1).
        and(rs.next aka "has row" must beTrue).
        and(rs.getBigDecimal(1) aka "single col" mustEqual bigdec)
    }

    "be created with initial big decimal values" in {
      val (a, b, c) = (
        new java.math.BigDecimal(1.2),
        new java.math.BigDecimal(23.4),
        new java.math.BigDecimal(4.567))

      val rs = RowLists.bigDecimalList(a, b, c).resultSet

      (rs.getFetchSize aka "size" mustEqual 3).
        and(rs.next aka "has row #1" must beTrue).
        and(rs.getBigDecimal(1) aka "single col #1" mustEqual a).
        and(rs.next aka "has row #2" must beTrue).
        and(rs.getBigDecimal(1) aka "single col #2" mustEqual b).
        and(rs.next aka "has row #3" must beTrue).
        and(rs.getBigDecimal(1) aka "single col #3" mustEqual c)

    }

    "accept date" in {
      val d = new java.sql.Date(1, 2, 3)
      val rs = RowLists.dateList.append(d).resultSet

      (rs.getFetchSize aka "size" mustEqual 1).
        and(rs.next aka "has row" must beTrue).
        and(rs.getDate(1) aka "single col" mustEqual d)
    }

    "be created with initial date values" in {
      val (a, b, c) = (
        new java.sql.Date(1, 2, 3),
        new java.sql.Date(2, 3, 4),
        new java.sql.Date(3, 4, 5))

      val rs = RowLists.dateList(a, b, c).resultSet

      (rs.getFetchSize aka "size" mustEqual 3).
        and(rs.next aka "has row #1" must beTrue).
        and(rs.getDate(1) aka "single col #1" mustEqual a).
        and(rs.next aka "has row #2" must beTrue).
        and(rs.getDate(1) aka "single col #2" mustEqual b).
        and(rs.next aka "has row #3" must beTrue).
        and(rs.getDate(1) aka "single col #3" mustEqual c)

    }

    "accept time" in {
      val t = new java.sql.Time(4, 5, 6)
      val rs = RowLists.timeList.append(t).resultSet

      (rs.getFetchSize aka "size" mustEqual 1).
        and(rs.next aka "has row" must beTrue).
        and(rs.getTime(1) aka "single col" mustEqual t)
    }

    "be created with initial time values" in {
      val (a, b, c) = (
        new java.sql.Time(1, 2, 3),
        new java.sql.Time(2, 3, 4),
        new java.sql.Time(3, 4, 5))

      val rs = RowLists.timeList(a, b, c).resultSet

      (rs.getFetchSize aka "size" mustEqual 3).
        and(rs.next aka "has row #1" must beTrue).
        and(rs.getTime(1) aka "single col #1" mustEqual a).
        and(rs.next aka "has row #2" must beTrue).
        and(rs.getTime(1) aka "single col #2" mustEqual b).
        and(rs.next aka "has row #3" must beTrue).
        and(rs.getTime(1) aka "single col #3" mustEqual c)

    }

    "accept timestamp" in {
      val ts = new java.sql.Timestamp(1234l)
      val rs = RowLists.timestampList.append(ts).resultSet

      (rs.getFetchSize aka "size" mustEqual 1).
        and(rs.next aka "has row" must beTrue).
        and(rs.getTimestamp(1) aka "single col" mustEqual ts)
    }

    "be created with initial timestamp values" in {
      val (a, b, c) = (
        new java.sql.Timestamp(1, 2, 3, 4, 5, 6, 7),
        new java.sql.Timestamp(2, 3, 4, 5, 6, 7, 8),
        new java.sql.Timestamp(3, 4, 5, 6, 7, 8, 9))

      val rs = RowLists.timestampList(a, b, c).resultSet

      (rs.getFetchSize aka "size" mustEqual 3).
        and(rs.next aka "has row #1" must beTrue).
        and(rs.getTimestamp(1) aka "single col #1" mustEqual a).
        and(rs.next aka "has row #2" must beTrue).
        and(rs.getTimestamp(1) aka "single col #2" mustEqual b).
        and(rs.next aka "has row #3" must beTrue).
        and(rs.getTimestamp(1) aka "single col #3" mustEqual c)

    }
  }

  "Result set fetch size" should {
    "be immutable" in {
      new RowList1(classOf[String]).resultSet.setFetchSize(1).
        aka("setter") must throwA[UnsupportedOperationException]

    }

    "be 1" in {
      (new RowList1(classOf[String]).append(row1("str")).
        resultSet.getFetchSize aka "size" mustEqual 1).
        and(new RowList2(classOf[String], classOf[Float]).
          append(row2("str", 1.23.toFloat)).
          resultSet.getFetchSize aka "size" mustEqual 1)

    }

    "be 2" in {
      (new RowList1(classOf[String]).append(row1("a")).append(row1("b")).
        resultSet.getFetchSize aka "size" mustEqual 2)

    }
  }

  "Max rows limit" should {
    lazy val rows = RowLists.intList(10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0)

    "drop half of rows" in {
      rows.resultSet(5) aka "resultset" mustEqual {
        RowLists.intList(10, 9, 8, 7, 6).resultSet
      }
    }

    "drop 2 rows at end (max = 8)" in {
      rows.resultSet(8) aka "resultset" mustEqual {
        RowLists.intList(10, 9, 8, 7, 6, 5, 4, 3).resultSet
      }
    }

    "extract only 3 rows" in {
      rows.resultSet(3) aka "resultset" mustEqual {
        RowLists.intList(10, 9, 8).resultSet
      }
    }

    "get all rows" in {
      rows.resultSet(11) aka "resultset" mustEqual rows.resultSet
    }
  }

  "Object column by index" should {
    "not be read when not on a row" in {
      lazy val typemap = null.asInstanceOf[java.util.Map[String, Class[_]]]
      lazy val rs = new RowList1(classOf[String]).
        withLabel(1, "n").append(row1("str")).resultSet

      (rs.getObject(1) aka "getObject" must throwA[SQLException](
        message = "Not on a row")).
        and(rs.getObject("n") aka "getObject" must throwA[SQLException](
          message = "Not on a row")).
        and(rs.getObject(1, typemap) aka "getObject" must throwA[SQLException](
          message = "Not on a row")).
        and(rs.getObject("n", typemap).
          aka("getObject") must throwA[SQLException]("Not on a row")).
        and(rs.getObject(1, classOf[String]).
          aka("getObject") must throwA[SQLException]("Not on a row")).
        and(rs.getObject("n", classOf[String]).
          aka("getObject") must throwA[SQLException]("Not on a row"))

    }

    "be expected one" in {
      lazy val rs = (new RowList1(classOf[Long]).
        append(row1(123.toLong))).resultSet
      rs.next

      rs.getObject(1) aka "cell1" mustEqual 123.toLong
    }

    "be null" in {
      lazy val rs = new RowList1(classOf[Float]).withLabel(1, "n").
        append(row1(null.asInstanceOf[Float])).resultSet

      rs.next

      (rs.getObject(1) aka "cell1" must beNull).
        and(rs.getObject("n") aka "cell1" must beNull).
        and(rs.getObject(1, classOf[String]) aka "cell1" must beNull).
        and(rs.getObject("n", classOf[Float]) aka "cell1" mustEqual 0.0.toFloat)

    }

    "not be read with invalid index" in {
      lazy val rs = new RowList1(classOf[Long]).append(row1(123.toLong)).
        resultSet
      rs.next

      rs.getObject(2) aka "getObject" must throwA[SQLException](
        message = "Invalid column index: 2")

    }

    "not convert without type" in {
      lazy val rs = new RowList1(classOf[String]).withLabel(1, "n").
        append(row1("str")).resultSet
      rs.next

      (rs.getObject(1, null.asInstanceOf[Class[Object]]).
        aka("byIndex") must throwA[SQLException]("Invalid type")).
        and(rs.getObject("n", null.asInstanceOf[Class[Object]]).
          aka("byLabel") must throwA[SQLException]("Invalid type"))

    }

    "not convert incompatible type" in {
      lazy val rs = new RowList1(classOf[String]).withLabel(1, "n").
        append(row1("str")).resultSet
      rs.next

      (rs.getObject(1, classOf[java.sql.Date]).
        aka("byIndex") must throwA[SQLException]("Incompatible type")).
        and(rs.getObject("n", classOf[java.sql.Date]).
          aka("byLabel") must throwA[SQLException]("Incompatible type"))

    }

    "convert compatible type" in {
      val d = new Date(1, 2, 3)
      lazy val rs = new RowList1(classOf[Date]).withLabel(1, "n").
        append(row1(d)).resultSet
      rs.next

      (rs.getObject(1, classOf[java.util.Date]) aka "byIndex" mustEqual d).
        and(rs.getObject("n", classOf[java.util.Date]).
          aka("byLabel") mustEqual d)

    }

    "convert compatible temporal type" in {
      val d = new Date(1, 2, 3)
      lazy val t = new Time(d.getTime)
      lazy val ts = new Timestamp(d.getTime)
      lazy val rs = new RowList1(classOf[Date]).withLabel(1, "n").
        append(row1(d)).resultSet
      rs.next

      (rs.getObject(1, classOf[Time]) aka "byIndex" mustEqual t).
        and(rs.getObject("n", classOf[Timestamp]).
          aka("byLabel") mustEqual ts)

    }

    "convert compatible numeric type" in {
      lazy val rs = new RowList1(classOf[Int]).withLabel(1, "n").
        append(row1(1)).resultSet
      rs.next

      (rs.getObject(1, classOf[java.lang.Float]).
        aka("byIndex") mustEqual 1.toFloat).
        and(rs.getObject("n", classOf[java.lang.Double]).
          aka("byLabel") mustEqual 1.toDouble)

    }
  }

  "Object column by name" should {
    "not be read when not on a row" in {
      new RowList1(classOf[String]).withLabel(1, "n").
        append(row1("str")).resultSet.
        getObject("n") aka "getObject" must throwA[SQLException](
          message = "Not on a row")

    }

    "be expected one" in {
      lazy val rs = (new RowList1(classOf[Long]).
        withLabel(1, "l").append(row1(123.toLong))).resultSet
      rs.next

      rs.getObject("l") aka "cell1" mustEqual 123.toLong
    }

    "be null" in {
      lazy val rs = new RowList1(classOf[Float]).withLabel(1, "name").
        append(row1(null.asInstanceOf[Float])).resultSet

      rs.next

      rs.getObject("name") aka "cell1" must beNull
    }

    "not be read with invalid name" in {
      lazy val rs = new RowList1(classOf[Long]).
        withLabel(1, "n").append(row1(123.toLong)).resultSet
      rs.next

      (rs.getObject(null).
        aka("getObject") must throwA[SQLException]("Invalid label: null")).
        and(rs.getObject("label").
          aka("getObject") must throwA[SQLException]("Invalid label: label"))
    }

    "not be read with invalid name mapping" in {
      lazy val rs = new RowList1(classOf[Long]).
        withLabel(0, "before").withLabel(2, "after").
        append(row1(123.toLong)).resultSet
      rs.next

      (rs.getLong("before") aka "get" must throwA[SQLException](
        message = "Invalid column index: 0")).
        and(rs.getLong("after") aka "get" must throwA[SQLException](
          message = "Invalid column index: 2"))
    }
  }

  "String column from result set" should {
    "not be read by index when not on a row" in {
      (new RowList1(classOf[String]).append(row1("str")).resultSet.
        getString(1) aka "getString" must throwA[SQLException](
          message = "Not on a row")).
          and(new RowList1(classOf[String]).withLabel(1, "n").
            append(row1("str")).resultSet.getString("n").
            aka("getString") must throwA[SQLException]("Not on a row"))

    }

    "be expected one" in {
      val rs = new RowList1(classOf[String]).withLabel(1, "n").
        append(row1("str")).resultSet
      rs.next

      (rs.getString(1) aka "string by index" mustEqual "str").
        and(rs.getString("n") aka "string by name" mustEqual "str")
    }

    "be null" in {
      val rs = (new RowList1(classOf[String]).
        withLabel(1, "n").append(row1(null.asInstanceOf[String]))).resultSet

      rs.next

      (rs.getString(1) aka "boolean" must beNull).
        and(rs.getString("n") aka "boolean" must beNull)
    }
  }

  "Boolean column from result set" should {
    "not be read by index when not on a row" in {
      (new RowList1(classOf[Boolean]).append(row1(true)).resultSet.
        getBoolean(1) aka "getBoolean" must throwA[SQLException](
          message = "Not on a row")).
          and(new RowList1(classOf[Boolean]).withLabel(1, "n").
            append(row1(false)).resultSet.getBoolean("n").
            aka("getBoolean") must throwA[SQLException]("Not on a row"))

    }

    "be expected one" in {
      val rs = new RowList1(classOf[Boolean]).withLabel(1, "n").
        append(row1(true)).resultSet
      rs.next

      (rs.getBoolean(1) aka "boolean by index" mustEqual true).
        and(rs.getBoolean("n") aka "boolean by name" mustEqual true)
    }

    "be null (false)" in {
      val rs = new RowList1(classOf[Boolean]).withLabel(1, "n").
        append(row1(null.asInstanceOf[Boolean])).resultSet

      rs.next

      (rs.getBoolean(1) aka "boolean" must beFalse).
        and(rs.getBoolean("n") aka "boolean" must beFalse)
    }

    "converted to false by index" >> {
      val rs = Seq[(String, ResultSet)](
        "char" -> new RowList1(classOf[Char]).append(row1('0')).resultSet,
        "byte" -> new RowList1(classOf[Byte]).append(row1(0.toByte)).resultSet,
        "short" -> new RowList1(classOf[Short]).
          append(row1(0.toShort)).resultSet,
        "int" -> new RowList1(classOf[Int]).append(row1(0)).resultSet,
        "long" -> new RowList1(classOf[Long]).append(row1(0.toLong)).resultSet)

      rs.foreach(_._2.next)

      examplesBlock {
        rs foreach { r ⇒
          s"from ${r._1}" in { r._2.getBoolean(1) aka "boolean" must beFalse }
        }
      }
    }

    "converted to true by index" >> {
      val rs = Seq[(String, ResultSet)](
        "char" -> (new RowList1(classOf[Char]).append(row1('1'))).resultSet,
        "byte" -> (new RowList1(classOf[Byte]).append(row1(2.toByte))).resultSet,
        "short" -> (new RowList1(classOf[Short]).append(row1(3.toShort))).resultSet,
        "int" -> (new RowList1(classOf[Int]).append(row1(4))).resultSet,
        "long" -> (new RowList1(classOf[Long]).append(row1(5.toLong))).resultSet)

      rs.foreach(_._2.next)

      examplesBlock {
        rs foreach { r ⇒
          s"from ${r._1}" in { r._2.getBoolean(1) aka "boolean" must beTrue }
        }
      }
    }

    "converted to false by label" >> {
      val rs = Seq[(String, ResultSet)](
        "char" -> (new RowList1(classOf[Char]).
          withLabel(1, "n").append(row1('0'))).resultSet,
        "byte" -> (new RowList1(classOf[Byte]).
          withLabel(1, "n").append(row1(0.toByte))).resultSet,
        "short" -> (new RowList1(classOf[Short]).
          withLabel(1, "n").append(row1(0.toShort))).resultSet,
        "int" -> (new RowList1(classOf[Int]).
          withLabel(1, "n").append(row1(0))).resultSet,
        "long" -> (new RowList1(classOf[Long]).
          withLabel(1, "n").append(row1(0.toLong))).resultSet)

      rs.foreach(_._2.next)

      examplesBlock {
        rs foreach { r ⇒
          s"from ${r._1}" in { r._2.getBoolean("n") aka "boolean" must beFalse }
        }
      }
    }

    "converted to true by label" >> {
      val rs = Seq[(String, ResultSet)](
        "char" -> (new RowList1(classOf[Char]).
          withLabel(1, "n").append(row1('1'))).resultSet,
        "byte" -> (new RowList1(classOf[Byte]).
          withLabel(1, "n").append(row1(2.toByte))).resultSet,
        "short" -> (new RowList1(classOf[Short]).
          withLabel(1, "n").append(row1(3.toShort))).resultSet,
        "int" -> (new RowList1(classOf[Int]).
          withLabel(1, "n").append(row1(4))).resultSet,
        "long" -> (new RowList1(classOf[Long]).
          withLabel(1, "n").append(row1(5.toLong))).resultSet)

      rs.foreach(_._2.next)

      examplesBlock {
        rs foreach { r ⇒
          s"from ${r._1}" in { r._2.getBoolean("n") aka "boolean" must beTrue }
        }
      }
    }
  }

  numberGetterSpec[Byte]("Byte", 1.toByte)
  numberGetterSpec[Short]("Short", 1.toShort)
  numberGetterSpec[Int]("Int", 1)
  numberGetterSpec[Long]("Long", 1.toLong)
  numberGetterSpec[Float]("Float", 1.toFloat)
  numberGetterSpec[Double]("Double", 1.toDouble)

  "BigDecimal column from result set" should {
    val v = new JBigDec("1")

    "not be read by index when not on a row" in {
      (new RowList1(classOf[JBigDec]).append(row1(v)).resultSet.
        getBigDecimal(1) aka "get" must throwA[SQLException](
          message = "Not on a row")).
          and(new RowList1(classOf[JBigDec]).withLabel(1, "n").
            append(row1(v)).resultSet.getBigDecimal("n").
            aka("get") must throwA[SQLException]("Not on a row"))

    }

    "be expected one" in {
      val rs = new RowList1(classOf[JBigDec]).withLabel(1, "n").
        append(row1(v)).resultSet
      rs.next

      (rs.getBigDecimal(1) aka "big decimal by index" mustEqual v).
        and(rs.getBigDecimal("n") aka "big decimal by name" mustEqual v)
    }

    "be scaled one" in {
      val rs = new RowList1(classOf[JBigDec]).withLabel(1, "n").
        append(row1(new JBigDec("1.2345"))).resultSet

      rs.next

      (rs.getBigDecimal(1, 2).
        aka("big decimal by index") mustEqual new JBigDec("1.23")).
        and(rs.getBigDecimal("n", 3).
          aka("big decimal by name") mustEqual new JBigDec("1.234"))
    }

    "be null" in {
      val rs = new RowList1(classOf[JBigDec]).withLabel(1, "n").
        append(row1(null.asInstanceOf[JBigDec])).resultSet
      rs.next

      (rs.getBigDecimal(1) aka "big decimal" must beNull).
        and(rs.getBigDecimal(1, 1) aka "big decimal" must beNull).
        and(rs.getBigDecimal("n") aka "big decimal" must beNull).
        and(rs.getBigDecimal("n", 1) aka "big decimal" must beNull)
    }

    "be undefined" in {
      val rs = new RowList1(classOf[String]).withLabel(1, "n").
        append(row1("str")).resultSet
      rs.next

      (rs.getBigDecimal(1) aka "getBigDecimal" must throwA[SQLException](
        message = "Not a BigDecimal: 1")).
        and(rs.getBigDecimal(1, 1) aka "getBigDecimal" must {
          throwA[SQLException]("Not a BigDecimal: 1")
        }).
        and(rs.getBigDecimal("n").aka("getBigDecimal").must {
          throwA[SQLException]("Not a BigDecimal: n")
        }).
        and(rs.getBigDecimal("n", 1).aka("getBigDecimal").must {
          throwA[SQLException]("Not a BigDecimal: n")
        })

    }

    "converted by index" >> {
      val rs = Seq[(String, ResultSet)](
        "byte" -> (new RowList1(classOf[Byte]).
          append(row1(v.intValue.toByte))).resultSet,
        "short" -> (new RowList1(classOf[Short]).
          append(row1(v.intValue.toShort))).resultSet,
        "int" -> (new RowList1(classOf[Int]).
          append(row1(v.intValue))).resultSet,
        "long" -> (new RowList1(classOf[Long]).
          append(row1(v.longValue))).resultSet,
        "float" -> (new RowList1(classOf[Float]).
          append(row1(v.floatValue))).resultSet,
        "double" -> (new RowList1(classOf[Double]).
          append(row1(v.doubleValue))).resultSet)

      rs.foreach(_._2.next)

      examplesBlock {
        rs foreach { r ⇒
          s"from ${r._1}" in {
            r._2.getBigDecimal(1).doubleValue.
              aka("big decimal") mustEqual v.doubleValue
          }
        }
      }
    }

    "converted by label" >> {
      val rs = Seq[(String, ResultSet)](
        "byte" -> (new RowList1(classOf[Byte]).withLabel(1, "n").
          append(row1(v.intValue.toByte))).resultSet,
        "short" -> (new RowList1(classOf[Short]).withLabel(1, "n").
          append(row1(v.intValue.toShort))).resultSet,
        "int" -> (new RowList1(classOf[Int]).withLabel(1, "n").
          append(row1(v.intValue))).resultSet,
        "long" -> (new RowList1(classOf[Long]).withLabel(1, "n").
          append(row1(v.longValue))).resultSet,
        "float" -> (new RowList1(classOf[Float]).withLabel(1, "n").
          append(row1(v.floatValue))).resultSet,
        "double" -> (new RowList1(classOf[Double]).withLabel(1, "n").
          append(row1(v.doubleValue))).resultSet)

      rs.foreach(_._2.next)

      examplesBlock {
        rs foreach { r ⇒
          s"from ${r._1}" in {
            r._2.getBigDecimal("n").doubleValue.
              aka("big decimal") mustEqual v.doubleValue
          }
        }
      }
    }
  }

  temporalGetterSpec[Date]("Date", new Date(1, 2, 3))
  temporalGetterSpec[Time]("Time", new Time(1, 2, 3))
  temporalGetterSpec[Timestamp]("Timestamp", new Timestamp(1, 2, 3, 5, 6, 7, 8))
}

sealed trait RowListTest { specs: Specification ⇒
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
  implicit def tsByLabelWithCal(rs: ResultSet, n: String, c: Calendar): Timestamp = rs.getTimestamp(n, c)

  def temporalGetterSpec[D <: java.util.Date](name: String, v: D)(implicit mf: Manifest[D], byIndex: (ResultSet, Int) ⇒ D, byLabel: (ResultSet, String) ⇒ D, byIndexWithCal: (ResultSet, Int, Calendar) ⇒ D, byLabelWithCal: (ResultSet, String, Calendar) ⇒ D) =
    s"$name column from result set" should {
      "not be read by index when not on a row" in {
        (byIndex(new RowList1(mf.runtimeClass.asInstanceOf[Class[D]]).
          append(row1(v)).resultSet, 1).
          aka("get") must throwA[SQLException](
            message = "Not on a row")).
            and(byLabel(new RowList1(mf.runtimeClass.asInstanceOf[Class[D]]).
              withLabel(1, "n").append(row1(v)).resultSet, "n").
              aka("get") must throwA[SQLException]("Not on a row"))

      }

      "be expected one" in {
        val c = Calendar.getInstance
        val rs = new RowList1(mf.runtimeClass.asInstanceOf[Class[D]]).
          withLabel(1, "n").append(row1(v)).resultSet
        rs.next

        (byIndex(rs, 1) aka "big decimal by index" mustEqual v).
          and(byIndexWithCal(rs, 1, c) aka "big decimal by index" mustEqual v).
          and(byLabel(rs, "n") aka "big decimal by name" mustEqual v).
          and(byLabelWithCal(rs, "n", c) aka "big decimal by name" mustEqual v)
      }

      "be null" in {
        val c = Calendar.getInstance
        val rs = new RowList1(mf.runtimeClass.asInstanceOf[Class[D]]).
          withLabel(1, "n").append(row1(null.asInstanceOf[D])).resultSet

        rs.next

        (byIndex(rs, 1) aka "time" must beNull).
          and(byIndexWithCal(rs, 1, c) aka "time" must beNull).
          and(byLabel(rs, "n") aka "time" must beNull).
          and(byLabelWithCal(rs, "n", c) aka "time" must beNull)
      }

      "be undefined" in {
        val c = Calendar.getInstance
        val rs = new RowList1(classOf[String]).withLabel(1, "n").
          append(row1("str")).resultSet
        rs.next

        (byIndex(rs, 1) aka "get" must throwA[SQLException](
          message = s"Not a $name: 1")).
          and(byIndexWithCal(rs, 1, c) aka "get" must throwA[SQLException](
            message = s"Not a $name: 1")).
          and(byLabel(rs, "n").
            aka("get") must throwA[SQLException](s"Not a $name: n")).
          and(byLabelWithCal(rs, "n", c).
            aka("get") must throwA[SQLException](s"Not a $name: n"))

      }

      "converted by index" >> {
        val rs = Seq[(String, ResultSet)](
          "date" -> new RowList1(classOf[Date]).
            append(row1(new Date(v.getTime))).resultSet,
          "time" -> new RowList1(classOf[Time]).
            append(row1(new Time(v.getTime))).resultSet,
          "ts" -> new RowList1(classOf[Timestamp]).
            append(row1(new Timestamp(v.getTime))).resultSet)

        rs.foreach(_._2.next)

        examplesBlock {
          rs foreach { r ⇒
            s"from ${r._1}" in {
              byIndex(r._2, 1) aka "get" must not(throwA[SQLException])
            }
          }
        }
      }

      "converted by label" >> {
        val rs = Seq[(String, ResultSet)](
          "date" -> new RowList1(classOf[Date]).withLabel(1, "n").
            append(row1(new Date(v.getTime))).resultSet,
          "time" -> new RowList1(classOf[Time]).withLabel(1, "n").
            append(row1(new Time(v.getTime))).resultSet,
          "ts" -> new RowList1(classOf[Timestamp]).withLabel(1, "n").
            append(row1(new Timestamp(v.getTime))).resultSet)

        rs.foreach(_._2.next)

        examplesBlock {
          rs foreach { r ⇒
            s"from ${r._1}" in {
              byLabel(r._2, "n") aka "get" must not(throwA[SQLException])
            }
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

  def numberGetterSpec[N](name: String, v: N)(implicit num: Numeric[N], mf: Manifest[N], byIndex: (ResultSet, Int) ⇒ N, byLabel: (ResultSet, String) ⇒ N) =
    s"$name column from result set" should {
      "not be read by index when not on a row" in {
        (byIndex(new RowList1(mf.runtimeClass.asInstanceOf[Class[N]]).
          append(row1(v)).resultSet, 1).
          aka("get") must throwA[SQLException](
            message = "Not on a row")).
            and(byLabel(new RowList1(mf.runtimeClass.asInstanceOf[Class[N]]).
              withLabel(1, "n").append(row1(v)).resultSet, "n").
              aka("get") must throwA[SQLException]("Not on a row"))

      }

      "be expected one" in {
        val rs = new RowList1(mf.runtimeClass.asInstanceOf[Class[N]]).
          withLabel(1, "n").append(row1(v)).resultSet
        rs.next

        (byIndex(rs, 1) aka s"$name by index" mustEqual v).
          and(byLabel(rs, "n") aka s"$name by name" mustEqual v)
      }

      "be null (0)" in {
        val rs = new RowList1(mf.runtimeClass.asInstanceOf[Class[N]]).
          withLabel(1, "n").append(row1(null.asInstanceOf[N])).resultSet
        rs.next

        (byIndex(rs, 1) aka s"$name" mustEqual 0).
          and(byLabel(rs, "n") aka s"$name" mustEqual 0)
      }

      "be undefined (-1)" in {
        val rs = new RowList1(classOf[String]).withLabel(1, "n").
          append(row1("str")).resultSet
        rs.next

        (byIndex(rs, 1) aka s"$name" mustEqual -1).
          and(byLabel(rs, "n") aka s"$name" mustEqual -1)

      }

      "converted by index" >> {
        val rs = Seq[(String, ResultSet)](
          "byte" -> new RowList1(classOf[Byte]).
            append(row1(num.toInt(v).toByte)).resultSet,
          "short" -> new RowList1(classOf[Short]).
            append(row1(num.toInt(v).toShort)).resultSet,
          "int" -> new RowList1(classOf[Int]).append(row1(num.toInt(v))).resultSet,
          "long" -> new RowList1(classOf[Long]).
            append(row1(num.toLong(v))).resultSet,
          "float" -> new RowList1(classOf[Float]).
            append(row1(num.toFloat(v))).resultSet,
          "double" -> new RowList1(classOf[Double]).
            append(row1(num.toDouble(v))).resultSet)

        rs.foreach(_._2.next)

        examplesBlock {
          rs foreach { r ⇒
            s"from ${r._1}" in { byIndex(r._2, 1) aka s"$name" mustEqual 1 }
          }
        }
      }

      "converted by label" >> {
        val rs = Seq[(String, ResultSet)](
          "byte" -> new RowList1(classOf[Byte]).withLabel(1, "n").
            append(row1(num.toInt(v).toByte)).resultSet,
          "short" -> new RowList1(classOf[Short]).withLabel(1, "n").
            append(row1(num.toInt(v).toShort)).resultSet,
          "int" -> new RowList1(classOf[Int]).withLabel(1, "n").
            append(row1(num.toInt(v))).resultSet,
          "long" -> new RowList1(classOf[Long]).withLabel(1, "n").
            append(row1(num.toLong(v))).resultSet,
          "float" -> new RowList1(classOf[Float]).withLabel(1, "n").
            append(row1(num.toFloat(v))).resultSet,
          "double" -> new RowList1(classOf[Double]).withLabel(1, "n").
            append(row1(num.toDouble(v))).resultSet)

        rs.foreach(_._2.next)

        examplesBlock {
          rs foreach { r ⇒
            s"from ${r._1}" in { byLabel(r._2, "n") aka s"$name" mustEqual 1 }
          }
        }
      }
    }

}
